package com.athlete.services.task;

import java.io.File;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.model.TaskResult;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class SendPhotoTask extends BaseTask<TaskResult<Boolean>> {

	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private File file;
	private String urlHost;
	private String URL_POST = Constants.HOST.API_V + "/user/picture/";
	private Activity activity;

	public SendPhotoTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, File file, String apiKey,
			String userName) {
		super(activity.getString(R.string.lbl_please_wait));
		this.mApiKey = apiKey;
		this.mUserName = userName;
		this.mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.urlHost = urlHost;
		this.file = file;
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
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_POST + "?" + Constants.HOST.PUBLIC_KEY
					+ publicKey + Constants.HOST.SIGNATURE + signature
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
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
