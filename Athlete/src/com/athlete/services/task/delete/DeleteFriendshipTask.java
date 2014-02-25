package com.athlete.services.task.delete;

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
 * @author edBaev In either case you are essentially deleting any record of
 *         association between these two users.
 * */
public final class DeleteFriendshipTask extends BaseTask<TaskResult<Boolean>> {

	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private String URL_DELETE = Constants.HOST.API_V + "/friendship/";
	private String urlHost;
	private Activity activity;

	public DeleteFriendshipTask(Activity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apiKey, String id) {
		super(null);
		this.activity = activity;
		this.URL_DELETE = URL_DELETE + id + "/";
		this.mApiKey = apiKey;
		this.urlHost = urlHost;
		this.mUserName = userName;
		this.mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<Boolean> doInBackground(List<NameValuePair>... params) {
		TaskResult<Boolean> result = new TaskResult<Boolean>();
		try {
			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature = Constants.SIGNATURE.DELETE + URL_DELETE + "\n"
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}

			String URL = urlHost + URL_DELETE + "?" + Constants.HOST.PUBLIC_KEY
					+ publicKey + Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date;
			boolean response = HttpUtil.delete(URL, mAcces);
			result.setResult(response);

		} catch (Exception e) {
			result.setResult(false);

		}
		return result;
	}
}
