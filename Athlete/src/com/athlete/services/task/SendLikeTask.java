package com.athlete.services.task;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.model.TaskResult;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class SendLikeTask extends BaseTask<TaskResult<String>> {
	private static final String OBJECT_ID = "object_id";
	private static final String OBJECT_TYPE = "object_type";
	private static final String LIKE_COUNT = "like_count";

	private String URL_POST = Constants.HOST.API_V + "/like/toggle/";
	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private int objectId;
	private String objectType;
	private String urlHost;
	private Activity activity;

	public SendLikeTask(Activity activity, String urlHost, String publicKey,
			String privateKey, String userName, String apikey, int objectId,
			String objectType) {
		super(null);
		mApiKey = apikey;
		mUserName = userName;
		mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.objectId = objectId;
		this.objectType = objectType;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<String> doInBackground(List<NameValuePair>... params) {
		TaskResult<String> result = new TaskResult<String>();
		try {
			JSONObject jsonObjSend = new JSONObject();
			jsonObjSend.put(OBJECT_TYPE, objectType);
			jsonObjSend.put(OBJECT_ID, objectId);

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature = Constants.SIGNATURE.POST + URL_POST + "\n"
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A"))
				signature = signature.substring(0, signature.length() - 3);

			String URL = urlHost + URL_POST + "?" + Constants.HOST.PUBLIC_KEY
					+ publicKey + Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date;

			String response = HttpUtil.post(URL, jsonObjSend, mAcces, false);
			if (response.equals(String.valueOf(HttpUtil.error404))) {
				result.setError(true, String.valueOf(HttpUtil.error404),
						String.valueOf(HttpUtil.error404));
				return result;
			}
			if (response == null || response.length() == 0) {
				result.setError(true, null, null);
				return result;
			}

			CommonHelper.dumpString(response);
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.has(LIKE_COUNT) && !jsonObject.isNull(LIKE_COUNT)) {
				result.setResult(jsonObject.getString(LIKE_COUNT));
			} else {
				result.setError(true, null, null);
			}
		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
