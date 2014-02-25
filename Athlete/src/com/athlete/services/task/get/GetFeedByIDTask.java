package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.model.Feed;
import com.athlete.model.TaskResult;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.FeedParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetFeedByIDTask extends BaseTask<TaskResult<Feed>> {

	private String URL_GET = Constants.HOST.API_V2 + "/post/";

	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseOperationsBL bl;
	private int current_id;
	private int type;
	private String urlHost;
	private Activity activity;

	public GetFeedByIDTask(Activity activity, String urlHost, String publicKey,
			String privateKey, String userName, String apikey, String feedID,
			BaseOperationsBL bl, int current_id, int type) {
		super(null);
		this.mUserName = userName;
		this.mApikey = apikey;
		this.mAcces = "ApiKey " + mUserName + ":" + mApikey;
		this.URL_GET += feedID + "/";
		this.bl = bl;
		this.current_id = current_id;
		this.type = type;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<Feed> doInBackground(List<NameValuePair>... params) {
		TaskResult<Feed> result = new TaskResult<Feed>();
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
			if (response == null) {
				result.setError(true, response, response);
				return result;
			}
			if (response.equals(String.valueOf(HttpUtil.error404))) {
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
				FeedParser parser = new FeedParser();

				result.setResult(parser.getFeed(new JSONObject(response),
						current_id, bl, type));
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
