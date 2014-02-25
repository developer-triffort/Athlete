package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.activity.BaseActivity;
import com.athlete.bl.BaseBl;
import com.athlete.model.TaskResult;
import com.athlete.model.WorkOut;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.WorkOutParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetWorkoutTask extends BaseTask<TaskResult<List<WorkOut>>> {

	private String URL_GET = Constants.HOST.API_V2 + "/workout/";
	private final String OFFSET = "offset=";
	private final String USER_ID = "&user=";

	private String user_id;
	private int offset;
	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseBl baseBl;
	private String urlHost;
	private Activity activity;
	private boolean isLog;

	public GetWorkoutTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String userName, String apikey) {
		super(null);
		mUserName = userName;
		mApikey = apikey;
		mAcces = "ApiKey " + mUserName + ":" + mApikey;
		baseBl = new BaseBl(activity);
		this.activity = activity;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public GetWorkoutTask(BaseActivity context, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, String user_id, int offset, boolean isLog) {
		this(context, urlHost, publicKey, privateKey, userName, apikey);
		this.offset = offset;
		this.user_id = user_id;
		this.isLog = isLog;
	}

	// offset

	// public
	// signature
	// time

	// user

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<List<WorkOut>> doInBackground(
			List<NameValuePair>... params) {
		TaskResult<List<WorkOut>> result = new TaskResult<List<WorkOut>>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature;
			StringBuffer typeAndId = new StringBuffer();
			StringBuffer earLaterLimit = new StringBuffer();

			if (user_id != null) {
				typeAndId.append(USER_ID);
				typeAndId.append(user_id);
			}

			if (offset != 0) {
				earLaterLimit.append(OFFSET);
				earLaterLimit.append(offset);
			} else {
				if (isLog) {
					baseBl.deleteByField(WorkOut.USER, WorkOut.class, user_id);
				}
			}

			if (earLaterLimit.length() != 0) {
				earLaterLimit.append("&");
			}
			signature = Constants.SIGNATURE.GET + URL_GET + "\n"
					+ earLaterLimit + Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date)
					+ typeAndId;
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_GET + "?" + earLaterLimit
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date + typeAndId;

			String response = HttpUtil.get(URL, mAcces);
			if (response == null) {
				result.setError(true, response, response);
				return result;
			}

			CommonHelper.dumpString(response);

			if (new JSONObject(response).has(ErrorParser.ERROR)) {
				ErrorParser parser = new ErrorParser();
				response = parser.errorParser(response);
				result.setError(true, response, response);
				return result;
			} else {
				WorkOutParser parser = new WorkOutParser();

				result.setResult(parser.getListWorkout(response, baseBl));
				if (result.getResult().isEmpty()) {
					result.setError(true, response, response);
				}
				return result;

			}

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
