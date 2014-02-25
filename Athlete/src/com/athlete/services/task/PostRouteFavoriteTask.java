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
public final class PostRouteFavoriteTask extends BaseTask<TaskResult<Boolean>> {
	private static final String IS_FAVORITE = "is_favorite";

	private String URL_POST = Constants.HOST.API_V + "/route/";
	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private String urlHost;
private Activity activity;
	public PostRouteFavoriteTask( Activity activity,String urlHost, String publicKey,
			String privateKey, String userName, String apikey, int objectId) {
		super(null);
		mApiKey = apikey;
		mUserName = userName;
		mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		URL_POST += String.valueOf(objectId) + "/toggle_favorite/";
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity=activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<Boolean> doInBackground(List<NameValuePair>... params) {
		TaskResult<Boolean> result = new TaskResult<Boolean>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature = Constants.SIGNATURE.POST + URL_POST + "\n"
					+ Constants.HOST.PUBLIC_KEY +publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature,
					privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}

			String URL = urlHost + URL_POST + "?" + Constants.HOST.PUBLIC_KEY
					+ publicKey + Constants.HOST.SIGNATURE
					+ signature + Constants.HOST.TIMESTAMP + date;

			String response = HttpUtil.post(URL, null, mAcces, false);
			if (response == null || response.length() == 0) {
				result.setError(true, null, null);
				return result;
			}

			CommonHelper.dumpString(response);

			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.has(IS_FAVORITE) && !jsonObject.isNull(IS_FAVORITE)) {
				result.setResult(jsonObject.getBoolean(IS_FAVORITE));
			} else {
				result.setError(true, null, null);
			}
		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
