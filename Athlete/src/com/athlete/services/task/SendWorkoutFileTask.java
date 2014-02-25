package com.athlete.services.task;

import java.io.File;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;

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
public final class SendWorkoutFileTask extends BaseTask<TaskResult<String>> {

	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private File file;
	private String urlHost;
	private String URL_POST = Constants.HOST.API_V2 + "/workout/";
	private Activity activity;

	public SendWorkoutFileTask(BaseActivity activity, File file,
			String urlHost, String publicKey, String privateKey, String apiKey,
			String userName) {
		super(null);
		mApiKey = apiKey;
		mUserName = userName;
		mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.file = file;
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
			String response = HttpUtil.postWorkout(URL, mAcces, file);
			if (response == null) {
				result.setError(true, null, null);
			} else {
				result.setResult(response);
			}
			return result;
		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
