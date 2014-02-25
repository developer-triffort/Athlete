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
public final class PatchReadConversationTask extends
		BaseTask<TaskResult<Boolean>> {

	private final String LAST_MESSAGE_ID = "last_message_id=";
	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private int messageID;
	private String URL_POST = Constants.HOST.API_V + "/conversation/";
	private String urlHost;
	private Activity activity;

	public PatchReadConversationTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String apiKey,
			String userName, int id, int messageID) {
		super(null);
		this.URL_POST = URL_POST + id + "/read/";
		this.messageID = messageID;
		this.mApiKey = apiKey;
		this.mUserName = userName;
		this.mAcces = "ApiKey " + mUserName + ":" + mApiKey;
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
					+ LAST_MESSAGE_ID + messageID + "&"
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}

			String URL = urlHost + URL_POST + "?" + LAST_MESSAGE_ID + messageID
					+ "&" + Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date;

			String response = HttpUtil.post(URL, null, mAcces, true);

			if (response.equals(String.valueOf(HttpUtil.error200))) {
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