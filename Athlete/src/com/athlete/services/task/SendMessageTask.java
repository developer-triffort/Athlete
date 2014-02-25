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
public final class SendMessageTask extends BaseTask<TaskResult<String>> {
	private static final String CONVERSATION_ID = "conversation_id";
	private static final String MESSAGE = "message";
	private String URL_POST = Constants.HOST.API_V + "/message/";
	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private int conversationID;
	private String message;
	private String urlHost;
	private Activity activity;

	public SendMessageTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, int conversationID, String message) {
		super(null);
		mApiKey = apikey;
		mUserName = userName;
		mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.conversationID = conversationID;
		this.message = message;
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
			jsonObjSend.put(MESSAGE, message);
			jsonObjSend.put(CONVERSATION_ID, conversationID);

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

			String response = HttpUtil.post(URL, jsonObjSend, mAcces, false);
			int i = response.indexOf("/api/");
			response = response.substring(i);

			CommonHelper.dumpString(response);

			result.setResult(response);

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
