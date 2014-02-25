package com.athlete.activity.setup;

import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.R;
import com.athlete.model.ProfileUser;
import com.athlete.model.User;
import com.athlete.util.AnalyticsUtils;

public class ActivityLocation extends BaseSetupActivity implements
		LocationListener {
	private double latitude, longitude;
	private LocationManager mLocManager;
	private String location = "";
	private TextView txtDetectLocation;
	private Dialog mDialog;
	private EditText edTxtCustomLocation;
	private final String LOCATION_NAME = "location_name";
	private final String sharedKeyIsDetect = "isDetectShared";
	private boolean isDetect;

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityLocation.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_LOCATION);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_setup_location);
		isDetect = getShared().getBoolean(sharedKeyIsDetect, false);
		txtDetectLocation = (TextView) findViewById(R.id.txtDetectLocation);
		edTxtCustomLocation = (EditText) findViewById(R.id.edTextCustom);
		edTxtCustomLocation.setImeOptions(EditorInfo.IME_ACTION_DONE);
		location = profileUser.getLocationName();
		if (location == null) {
			location = "";
		}
		setText();
		findViewById(R.id.layoutDetectLocation).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						AnalyticsUtils.sendPageViews(ActivityLocation.this,

						AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_LOCATION,
								AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
								AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
								"detect", 0);
						hideKeyboard(ActivityLocation.this);
						createProgressDialog();
						isDetect = true;
						getShared().edit()
								.putBoolean(sharedKeyIsDetect, isDetect)
								.commit();
						setText();
						mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

						mLocManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER, 0, 0,
								ActivityLocation.this);
						mLocManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER, 0, 0,
								ActivityLocation.this);

					}
				});
		edTxtCustomLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isDetect) {
					isDetect = false;
					getShared().edit().putBoolean(sharedKeyIsDetect, isDetect)
							.commit();
					setText();
				}

			}
		});
		findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						onBackPressed();

					}
				});
		edTxtCustomLocation
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {

						if (actionId == EditorInfo.IME_ACTION_DONE) {
							hideKeyboard(ActivityLocation.this);
							if (!location.equalsIgnoreCase(edTxtCustomLocation
									.getText().toString())) {
								AnalyticsUtils
										.sendPageViews(
												ActivityLocation.this,

												AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SELECT_LOCATION,
												AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
												AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
												"custom", 0);
								createProgressDialog();
								location = edTxtCustomLocation.getText()
										.toString();
								isDetect = false;
								getShared()
										.edit()
										.putBoolean(sharedKeyIsDetect, isDetect)
										.commit();
								setText();

								try {
									updateLocation();
								} catch (Exception e) {

								}
								hideProgress();
							}
							return true;
						}

						return false;
					}
				});

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		hideKeyboard(ActivityLocation.this);
	}

	private void setText() {
		if (isDetect) {
			txtDetectLocation.setText(location);
			edTxtCustomLocation.setText("");
		} else {
			edTxtCustomLocation.setText(location);
			txtDetectLocation.setText("");
		}
	}

	public void getAddress(double lat, double lng) {
		Geocoder geocoder = new Geocoder(ActivityLocation.this,
				Locale.getDefault());
		try {
			List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
			Address obj = addresses.get(0);
			location = obj.getLocality() + ", " + obj.getAdminArea();
			isDetect = true;
			getShared().edit().putBoolean(sharedKeyIsDetect, true).commit();
			setText();

			updateLocation();
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}

		hideProgress();
	}

	private void updateLocation() throws Exception {
		jsonObjSend = new JSONObject();

		jsonObjSend.put(LOCATION_NAME, location);

		profileUser.setLocationName(location);
		baseBl.createOrUpdate(ProfileUser.class, profileUser);
		currentUser.setProfileUser(profileUser);
		baseBl.createOrUpdate(User.class, currentUser);
		updateUser(null);
	}

	private void hideProgress() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	private void createProgressDialog() {
		if (mDialog == null) {
			mDialog = new ProgressDialog(ActivityLocation.this);
			((ProgressDialog) mDialog).setMessage(getString(R.string.updating));
			mDialog.show();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		getAddress(latitude, longitude);
		if (location != null) {
			mLocManager.removeUpdates(this);
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		Toast.makeText(ActivityLocation.this, "Gps Disabled",
				Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}

	@Override
	public void onProviderEnabled(String arg0) {

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

	}
}
