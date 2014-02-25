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
public final class AcceptFriendRequestTask extends
		BaseTask<TaskResult<Boolean>> {
	private String FRIENDSHIP_ID = "friendship_id";
	private String URL_POST = Constants.HOST.API_V + "/friendship/accept/";
	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private String friendShipId;
	private String urlHost;
	private Activity activity;

	public AcceptFriendRequestTask(Activity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, String friendShipId) {
		super(null);
		this.mApiKey = apikey;
		this.mUserName = userName;
		this.mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.friendShipId = friendShipId;
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
			JSONObject jsonObjSend = new JSONObject();
			jsonObjSend.put(FRIENDSHIP_ID, friendShipId);
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
			if (response == null || response.length() == 0) {
				result.setError(true, null, null);
				return result;
			}

			CommonHelper.dumpString(response);

			if (!response.equals(String.valueOf(HttpUtil.error404))) {
				result.setResult(true);
			} else {
				result.setResult(false);
			}
		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
