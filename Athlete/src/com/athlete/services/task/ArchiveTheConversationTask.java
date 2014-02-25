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
import com.athlete.parser.ErrorParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class ArchiveTheConversationTask extends
		BaseTask<TaskResult<Boolean>> {

	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private String URL_PATCH = Constants.HOST.API_V + "/conversation/";
	private String urlHost;
	private Activity activity;

	public ArchiveTheConversationTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apiKey, int id) {
		super(null);
		this.URL_PATCH = URL_PATCH + id + "/archive/";
		this.mApiKey = apiKey;
		this.mUserName = userName;
		this.mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.activity = activity;
		this.privateKey = privateKey;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<Boolean> doInBackground(List<NameValuePair>... params) {
		TaskResult<Boolean> result = new TaskResult<Boolean>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature = Constants.SIGNATURE.PATCH + URL_PATCH + "\n"
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}

			String URL = urlHost + URL_PATCH + "?" + Constants.HOST.PUBLIC_KEY
					+ publicKey + Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date;

			String response = HttpUtil.patch(URL, null, mAcces);

			if (response.equals(String.valueOf(HttpUtil.error204))) {
				result.setResult(true);
				return result;
			} else {

				CommonHelper.dumpString(response);

				if (new JSONObject(response).has(ErrorParser.ERROR)) {
					ErrorParser parser = new ErrorParser();
					response = parser.errorParser(response);
					result.setError(true, response, response);
					return result;
				}
				result.setResult(false);
				return result;
			}

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
