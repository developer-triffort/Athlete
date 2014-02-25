package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.bl.BaseBl;
import com.athlete.exception.AuthException;
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
public final class GetMeTask extends BaseTask<TaskResult<User>> {

	private String URL_GET = Constants.HOST.API_V + "/me/";
	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private Activity activity;
	private BaseBl baseBl;
	private String urlHost;
	private Handler handler = new Handler();

	public GetMeTask(BaseActivity activity, String urlHost, String publicKey,
			String privateKey, String userName, String apikey, BaseBl baseBl) {
		super(activity.getString(R.string.lbl_please_wait));
		mApiKey = apikey;
		mUserName = userName;
		mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.activity = activity;
		this.baseBl = baseBl;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<User> doInBackground(List<NameValuePair>... params) {
		TaskResult<User> result = new TaskResult<User>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));

			String signature = Constants.SIGNATURE.GET + URL_GET + "\n"
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
			if (response == null) {
				response = activity.getResources().getString(
						R.string.error_timeout);
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
				result.setResult(parser.getUserDetails(response, baseBl));

				return result;
			}

		} catch (AuthException e) {
			result.setError(true, e.getError(), e.getError_description());

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
