package com.athlete.services.task;

import java.io.File;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.model.TaskResult;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class SendPicturePostTask extends BaseTask<TaskResult<Boolean>> {

	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private File file;
	private String POST_ID = "post_id=";
	private String URL_POST = Constants.HOST.API_V + "/picture/";
	private String urlHost;
	private Activity activity;

	public SendPicturePostTask(Activity activity, String txtProgress,
			File file, String urlHost, String publicKey, String privateKey,
			String apiKey, String userName, String postID) {
		super(txtProgress);
		this.mApiKey = apiKey;
		this.mUserName = userName;
		this.mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.POST_ID += postID + "&";
		this.file = file;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<Boolean> doInBackground(List<NameValuePair>... params) {
		TaskResult<Boolean> result = new TaskResult<Boolean>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature = Constants.SIGNATURE.POST + URL_POST + "\n"
					+ POST_ID + Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_POST + "?" + POST_ID
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date;
			String response = HttpUtil.postPhoto(URL, mAcces, file);

			if (response.equals(String.valueOf(HttpUtil.error201))) {
				result.setResult(true);
				return result;
			} else {
				result.setResult(false);
				return result;
			}

		} catch (Exception e) {
			result.setResult(false);

		}
		return result;
	}
}
