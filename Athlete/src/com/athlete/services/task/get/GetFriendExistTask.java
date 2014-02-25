package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.model.FriendsM2M;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.FriendParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetFriendExistTask extends BaseTask<TaskResult<User>> {

	private String URL_GET = Constants.HOST.API_V + "/friendship/exists/";
	private String WITH_USER_ID = "&with_user_id=";
	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseOperationsBL baseBl;
	private User currentUser;
	private String urlHost;
	private Activity activity;

	public GetFriendExistTask(Activity activity, BaseOperationsBL baseBl,
			String urlHost, String publicKey, String privateKey,
			String userName, String apikey, User currentUser, String idOtherUser) {
		super(null);
		this.mUserName = userName;
		this.mApikey = apikey;
		this.mAcces = "ApiKey " + mUserName + ":" + mApikey;
		this.baseBl = baseBl;
		this.URL_GET += currentUser.getId() + "/";
		this.WITH_USER_ID += idOtherUser;
		this.urlHost = urlHost;
		this.currentUser = currentUser;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<User> doInBackground(List<NameValuePair>... params) {
		TaskResult<User> result = new TaskResult<User>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));
			String signature;
			signature = Constants.SIGNATURE.GET + URL_GET + "\n"
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date)
					+ WITH_USER_ID;
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_GET + "?" + Constants.HOST.PUBLIC_KEY
					+ publicKey + Constants.HOST.SIGNATURE + signature
					+ Constants.HOST.TIMESTAMP + date + WITH_USER_ID;

			String response = HttpUtil.get(URL, mAcces);

			CommonHelper.dumpString(response);

			if (response == null
					|| response.equals(String.valueOf(HttpUtil.error404))) {
				result.setError(true, response, response);
				return result;
			}
			if (new JSONObject(response).has(ErrorParser.ERROR)) {
				ErrorParser parser = new ErrorParser();
				response = parser.errorParser(response);
				result.setError(true, response, response);
				return result;
			} else {
				FriendParser parser = new FriendParser();

				result.setResult(parser.getFriend(baseBl, currentUser.getId(),
						new JSONObject(response)));

				int count = parser.getTotalFriend();
				ArrayList<Integer> requesters = parser.getRequester();
				ArrayList<String> status = parser.getStatus();
				ArrayList<String> friendShipId = parser.getFriendShipId();

				if (result.getResult() == null) {
					result.setError(true, response, response);
				} else {
					try {

						FriendsM2M friendsM2M = new FriendsM2M(currentUser,
								result.getResult(), count, requesters.get(0),
								status.get(0), friendShipId.get(0));
						baseBl.createOrUpdate(FriendsM2M.class, friendsM2M);
					} catch (Exception e) {
						result.setError(true, response, response);
					}
				}
				return result;

			}

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
