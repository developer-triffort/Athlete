package com.athlete.control.sectionlistview;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Pair;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.model.User;

public class FriendInviteData {
	public static final String TAG = FriendInviteData.class.getSimpleName();
	public static List<Pair<String, List<User>>> res;
	private static LinkedHashSet<String> header;
	private static List<String> strs;
	private static int countFB = 0;
	private static Context ctx;

	public static List<Pair<String, List<User>>> getAllData(Context context,
			List<User> friends) {
		countFB = 0;
		ctx = context;
		header = new LinkedHashSet<String>();
		header.add(ctx.getString(R.string.header_friend_invites));
		header.add(ctx.getString(R.string.header_invite_friends));
		for (User user : friends) {
			if (user.getId().startsWith(Constants.FB_TAG)) {
				countFB++;
			}
		}

		if (countFB > 0) {
			StringBuffer headerBuffer = new StringBuffer();
			headerBuffer.append(countFB + " ");
			if (countFB == 1) {
				headerBuffer
						.append(ctx.getString(R.string.friend_like_running));
			} else {
				headerBuffer.append(ctx
						.getString(R.string.friends_like_running));
			}
			header.add(headerBuffer.toString());
		}

		strs = new LinkedList<String>();
		strs.addAll(header);

		res = new ArrayList<Pair<String, List<User>>>();

		getOneSection(friends);

		return res;
	}

	public static void getOneSection(List<User> friends) {
		for (int i = 0; i < strs.size(); i++) {
			List<User> temp = new LinkedList<User>();

			for (User user : friends) {
				if (countFB > 0) {
					if (user.getId().startsWith(Constants.FB_TAG)
							&& !strs.get(i)
									.startsWith(
											ctx.getString(R.string.header_friend_invites))
							&& !strs.get(i)
									.startsWith(
											ctx.getString(R.string.header_invite_friends))) {
						temp.add(user);
					}
					if (!user.getId().startsWith(Constants.FB_TAG)
							&& user.getId().length() > 0
							&& strs.get(i)
									.startsWith(
											ctx.getString(R.string.header_friend_invites))) {
						temp.add(user);
					}
					if (user.getId().length() == 0
							&& strs.get(i)
									.equals(ctx
											.getString(R.string.header_invite_friends))) {
						temp.add(user);
					}
				} else {
					if (!user.getId().startsWith(Constants.FB_TAG)
							&& user.getId().length() > 0
							&& strs.get(i)
									.startsWith(
											ctx.getString(R.string.header_friend_invites))) {
						temp.add(user);
					}
					if (user.getId().length() == 0
							&& strs.get(i)
									.equals(ctx
											.getString(R.string.header_invite_friends))) {
						temp.add(user);
					}

				}

			}
			res.add(new Pair<String, List<User>>(strs.get(i), temp));
		}

	}
}
