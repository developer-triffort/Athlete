package com.athlete.services.task;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.activity.BaseActivity;
import com.athlete.model.TaskResult;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class SendWorkoutJSONTask extends BaseTask<TaskResult<String>> {
	private String URL_POST = Constants.HOST.API_V2 + "/workout/";
	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private JSONObject jsonObjSend;
	private final String RUN_DATE = "run_date";
	private String urlHost;
	private Activity activity;

	public SendWorkoutJSONTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, JSONObject jsondata) {
		super(null);
		mApiKey = apikey;
		mUserName = userName;
		mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		jsonObjSend = jsondata;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.activity = activity;
		this.privateKey = privateKey;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<String> doInBackground(List<NameValuePair>... params) {
		TaskResult<String> result = new TaskResult<String>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));

			String signature = Constants.SIGNATURE.POST + URL_POST + "\n"
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}

			String URL = urlHost + URL_POST + "?" + Constants.HOST.PUBLIC_KEY
					+ publicKey + Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date;

			String response = HttpUtil.postJSONTrack(URL, jsonObjSend, mAcces);
			if (response == null) {
				result.setError(true, "", "");
				return result;
			}

			CommonHelper.dumpString(response);

			result.setResult(response);

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
