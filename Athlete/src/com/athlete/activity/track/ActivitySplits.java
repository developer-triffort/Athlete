package com.athlete.activity.track;

import java.text.DecimalFormat;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.adapter.BaseListAdapter;
import com.athlete.google.android.apps.mytracks.util.UnitConversions;
import com.athlete.model.Split;
import com.athlete.util.AnalyticsUtils;

public class ActivitySplits extends BaseActivity {
	private ListView listSplit;
	private BaseListAdapter<Split> adapter;
	private List<Split> splits;
	private double maxAvg;
	private TabActivityTrack parentActivity;
	private String type;
	private final int hundredPercent = 100;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.actv_split);
		setView();
	}

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivitySplits.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.SPLIT);
	}

	public void updateSplits() {
		if (parentActivity != null) {
			splits = parentActivity.getListOfSplit();
			for (int i = 0; i < splits.size(); i++) {
				if (splits.size() > i + 1) {
					maxAvg = Math.max(
							maxAvg,
							Math.max(splits.get(i).getAvgPace(),
									splits.get(i + 1).getAvgPace()));
				}
			}
		}
		if (splits != null && !splits.isEmpty()) {
			setAdapter();
		}
	}

	private void setView() {
		type = isMetric() ? getString(R.string.split_kilometer)
				: getString(R.string.split_mile);
		type += " ";
		parentActivity = ((TabActivityTrack) getParent());
		parentActivity.setActivitySplits(this);
		listSplit = (ListView) findViewById(R.id.listViewSplit);
		updateSplits();
	}

	private void setAdapter() {
		adapter = new BaseListAdapter<Split>(this, splits, R.layout.item_split) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				final Split item = getItem(position);

				if (convertView == null) {
					holder = new ViewHolder();
					convertView = getLayout();
					holder.progressSplit = (ProgressBar) convertView
							.findViewById(R.id.progressBarAVGPace);
					holder.txtClimb = (TextView) convertView
							.findViewById(R.id.txtClimb);
					holder.txtAVGPace = (TextView) convertView
							.findViewById(R.id.txtAvgPace);
					convertView.setTag(holder);
				} else {
					if (convertView.getTag() != null
							&& convertView.getTag() instanceof ViewHolder) {
						holder = (ViewHolder) convertView.getTag();
					} else {
						return getLayout();
					}

				}
				if (maxAvg != 0) {
					holder.progressSplit
							.setMax((int) (maxAvg * hundredPercent));
				} else {
					holder.progressSplit
							.setMax((int) (item.getAvgPace() * hundredPercent));
				}
				holder.progressSplit
						.setProgress((int) (item.getAvgPace() * hundredPercent));
				holder.txtAVGPace.setText(type
						+ (splits.size() - position)
						+ " - "
						+ String.valueOf(
								new DecimalFormat("0.00").format(item
										.getAvgPace())).replace(",", "'")
								.replace(".", "'"));
				holder.txtClimb.setText(String.valueOf((int) (Math.abs(item
						.getClimb() * UnitConversions.M_TO_FT)))
						+ " " + getString(R.string.ft));
				if (item.getClimb() >= 0) {
					holder.txtClimb.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.splits_arrow_up), null, null,
							null);
				} else {
					holder.txtClimb.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.splits_arrow_down), null, null,
							null);
				}
				return convertView;

			}
		};
		listSplit.setAdapter(adapter);
	}
}
