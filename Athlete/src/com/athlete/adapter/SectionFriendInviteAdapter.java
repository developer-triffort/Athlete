package com.athlete.adapter;

import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.athlete.R;
import com.athlete.control.sectionlistview.FriendInviteData;
import com.athlete.model.User;
import com.segment.SegmentAdapter;

public class SectionFriendInviteAdapter extends SegmentAdapter {

	private Context ctx;
	private List<Pair<String, List<User>>> all;
	private final int alpha = 24;
	private final int backgroundColor = 0x3a3a3a;
	private final int textColor = 0xababab;

	/**
	 * @author edBaev
	 */
	public SectionFriendInviteAdapter(Context ctx, List<User> friends) {

		this.ctx = ctx;
		all = FriendInviteData.getAllData(ctx, friends);
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
	public User getItem(int position) {
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
			view.findViewById(R.id.header).setVisibility(View.VISIBLE);
			TextView lSectionTitle = (TextView) view.findViewById(R.id.header);
			lSectionTitle
					.setText(getSections()[getSectionForPosition(position)]);
			setDrawable(lSectionTitle);
		} else {
			view.findViewById(R.id.header).setVisibility(View.GONE);
		}
	}

	@Override
	public View getSectionView(int position, View convertView, ViewGroup parent) {
		View res = convertView;
		if (res == null) {
			res = LayoutInflater.from(ctx)
					.inflate(R.layout.item_composer, null);
		}

		return res;
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		TextView lSectionHeader = (TextView) header;
		lSectionHeader.setText(getSections()[getSectionForPosition(position)]);
		lSectionHeader.setBackgroundColor(alpha << this.alpha
				| (backgroundColor));
		lSectionHeader.setTextColor(alpha << this.alpha | (textColor));
		setDrawable(lSectionHeader);
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

	private void setDrawable(TextView txt) {
		if (!txt.getText()
				.toString()
				.equalsIgnoreCase(ctx.getString(R.string.header_friend_invites))
				&& !txt.getText()
						.toString()
						.equalsIgnoreCase(
								ctx.getString(R.string.header_invite_friends))) {
			txt.setCompoundDrawablesWithIntrinsicBounds(null, null, ctx
					.getResources().getDrawable(R.drawable.facebook_small),
					null);
		} else {
			txt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
	}
}
