package com.athlete.activity.track;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.athlete.R;
import com.athlete.activity.auth.ActivityBaseAuth;
import com.athlete.exception.InvalidActivityTypeException;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.athlete.google.android.apps.mytracks.content.Track;
import com.athlete.model.ActivityType;

import java.util.List;

public class BaseSaveTrackActivity extends ActivityBaseAuth {
	/**
	 * @author edBaev
	 */
	protected double distance;
	/*
	 * 0-mi, 1-km
	 */
	protected int typeID;
	protected Long time;
	public static final String EXTRA_TRACK_ID = "track_id";
	public static final String EXTRA_NEW_TRACK = "new_track";

	protected static final String TAG = BaseSaveTrackActivity.class
			.getSimpleName();

	protected Long trackId;
	protected MyTracksProviderUtils myTracksProviderUtils;
	protected Track track;

	protected EditText name, description;
	protected TextView mTxtRunType, mTxtRouteView, mTxtTitle;

    protected TextView mTxtActivityType;
    protected TextView mTxtActivitySubtype;

	protected String mFBAcces;
	protected CheckBox checkFB;

    protected ActivityType workoutActivityType;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

	}

    protected void showSelectDialogForActivitySubtype(){
        final List<ActivityType.ActivitySubType> subtypes = workoutActivityType.getSubtypes();

        ListAdapter adapter = new ArrayAdapterForActivitySubType(this, subtypes, workoutActivityType);

        String dialogTitle = String.format("%s type", mTxtActivityType.getText());
        new AlertDialog.Builder(this).setTitle(
                dialogTitle
        ).setAdapter(
                adapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        mTxtActivitySubtype.setText(subtypes.get(position).getName());
                       /* if (track != null) {
                            track.setActivitySubType(subtypes.get(position).getName());
                        }*/
                    }
                }
        ).show();
    }

    private abstract class BaseArrayAdapterForTypeLists<T> extends ArrayAdapter<T>{
        private List<T> items;
        public BaseArrayAdapterForTypeLists(Context context, List<T> items){
            super(context, R.layout.select_activity_type_dialog_item, R.id.txtActivityTypeName, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            TextView txtActivityTypeName = (TextView) v.findViewById(R.id.txtActivityTypeName);
            TextView txtActivityTypeIcon = (TextView) v.findViewById(R.id.txtActivityTypeIcon);
            if(Build.VERSION.SDK_INT < 11){
                txtActivityTypeName.setTextColor(getResources().getColor(R.color.black));
            }
            Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/athlete-regular-webfont.ttf");
            txtActivityTypeIcon.setTypeface(typeFace);

            T item = items.get(position);
            processViewForItem(item, txtActivityTypeName, txtActivityTypeIcon);
            return v;
        }
        protected abstract void processViewForItem(T item, TextView txtActivityTypeName, TextView txtActivityTypeIcon);
    }
    private class ArrayAdapterForActivityType extends BaseArrayAdapterForTypeLists<ActivityType>{
        public ArrayAdapterForActivityType(Context context, List<ActivityType> items){
            super(context, items);
        }
        @Override
        protected void processViewForItem(ActivityType item, TextView txtActivityTypeName, TextView txtActivityTypeIcon) {
            txtActivityTypeName.setText(item.getName());
            txtActivityTypeIcon.setText(item.getCharRepresentation());
            txtActivityTypeIcon.setTextColor(getResources().getColor(R.color.default_activity_type_color));
        }
    }
    private class ArrayAdapterForActivitySubType extends BaseArrayAdapterForTypeLists<ActivityType.ActivitySubType>{
        ActivityType workoutActivityType;
        public ArrayAdapterForActivitySubType(Context context, List<ActivityType.ActivitySubType> items, ActivityType workoutActivityType){
            super(context, items);
            this.workoutActivityType = workoutActivityType;
        }
        @Override
        protected void processViewForItem(ActivityType.ActivitySubType item, TextView txtActivityTypeName, TextView txtActivityTypeIcon) {
            txtActivityTypeName.setText(item.getName());
            txtActivityTypeIcon.setText(workoutActivityType.getCharRepresentation());
            txtActivityTypeIcon.setTextColor(getResources().getColor(item.getColorResource()));
        }
    }
	protected void setAdapterForDialog() {
        final List<ActivityType> registeredActivityTypes = ActivityType.getRegisteredActivityTypes();

        ListAdapter adapter = new ArrayAdapterForActivityType(this, registeredActivityTypes);

        new AlertDialog.Builder(this).setTitle(
                getString(R.string.label_activity_type)
        ).setAdapter(
                adapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        ActivityType activityType = registeredActivityTypes.get(position);
                        BaseSaveTrackActivity.this.workoutActivityType = activityType;
                        mTxtActivityType.setText(activityType.getName());
                        if(activityType.getSubtypes().size() > 0){
                            mTxtActivitySubtype.setText(activityType.getSubtypes().get(0).getName());
                        }
                        /*if (track != null) {
                            track.setActivityType(activityType.getName());
                        }*/
                    }
                }
        ).show();
	}

	protected void setAdapterForDialogPrivacy() {
		final String[] items = { getString(R.string.label_private),
				getString(R.string.label_public), getString(R.string.friends) };
		ListAdapter adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, android.R.id.text1, items) {
			public View getView(int position, View convertView, ViewGroup parent) {
				return super.getView(position, convertView, parent);
			}
		};

		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.label_route_viewable))
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						mTxtRouteView.setText(items[item].toString());
						/*if (track != null) {
							track.setPrivacy(items[item].toString());
						}*/
					}
				}).show();
	}

	protected static class Item {
		public final String text;
		public final int icon;

		public Item(String text, Integer icon) {
			this.text = text;
			this.icon = icon;
		}

        public String getText() {
            return text;
        }

        public int getIcon() {
            return icon;
        }

        @Override
		public String toString() {
			return text;
		}
	}
}
