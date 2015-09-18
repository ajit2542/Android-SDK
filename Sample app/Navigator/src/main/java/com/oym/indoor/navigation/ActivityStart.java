package com.oym.indoor.navigation;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

public class ActivityStart extends AppCompatActivity {

	private boolean isAlertShown = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.container);
		
//		FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/NeoSansPro-Medium.ttf");
//		FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/Roboto-Light.ttf");
		FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/HelveticaNeue-Light.otf");

		getSupportActionBar().hide();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();        
		isAlertShown = false;

		// Check Bluetooth
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
		if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
			Toast.makeText(this, "Enabling bluetooth", Toast.LENGTH_LONG).show();
		}

		// Check Location (API dependant)
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			if(!(provider.contains("gps") && provider.contains("network"))){
				showLocationDialog();
			}
		} else {
			int mode;
			try {
				mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
			} catch (SettingNotFoundException e) {
				mode = Settings.Secure.LOCATION_MODE_OFF;
			}
			if (mode != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
				showLocationDialog();

			}
		}
		
		Fragment fragment = new FragmentSplashscreen();
		Bundle extras = new Bundle();
		extras.putBoolean(FragmentSplashscreen.KEY_CANCELLOGIN, isAlertShown);
		fragment.setArguments(extras);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.container, fragment)
		.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:			
			getSupportFragmentManager().popBackStack();
			getSupportActionBar().hide();
			return true;			

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void showLocationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.ASLocationMessage);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// FIXME Should the app continue?
				finish();
			}
		});
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		});
		builder.setCancelable(false);
		builder.create().show();
		isAlertShown = true;
	}
	
}
