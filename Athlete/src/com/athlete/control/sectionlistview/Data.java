package com.athlete.control.sectionlistview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import android.util.Pair;

import com.athlete.model.User;

public class Data {
	public static final String TAG = Data.class.getSimpleName();
	public static List<Pair<String, List<User>>> res;
	private static LinkedHashSet<String> firstChar;
	private static List<String> strs;

	public static List<Pair<String, List<User>>> getAllData(List<User> friends) {
		firstChar = new LinkedHashSet<String>();
		for (User user : friends)
			firstChar.add(user.getFirstName().substring(0, 1).toUpperCase());
		strs = new LinkedList<String>();
		strs.addAll(firstChar);

		Collections.sort(strs, new Comparator<String>() {
			@Override
			public int compare(String paramT1, String paramT2) {
				return paramT1.compareToIgnoreCase(paramT2);
			}
		});
		Collections.sort(friends, new Comparator<User>() {
			@Override
			public int compare(User paramT1, User paramT2) {

				return paramT1.getFirstName().compareToIgnoreCase(paramT2.getFirstName());
			}
		});

		res = new ArrayList<Pair<String, List<User>>>();

		getOneSection(friends);

		return res;
	}


	public static void getOneSection(List<User> friends) {
		for (String str : strs) {
			List<User> temp = new LinkedList<User>();
			for (User user : friends) {
				if (user.getFirstName().toUpperCase()
						.startsWith(str)) {
					temp.add(user);
				}
			}
			res.add(new Pair<String, List<User>>(str, temp));
		}

	}
}
