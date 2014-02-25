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
import com.athlete.model.Stats;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.StatsParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetStatsTask extends BaseTask<TaskResult<Stats>> {
	// /api/v1/stats/307/
	private String URL_GET;

	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseBl baseBl;
	private User userDetails;
	private String urlHost;
	private Activity activity;

	public GetStatsTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String userName, String apikey) {
		super(null);
		mUserName = userName;
		mApikey = apikey;
		mAcces = "ApiKey " + mUserName + ":" + mApikey;
		baseBl = new BaseBl(activity);
		this.activity = activity;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public GetStatsTask(BaseActivity context, String urlHost, String publicKey,
			String privateKey, String userName, String apikey, User userDetails) {
		this(context, urlHost, publicKey, privateKey, userName, apikey);
		this.userDetails = userDetails;
		URL_GET = userDetails.getStats();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<Stats> doInBackground(List<NameValuePair>... params) {
		TaskResult<Stats> result = new TaskResult<Stats>();
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
				StatsParser parser = new StatsParser();

				result.setResult(parser.getStats(response));
				if (result.getResult() == null) {
					result.setError(true, response, response);
				} else {
					result.getResult().setUser(userDetails);
					baseBl.createOrUpdate(Stats.class, result.getResult());

				}
				return result;

			}

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
