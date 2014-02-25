package com.athlete.services.task;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.bl.BaseBl;
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
public final class LoginTaskViaFB extends BaseTask<TaskResult<String[]>> {

	private String token;
	private final String TOKEN = "token";
	private String URL_POST = Constants.HOST.API_V + "/account/facebook/login/";
	private boolean mIsLogin;
	private String urlHost;
	private BaseActivity activity;

	public LoginTaskViaFB(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String token, boolean isLogin,
			BaseBl baseBl) {
		super(activity.getString(R.string.lbl_please_wait));
		this.token = token;
		this.mIsLogin = isLogin;
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
			jsonObjSend.put(TOKEN, token);

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
			try {
				String response = HttpUtil.post(URL, jsonObjSend, null, false);

				CommonHelper.dumpString(response);
				if (response.length() == 0) {

					result.setError(true,
							activity.getString(R.string.toast_non_internet),
							activity.getString(R.string.toast_non_internet));
					return result;
				}
				if (new JSONObject(response).has(ErrorParser.ERROR)) {
					ErrorParser parser = new ErrorParser();
					response = parser.errorParser(response);
					result.setError(true, response, response);
					return result;
				} else {
					if (new JSONObject(response).has(ErrorParser.CREATED)) {
						if (new JSONObject(response)
								.getBoolean(ErrorParser.CREATED) || mIsLogin) {
							BaseParser parser = new BaseParser(response, null);
							result.setResult(parser.getUser());

							return result;
						} else {
							result.setError(true, Constants.ERRORS.FB_EXISTS,
									Constants.ERRORS.FB_EXISTS);
							return result;
						}
					}
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
