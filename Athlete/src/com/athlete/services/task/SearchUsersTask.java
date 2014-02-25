package com.athlete.services.task;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.activity.BaseActivity;
import com.athlete.bl.BaseBl;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.UserParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class SearchUsersTask extends BaseTask<TaskResult<List<User>>> {

	private String URL_GET = Constants.HOST.API_V + "/user/search/";

	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseBl baseBl;
	private String q;
	private String currentID;
	private String QUERY = "&q=";
	private String urlHost;
	private Activity activity;

	public SearchUsersTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, String q, String currentID) {
		super(null);
		mUserName = userName;
		mApikey = apikey;
		mAcces = "ApiKey " + mUserName + ":" + mApikey;
		baseBl = new BaseBl(activity);
		this.currentID = currentID;
		this.q = q;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<List<User>> doInBackground(
			List<NameValuePair>... params) {
		TaskResult<List<User>> result = new TaskResult<List<User>>();
		try {
			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature = Constants.SIGNATURE.GET + URL_GET + "\n"
					+ Constants.HOST.PUBLIC_KEY + publicKey + QUERY + q
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_GET + "?" + Constants.HOST.PUBLIC_KEY
					+ publicKey + QUERY + q + Constants.HOST.SIGNATURE
					+ signature + Constants.HOST.TIMESTAMP + date;

			String response = HttpUtil.get(URL, mAcces);
			if (response == null) {
				result.setError(true, response, response);
				return result;
			}

			CommonHelper.dumpString(response);

			if (new JSONObject(response).has(ErrorParser.ERROR)) {
				ErrorParser parser = new ErrorParser();
				response = parser.errorParser(response);
				result.setError(true, response, response);
				return result;
			} else {
				UserParser parser = new UserParser();

				result.setResult(parser.getUserSearch(response, baseBl,
						currentID));

				return result;

			}

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
