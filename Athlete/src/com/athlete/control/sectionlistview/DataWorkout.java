package com.athlete.control.sectionlistview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Pair;

import com.athlete.Constants;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.model.WorkOut;
import com.athlete.util.CommonHelper;

public class DataWorkout {
	public static final String TAG = DataWorkout.class.getSimpleName();
	public static List<Pair<String, List<WorkOut>>> res;
	private static LinkedHashSet<String> data;
	private static List<String> strs;
	private static Context context;

	public static List<Pair<String, List<WorkOut>>> getAllData(
			List<WorkOut> workOuts, Context ctx) {
		data = new LinkedHashSet<String>();
		for (WorkOut workOut : workOuts)
			data.add(CommonHelper.getDateFormatMMMMyyyy(workOut.getRunDate()));
		strs = new LinkedList<String>();
		strs.addAll(data);
		context = ctx;
		Collections.sort(strs, new Comparator<String>() {
			@Override
			public int compare(String paramT1, String paramT2) {

				if (CommonHelper.getLongMMMMyyyy(paramT1) > CommonHelper
						.getLongMMMMyyyy(paramT2)) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		Collections.sort(workOuts, new Comparator<WorkOut>() {

			@Override
			public int compare(WorkOut paramT1, WorkOut paramT2) {
				if (CommonHelper.getLongYYYYMMDDtHHMMSS(paramT1.getRunDate()) > CommonHelper
						.getLongYYYYMMDDtHHMMSS(paramT2.getRunDate())) {
					return -1;
				} else {
					return 1;
				}
			}
		});

		res = new ArrayList<Pair<String, List<WorkOut>>>();

		getOneSection(workOuts);

		return res;
	}

	public static void getOneSection(List<WorkOut> workOuts) {
		for (String str : strs) {
			double totalDistance = 0;
			long totalMovingTime = 0;
			List<WorkOut> temp = new LinkedList<WorkOut>();
			for (WorkOut workOut : workOuts) {

				if (CommonHelper.getDateFormatMMMMyyyy(workOut.getRunDate())
						.equals(str)) {
					temp.add(workOut);
					totalDistance += workOut.getDistance();
					totalMovingTime += workOut.getDuration();
				}

			}
			String string = CommonHelper.convertMetersToMiles(totalDistance,
					PreferencesUtils.getMetricUnit(context))
					+ CommonHelper.getMiOrKm(PreferencesUtils
							.getMetricUnit(context))
					+ CommonHelper.getDateMSS(totalMovingTime, context);
			res.add(new Pair<String, List<WorkOut>>(str
					+ Constants.REGULAR_EXPRESSION_LOG + string, temp));
		}

	}
}
