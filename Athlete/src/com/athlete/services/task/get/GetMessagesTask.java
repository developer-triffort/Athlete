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
public final class GetMessagesTask extends BaseTask<TaskResult<List<Message>>> {

	private String URL_GET = Constants.HOST.API_V + "/message/";

	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseBl baseBl;
	private final String LATER_THAN = "later_than=";
	private final String CONVERSATION = "conversation=";
	private String later_than;
	private String idConversation;
	private Conversation conversation;
	private String urlHost;
	private Activity activity;

	public GetMessagesTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, String later_than, Conversation conversation) {
		super(null);
		mUserName = userName;
		mApikey = apikey;
		mAcces = "ApiKey " + mUserName + ":" + mApikey;
		baseBl = new BaseBl(activity);
		this.later_than = later_than;
		this.conversation = conversation;
		this.idConversation = conversation.getId() + "&";
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<List<Message>> doInBackground(
			List<NameValuePair>... params) {
		StringBuffer earLaterLimit = new StringBuffer();
		if (later_than != null) {
			earLaterLimit.append(LATER_THAN);
			earLaterLimit.append(later_than + "&");
		}

		TaskResult<List<Message>> result = new TaskResult<List<Message>>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature;

			signature = Constants.SIGNATURE.GET + URL_GET + "\n" + CONVERSATION
					+ idConversation + earLaterLimit
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_GET + "?" + CONVERSATION
					+ idConversation + earLaterLimit
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

				result.setResult(parser.getMessages(response, baseBl,
						conversation));
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
