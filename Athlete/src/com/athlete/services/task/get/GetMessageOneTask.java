package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.model.Message;
import com.athlete.model.TaskResult;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.MessageParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetMessageOneTask extends BaseTask<TaskResult<Message>> {

	private String mUserName;
	private String mApikey;
	private String mAcces;
	private String URL_GET;
	private String urlHost;
	private Activity activity;

	public GetMessageOneTask(Activity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, String URL_GET) {
		super(null);
		this.mUserName = userName;
		this.URL_GET = URL_GET;
		this.mApikey = apikey;

		this.mAcces = "ApiKey " + mUserName + ":" + mApikey;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.urlHost = urlHost;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<Message> doInBackground(List<NameValuePair>... params) {
		StringBuffer earLaterLimit = new StringBuffer();

		TaskResult<Message> result = new TaskResult<Message>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature;

			signature = Constants.SIGNATURE.GET + URL_GET + "\n"
					+ earLaterLimit + Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_GET + "?" + earLaterLimit
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.SIGNATURE + signature
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
				MessageParser parser = new MessageParser();

				result.setResult(parser.getMessage(new JSONObject(response)));
				if (result.getResult() == null)
					result.setError(true, response, response);

				return result;

			}

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
