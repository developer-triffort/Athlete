package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.activity.BaseActivity;
import com.athlete.bl.BaseBl;
import com.athlete.model.Conversation;
import com.athlete.model.TaskResult;
import com.athlete.parser.ConversationParser;
import com.athlete.parser.ErrorParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetConversationOneTask extends
		BaseTask<TaskResult<Conversation>> {

	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseBl baseBl;
	private String URL_GET;
	private String currUserId;
	private String urlHost;
	private Activity activity;

	public GetConversationOneTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, String URL_GET, String currUserId) {
		super(null);
		mUserName = userName;
		this.URL_GET = URL_GET;
		mApikey = apikey;
		mAcces = "ApiKey " + mUserName + ":" + mApikey;
		baseBl = new BaseBl(activity);
		this.activity = activity;
		this.currUserId = currUserId;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<Conversation> doInBackground(
			List<NameValuePair>... params) {

		TaskResult<Conversation> result = new TaskResult<Conversation>();
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
			if (response == null || response.equals(HttpUtil.error404)) {
				result.setError(true, response, response);
				return result;
			}
			if (new JSONObject(response).has(ErrorParser.ERROR)) {
				ErrorParser parser = new ErrorParser();
				response = parser.errorParser(response);
				result.setError(true, response, response);
				return result;
			} else {
				ConversationParser parser = new ConversationParser();
				result.setResult(parser.getOneConversation(new JSONObject(
						response), baseBl, currUserId));
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
