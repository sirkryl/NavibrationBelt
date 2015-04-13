package com.btd.navibration;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;

import com.btd.navibration.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;

public class MapsActivity extends FragmentActivity implements OnClickListener,
		LocationListener, LocationSource {
	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */

	private static final String DEVICE_ADDRESS = "00:06:66:43:45:AF";
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private GoogleMap mMap;
	private OnLocationChangedListener mListener;
	private LocationManager locationManagerGPS;
	private LocationManager locationManagerNetwork;
	private double longitude = 0;
	private double latitude = 0;
	private Document newDoc = null;
	private GeoPoint currentLocation = null;
	private Location bestLocation = null;
	private TextView distanceText = null;
	private int vibrationTime = 5;
	private boolean threwException = false;
	private Button buttonNavi = null;
	private EditText destinationEditText = null;
	private String destination;
	private ArrayList<GeoPoint> coords = new ArrayList<GeoPoint>();
	private ArrayList<String> directives = new ArrayList<String>();
	private ArrayList<MarkerOptions> marker = new ArrayList<MarkerOptions>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_maps);

		Amarino.connect(this, DEVICE_ADDRESS);

		// Amarino.connect(this, DEVICE_ADDRESS);
		buttonNavi = (Button) findViewById(R.id.buttonNavi);
		buttonNavi.setOnClickListener(this);
		buttonNavi.setVisibility(View.VISIBLE);
		destinationEditText = (EditText) findViewById(R.id.destination);
		distanceText = (TextView) findViewById(R.id.distance);

		locationManagerGPS = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManagerNetwork = (LocationManager) getSystemService(LOCATION_SERVICE);

		if (locationManagerGPS != null) {

			// boolean gpsIsEnabled = false;
			boolean gpsIsEnabled = locationManagerGPS
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			// boolean networkIsEnabled = false;
			boolean networkIsEnabled = locationManagerNetwork
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (gpsIsEnabled) {
				Log.i("GPSENABLED", "GPS enabled");
				locationManagerGPS.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 500, 0, this);

			} else {
				Log.e("Error", "GPS disabled");
				// Show an error dialog that GPS is disabled.
			}
			if (networkIsEnabled) {
				locationManagerNetwork.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 500, 0, this);
			}

		} else {
			Log.e("Error", "LocationManager null");
			// Show a generic error dialog since LocationManager is null for
			// some reason
		}

		setUpMapIfNeeded();

	}

	@Override
	protected void onStop() {
		super.onStop();
		Amarino.disconnect(this, DEVICE_ADDRESS);
	}

	@Override
	public void onPause() {
		if (locationManagerGPS != null) {
			locationManagerGPS.removeUpdates(this);
		}
		if (locationManagerNetwork != null) {
			locationManagerNetwork.removeUpdates(this);
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		setUpMapIfNeeded();

		if (locationManagerGPS != null) {
			mMap.setMyLocationEnabled(true);
		}
		if (locationManagerNetwork != null) {
			mMap.setMyLocationEnabled(true);
		}

	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play
	 * services APK is correctly installed) and the map has not already been
	 * instantiated.. This will ensure that we only ever call
	 * {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt
	 * for the user to install/update the Google Play services APK on their
	 * device.
	 * <p>
	 * A user can return to this Activity after following the prompt and
	 * correctly installing/updating/enabling the Google Play services. Since
	 * the Activity may not have been completely destroyed during this process
	 * (it is likely that it would only be stopped or paused),
	 * {@link #onCreate(Bundle)} may not be called again so we should call this
	 * method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.

			if (mMap != null) {
				setUpMap();
			}

			// This is how you register the LocationSource
			mMap.setLocationSource(this);
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera. In this case, we just add a marker near Africa.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {
		mMap.setMyLocationEnabled(true);
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
	}

	@Override
	public void deactivate() {
		mListener = null;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.i("Onlocationchanged", "new location from " + location.getProvider());
		if (mListener != null) {
			if (isBetterLocation(location, bestLocation)) {
				mListener.onLocationChanged(location);
				// Move the camera to the user's location once it's available!
				mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
						location.getLatitude(), location.getLongitude())));
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				currentLocation = new GeoPoint((int) (latitude * 1E6),
						(int) (longitude * 1E6));
				if (marker.size() != 0) {

					Location nextMarker = new Location("next");
					nextMarker
							.setLatitude(marker.get(0).getPosition().latitude);
					nextMarker
							.setLongitude(marker.get(0).getPosition().longitude);
					float distance = nextMarker.distanceTo(location);
					distanceText.setText("  in " + Float.toString(distance)
							+ "m " + directives.get(0));
					distanceText.setVisibility(View.VISIBLE);
					// Log.i("Distance","Distance.. "+goal.distanceTo(location));
					int distanceDiff = 20;

					if (directives.get(0).equals("ziel"))
						distanceDiff = 10;
					if (distance <= distanceDiff) {// dosomethingvibra
						char dataToSend = 'g';
						if (directives.get(0).equals("rechts")) {
							dataToSend = 'r';
						} else if (directives.get(0).equals("links")) {
							dataToSend = 'l';
						} else if (directives.get(0).equals("gerade")) {
							dataToSend = 'g';
						} else
							dataToSend = 't';
						Amarino.sendDataToArduino(this, DEVICE_ADDRESS,
								dataToSend, vibrationTime);
						Log.i("Arduino", "Sent direction: " + directives.get(0));

						directives.remove(0);
						marker.remove(0);
					}
					this.drawLines();
				} else
					distanceText.setVisibility(View.INVISIBLE);
			}

		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.i("Provider", "provider disabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.i("Provider", "provider enabled");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		Log.i("Provider", "status changed");
	}

	public void getRoute() {
		String url = getUrl();
		new RetreiveDocument().execute(url);
		while (newDoc == null) {
			if (threwException)
				break;
		}
		Document doc = newDoc;
		parseDoc(doc);
		newDoc = null;
	}

	private void parseDoc(Document doc) {
		NodeList locationList = doc.getElementsByTagName("end_location");
		for (int i = 0; i < locationList.getLength() - 1; i++) {
			double lat = 0;
			double lng = 0;

			Node locationNode = locationList.item(i);
			if (locationNode.getNodeType() == Node.ELEMENT_NODE) {

				Element locElement = (Element) locationNode;

				// -------
				NodeList latList = locElement.getElementsByTagName("lat");
				Element latElement = (Element) latList.item(0);

				NodeList latTextList = latElement.getChildNodes();
				// Log.i("Marker", "Lat : "
				// + ((Node) latTextList.item(0)).getNodeValue().trim());
				String latStr = ((Node) latTextList.item(0)).getNodeValue()
						.trim();
				lat = Double.parseDouble(latStr);
				// -------
				NodeList lngList = locElement.getElementsByTagName("lng");
				Element lngElement = (Element) lngList.item(0);

				NodeList textLngList = lngElement.getChildNodes();
				// Log.i("Marker", "Lng : "
				// + ((Node) textLngList.item(0)).getNodeValue().trim());
				String lngStr = ((Node) textLngList.item(0)).getNodeValue()
						.trim();
				lng = Double.parseDouble(lngStr);

			}// end of if clause
			GeoPoint newPoint = new GeoPoint((int) (lat * 1E6),
					(int) (lng * 1E6));
			coords.add(newPoint);

			MarkerOptions newMarker = new MarkerOptions()
					.position(new LatLng(lat, lng))
					.title("Waypoint #"+(i+1))
					.snippet("")
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

			marker.add(newMarker);

		}

		String directive = "";
		NodeList directiveList = doc.getElementsByTagName("html_instructions");
		for (int i = 1; i < directiveList.getLength(); i++) {
			directive = "";

			Node locationNode = (Element) directiveList.item(i);
			NodeList directiveTextList = locationNode.getChildNodes();

			String latStr = ((Node) directiveTextList.item(0)).getNodeValue()
					.trim();
			if (latStr.contains("<b>Rechts</b> abbiegen"))
				directive = "rechts";
			else if (latStr.contains("<b>Links</b> abbiegen"))
				directive = "links";
			else
				directive = "gerade";
			// Log.i("Marker", "Directive : " + directive);

			// end of if clause
			directives.add(directive);
		}
		// Log.d("SIZE: ", "directives: " + directives.size() + ", marker: "
		// + marker.size());
		if (directives.size() != marker.size()) {
			directives.add("ziel");
		}

		this.drawLines();
	}

	private String getUrl() {

		StringBuilder urlString = new StringBuilder();

		urlString.append("http://maps.googleapis.com/maps/api/directions/xml?");
		urlString.append("origin=");
		urlString.append(Double.toString((double) currentLocation
				.getLatitudeE6() / 1.0E6));
		urlString.append(",");
		urlString.append(Double.toString((double) currentLocation
				.getLongitudeE6() / 1.0E6));
		urlString.append("&destination=");// to
		urlString.append(URLEncoder.encode(destination));
		urlString.append("&sensor=false&mode=walking&language=de");
		return urlString.toString();
		// "http://maps.googleapis.com/maps/api/directions/xml?origin=Gymnasiumstra%C3%9Fe,%20Wien&destination=Philippovichgasse,%20Wien&sensor=false&mode=walking&language=de";

	}

	class RetreiveDocument extends AsyncTask<String, Void, Document> {

		protected Document doInBackground(String... params) {
			threwException = false;
			Document doc = null;
			HttpURLConnection urlConnection = null;
			URL url = null;
			try {
				url = new URL(params[0].toString());
				Log.i("URL", "Google Maps URL: " + url);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				urlConnection.setDoInput(true);
				urlConnection.connect();

				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(urlConnection.getInputStream());
				doc.normalize();

			} catch (Exception e) {
				e.printStackTrace();
				Log.i("MapsActivity", "Exception in HTTP");
				threwException = true;

			}
			Log.i("Routin", "jo:");
			newDoc = doc;
			return doc;
		}
	}

	private void drawLines() {
		mMap.clear();
		if (marker.size() > 0) {
			for (int i = 0; i < marker.size(); i++) {
				marker.get(i).getPosition();
				if (i == 0) {
					mMap.addPolyline(new PolylineOptions()
							.add(marker.get(i).getPosition(),
									new LatLng(latitude, longitude)).width(4)
							.color(Color.RED));
				} else {
					mMap.addPolyline(new PolylineOptions()
							.add(marker.get(i).getPosition(),
									marker.get(i - 1).getPosition()).width(4)
							.color(Color.BLUE));
				}
				mMap.addMarker(marker.get(i));
			}
		}
	}

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.buttonNavi:
			this.destination = destinationEditText.getText().toString();
			//Log.d("Destination", "dest: " + destination);
			coords.clear();
			marker.clear();
			directives.clear();
			mMap.clear();
			this.getRoute();
			break;

		}

	}
}
