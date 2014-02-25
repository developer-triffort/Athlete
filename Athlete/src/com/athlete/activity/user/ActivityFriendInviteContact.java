package com.athlete.activity.user;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.util.AnalyticsUtils;

public class ActivityFriendInviteContact extends BaseActivity {
	private ListView mListView;
	private Cursor cursorEmail;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_contact);
		mListView = (ListView) findViewById(R.id.listViewContact);
		populateContactList();
		findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityFriendInviteContact.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FRIEND_INVITE_CONTACT);
	}

	private void populateContactList() {
		// Build adapter with contact entries
		// get all emails

		final Uri uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
		final String[] projection = new String[] {
				ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Email.DATA };
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP
				+ " = '1' AND " + ContactsContract.CommonDataKinds.Email.DATA
				+ "  > 0 ";
		String[] selectionArgs = null;
		final String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		String[] fields = new String[] { ContactsContract.Data.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Email.DATA };
		cursorEmail = getContentResolver().query(uri, projection, selection,
				selectionArgs, sortOrder);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.row_artists, cursorEmail, fields, new int[] {
						R.id.songname, R.id.rowartist });

		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				cursorEmail.moveToPosition(arg2);
				int columnIndex = cursorEmail
						.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA);
				String strEmail = cursorEmail.getString(columnIndex);
				Intent intent = new Intent().putExtra(
						Constants.INTENT_KEY.EMAIL, strEmail);
				columnIndex = cursorEmail
						.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME);
				String strName = cursorEmail.getString(columnIndex);
				intent.putExtra(Constants.INTENT_KEY.NAME, strName);
				AnalyticsUtils
						.sendPageViews(
								ActivityFriendInviteContact.this,
								AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FRIEND_INVITE_CONTACT,
								AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
								AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
								AnalyticsUtils.GOOGLE_ANALYTICS.INVITE_EMAIL, 0);
				setResult(Constants.REQUEST_CODE_TRANSFER, intent);
				finish();

			}

		});
	}
}
