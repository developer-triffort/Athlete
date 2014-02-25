package com.athlete.adapter;

import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.control.sectionlistview.DataWorkout;
import com.athlete.model.WorkOut;
import com.segment.SegmentAdapter;

public class SectionWorkoutAdapter extends SegmentAdapter {

	private Context ctx;
	private List<Pair<String, List<WorkOut>>> all;
	private final int alpha = 24;
	private final int backgroundColor = 0x3a3a3a;

	/**
	 * @author edBaev
	 */
	public SectionWorkoutAdapter(Context ctx, List<WorkOut> workOut) {
		this.ctx = ctx;
		all = DataWorkout.getAllData(workOut, ctx);
	}

	@Override
	public int getCount() {
		int res = 0;
		for (int i = 0; i < all.size(); i++) {
			res += all.get(i).second.size();
		}
		return res;
	}

	@Override
	public WorkOut getItem(int position) {
		int c = 0;
		for (int i = 0; i < all.size(); i++) {
			if (position >= c && position < c + all.get(i).second.size()) {
				return all.get(i).second.get(position - c);
			}
			c += all.get(i).second.size();
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	protected void onNextPageRequested(int page) {
	}

	@Override
	protected void bindSectionHeader(View view, int position,
			boolean displaySectionHeader) {
		if (displaySectionHeader) {
			view.findViewById(R.id.linearHeader).setVisibility(View.VISIBLE);
			TextView lSectionTitle = (TextView) view.findViewById(
					R.id.linearHeader).findViewById(R.id.header);
			TextView countDistance = (TextView) view.findViewById(
					R.id.linearHeader).findViewById(
					R.id.txtCountDistanceAndTime);
			String string[] = getSections()[getSectionForPosition(position)]
					.split(Constants.REGULAR_EXPRESSION_LOG);
			if (string != null && string.length > 0)
				lSectionTitle.setText(string[0]);
			if (string != null && string.length > 1) {

				countDistance.setText(string[1]);
			}
			
		} else {
			view.findViewById(R.id.linearHeader).setVisibility(View.GONE);
		}
	}

	@Override
	public View getSectionView(int position, View convertView, ViewGroup parent) {
		View res = convertView;
		if (res == null) {
			res = LayoutInflater.from(ctx).inflate(R.layout.item_log, null);
		}

		return res;
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		header.findViewById(R.id.linearHeader).setVisibility(View.VISIBLE);
		TextView lSectionTitle = (TextView) header.findViewById(
				R.id.linearHeader).findViewById(R.id.header);
		TextView countDistance = (TextView) header.findViewById(
				R.id.linearHeader).findViewById(R.id.txtCountDistanceAndTime);
		String string[] = getSections()[getSectionForPosition(position)]
				.split(Constants.REGULAR_EXPRESSION_LOG);
		if (string != null && string.length > 0)
			lSectionTitle.setText(string[0]);
		if (string != null && string.length > 1) {

			countDistance.setText(string[1]);
		}
		 header.setBackgroundColor(alpha << this.alpha | (backgroundColor));

	}

	@Override
	public int getPositionForSection(int section) {
		if (section < 0) {
			section = 0;
		}
		if (section >= all.size()) {
			section = all.size() - 1;
		}
		int c = 0;
		for (int i = 0; i < all.size(); i++) {
			if (section == i) {
				return c;
			}
			c += all.get(i).second.size();
		}
		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		int c = 0;
		for (int i = 0; i < all.size(); i++) {
			if (position >= c && position < c + all.get(i).second.size()) {
				return i;
			}
			c += all.get(i).second.size();
		}
		return -1;
	}

	@Override
	public String[] getSections() {
		String[] res = new String[all.size()];
		for (int i = 0; i < all.size(); i++) {
			res[i] = all.get(i).first;
		}
		return res;
	}

}
