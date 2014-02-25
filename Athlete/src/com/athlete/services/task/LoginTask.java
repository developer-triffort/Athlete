package com.athlete.services.task;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.exception.AuthException;
import com.athlete.exception.BaseException;
import com.athlete.model.TaskResult;
import com.athlete.parser.BaseParser;
import com.athlete.parser.ErrorParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class LoginTask extends BaseTask<TaskResult<String[]>> {

	private String mEmail;
	private String mPassword;
	private final String EMAIL = "email";
	private final String PASSWORD = "password";
	private String URL_POST = Constants.HOST.API_V + "/account/login/";
	private String urlHost;
	private Activity activity;

	public LoginTask(BaseActivity activity, String urlHost, String publicKey,
			String privateKey, String email, String password) {
		super(activity.getString(R.string.lbl_please_wait));
		this.mEmail = email;
		this.mPassword = password;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<String[]> doInBackground(List<NameValuePair>... params) {
		TaskResult<String[]> result = new TaskResult<String[]>();
		try {
			JSONObject jsonObjSend = new JSONObject();
			jsonObjSend.put(EMAIL, mEmail);
			jsonObjSend.put(PASSWORD, mPassword);
			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature = Constants.SIGNATURE.POST + URL_POST + "\n"
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			try {
				String URL = urlHost + URL_POST + "?"
						+ Constants.HOST.PUBLIC_KEY + publicKey
						+ Constants.HOST.SIGNATURE + signature
						+ Constants.HOST.TIMESTAMP + date;

				String response = HttpUtil.post(URL, jsonObjSend, null, false);

				CommonHelper.dumpString(response);

				if (new JSONObject(response).has(ErrorParser.ERROR)) {
					ErrorParser parser = new ErrorParser();
					response = parser.errorParser(response);
					result.setError(true, response, response);
					return result;
				} else {
					BaseParser parser = new BaseParser(response, null);
					result.setResult(parser.getUser());
					return result;
				}
			} catch (BaseException e) {

			}

		} catch (AuthException e) {
			result.setError(true, e.getError(), e.getError_description());

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
