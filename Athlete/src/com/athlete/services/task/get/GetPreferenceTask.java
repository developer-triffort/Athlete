package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.model.PreferenceUser;
import com.athlete.model.TaskResult;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.PreferenceParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetPreferenceTask extends
		BaseTask<TaskResult<PreferenceUser>> {

	private String URL_GET = Constants.HOST.API_V + "/preferences/";
	private PreferenceUser preferenceUser;
	private String mUserName;
	private String mApikey;
	private String mAcces;
	private String urlHost;
	private Activity activity;

	public GetPreferenceTask(Activity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, PreferenceUser preferenceUser) {
		super(null);
		this.mUserName = userName;
		this.mApikey = apikey;
		this.mAcces = "ApiKey " + mUserName + ":" + mApikey;
		this.URL_GET += (preferenceUser.getId() + "/");
		this.preferenceUser = preferenceUser;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<PreferenceUser> doInBackground(
			List<NameValuePair>... params) {
		TaskResult<PreferenceUser> result = new TaskResult<PreferenceUser>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature;

			signature = Constants.SIGNATURE.GET + URL_GET + "\n"
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_GET + "?" + Constants.HOST.PUBLIC_KEY
					+ publicKey + Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date;

			String response = HttpUtil.get(URL, mAcces);

			CommonHelper.dumpString(response);
			if (response == null) {
				result.setError(true, response, response);
				return result;
			}
			if (new JSONObject(response).has(ErrorParser.ERROR)) {
				ErrorParser parser = new ErrorParser();
				response = parser.errorParser(response);
				result.setError(true, response, response);
				return result;
			} else {
				PreferenceParser parser = new PreferenceParser();

				result.setResult(parser.getPreferenceParser(response,
						preferenceUser));
				if (result.getResult() == null) {
					result.setError(true, response, response);
				}

				return result;

			}

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
