package com.athlete.services.task;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.model.Comment;
import com.athlete.model.TaskResult;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class SendCommentTask extends BaseTask<TaskResult<Comment>> {
	private static final String POST_ID = "post_id";
	private static final String COMMENT = "comment";
	private String URL_POST = Constants.HOST.API_V + "/comment/";
	private String mAcces;
	private String mApiKey;
	private String mUserName;
	private int feedID;
	private String commentBody;
	private Comment comment;
	private String urlHost;
	private Activity activity;

	public SendCommentTask(Activity activity, String urlHost, String publicKey,
			String privateKey, String userName, String apikey, Comment comment) {
		super(null);
		mApiKey = apikey;
		mUserName = userName;
		mAcces = "ApiKey " + mUserName + ":" + mApiKey;
		this.feedID = comment.getFeedId();
		this.commentBody = comment.getComment();
		this.comment = comment;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<Comment> doInBackground(List<NameValuePair>... params) {
		TaskResult<Comment> result = new TaskResult<Comment>();
		try {
			JSONObject jsonObjSend = new JSONObject();
			jsonObjSend.put(COMMENT, commentBody);
			jsonObjSend.put(POST_ID, feedID);

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

			String response = HttpUtil.post(URL, jsonObjSend, mAcces, false);
			if (response == null || response.length() == 0) {
				result.setError(true, null, null);
				return result;
			}
			response = CommonHelper.getLastCompanion(response);
			comment.setId(CommonHelper.getLastCompanionInt(response));

			CommonHelper.dumpString(response);

			result.setResult(comment);

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
