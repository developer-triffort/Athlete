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
public final class GetFeedTask extends BaseTask<TaskResult<List<Feed>>> {

	private String URL_GET = Constants.HOST.API_V2 + "/post/";
	private final String TYPE = "&type=";
	private final String USER_ID = "&user_id=";
	private final String LIMIT = "&limit=";
	private final String EARLIER = "earlier_than=";
	private final String LATER_THAN = "&later_than=";
	private final String FALL_BACK = "&fallback=";
	private String urlHost;
	private String type;
	private String user_id;
	private String limit;
	private String earlier_than;
	private String mUserName;
	private String mApikey;
	private String mAcces;
	private String later_than;
	private int id_current;
	private BaseBl baseBl;
	private int typeFeed;
	private int fallBack;
	private boolean isDelete;
	private Activity activity;

	public GetFeedTask(BaseActivity activity, String urlHost, String publicKey,
			String privateKey, String userName, String apikey, int id_current,
			int typeFeed, boolean isDelete, int fallBack) {
		super(null);
		this.mUserName = userName;
		this.mApikey = apikey;
		this.mAcces = "ApiKey " + mUserName + ":" + mApikey;
		this.id_current = id_current;
		this.typeFeed = typeFeed;
		this.baseBl = new BaseBl(activity);
		this.isDelete = isDelete;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
		this.fallBack = fallBack;
	}

	public GetFeedTask(BaseActivity context, String urlHost, String publicKey,
			String privateKey, String userName, String apikey, String type,
			String user_id, String limit, String earlier_than,
			String later_than, int id_current, int typeFeed, boolean isDelete,
			int fallBack) {
		this(context, urlHost, publicKey, privateKey, userName, apikey,
				id_current, typeFeed, isDelete, fallBack);
		this.type = type;
		this.user_id = user_id;
		this.limit = limit;
		this.earlier_than = earlier_than;
		this.later_than = later_than;
	}

	// earlier
	// fallback
	// later
	// limit

	// public
	// signature
	// time

	// type
	// user_id

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<List<Feed>> doInBackground(
			List<NameValuePair>... params) {
		TaskResult<List<Feed>> result = new TaskResult<List<Feed>>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));

			String signature;
			StringBuffer typeAndId = new StringBuffer();
			StringBuffer earLaterLimit = new StringBuffer();
			if (type != null) {
				typeAndId.append(TYPE);
				typeAndId.append(type);
			}
			if (user_id != null) {
				typeAndId.append(USER_ID);
				typeAndId.append(user_id);
			}

			if (earlier_than != null) {
				earLaterLimit.append(EARLIER);
				earLaterLimit.append(earlier_than);
			}
			if (fallBack == 1) {
				earLaterLimit.append(FALL_BACK);
				earLaterLimit.append(fallBack);
			}

			if (later_than != null) {
				earLaterLimit.append(LATER_THAN);
				earLaterLimit.append(later_than);
			}
			if (limit != null) {
				earLaterLimit.append(LIMIT);
				earLaterLimit.append(limit);
			}
			if (earLaterLimit.length() != 0) {
				earLaterLimit.append("&");
			}
			signature = Constants.SIGNATURE.GET + URL_GET + "\n"
					+ earLaterLimit + Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date)
					+ typeAndId;
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_GET + "?" + earLaterLimit
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date + typeAndId;

			String response = HttpUtil.get(URL, mAcces);

			if (response == null) {
				result.setError(true, response, response);
				return result;
			}
			if (response.equalsIgnoreCase(String.valueOf(HttpUtil.error404))) {
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

				result.setResult(parser.getFeed(response, id_current, baseBl,
						typeFeed, isDelete, fallBack));
				if (fallBack == 1) {
					result.setTypeFeed(parser.getTypeFeed());
				}
				if (result.getResult().size() == 0) {
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
