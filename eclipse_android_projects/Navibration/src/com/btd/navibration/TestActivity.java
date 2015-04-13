package com.btd.navibration;

import com.btd.navibration.R;

import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.util.Log;

/**
 * 
 * @author 
 * this activity is only used for testing purposes
 *
 */
public class TestActivity extends Activity implements OnClickListener {

	// private static final String DEVICE_ADDRESS = "00:06:66:43:45:AF";

	// private Button buttonLight = null;
	// private Button buttonLight2 = null;
	// private Button buttonLight3 = null;
	private Button buttonMaps = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_test);
		// Amarino.connect(this, DEVICE_ADDRESS);
		/*
		 * buttonLight = (Button) findViewById(R.id.buttonLight);
		 * buttonLight.setOnClickListener(this);
		 * buttonLight.setVisibility(View.VISIBLE); buttonLight2 = (Button)
		 * findViewById(R.id.buttonLight2);
		 * buttonLight2.setOnClickListener(this);
		 * buttonLight2.setVisibility(View.VISIBLE); buttonLight3 = (Button)
		 * findViewById(R.id.buttonLight3);
		 * buttonLight3.setOnClickListener(this);
		 * buttonLight3.setVisibility(View.VISIBLE);
		 */
		buttonMaps = (Button) findViewById(R.id.buttonMaps);
		buttonMaps.setOnClickListener(this);
		buttonMaps.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onPause();
		Log.d("MainActivity", "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Amarino.disconnect(this, DEVICE_ADDRESS);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		/*
		 * case R.id.buttonLight: //Amarino.sendDataToArduino(this,
		 * DEVICE_ADDRESS, 'r', 1); Log.d("MainActivity", "bla"); break; case
		 * R.id.buttonLight2: //Amarino.sendDataToArduino(this, DEVICE_ADDRESS,
		 * 'g', 3); Log.d("MainActivity", "bla"); break; case R.id.buttonLight3:
		 * //Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 't', 7); break;
		 */
		case R.id.buttonMaps:
			// Bundle b = new Bundle();
			// b.putFloat("sensor",sensorReading);
			Intent mapsIntent = new Intent(this, MapsActivity.class);
			// myIntent.putExtras(b);
			startActivity(mapsIntent);
			break;

		}

	}

}
