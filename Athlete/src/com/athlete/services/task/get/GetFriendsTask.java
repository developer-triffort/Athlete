package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;

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
public final class GetFriendsTask extends BaseTask<TaskResult<ArrayList<User>>> {

	private String URL_GET = Constants.HOST.API_V + "/friendship/user/";

	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseOperationsBL baseBl;
	private String limit;
	private User userDetails;
	private String urlHost;
	private Activity activity;
	private Handler handler = new Handler();

	public GetFriendsTask(Activity activity, BaseOperationsBL baseBl,
			String urlHost, String publicKey, String privateKey,
			String userName, String apikey, String limit, String str) {
		super(str);
		this.mUserName = userName;
		this.mApikey = apikey;
		this.mAcces = "ApiKey " + mUserName + ":" + mApikey;
		this.baseBl = baseBl;
		this.urlHost = urlHost;
		this.limit = limit;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	public GetFriendsTask(Activity activity, BaseOperationsBL baseBl,
			String urlHost, String publicKey, String privateKey,
			String userName, String apikey, User userDetails, String limit,
			String str) {
		this(activity, baseBl, urlHost, publicKey, privateKey, userName,
				apikey, limit, str);
		this.userDetails = userDetails;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<ArrayList<User>> doInBackground(
			List<NameValuePair>... params) {
		TaskResult<ArrayList<User>> result = new TaskResult<ArrayList<User>>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));

			String signature;
			StringBuffer limitBuf = new StringBuffer();
			if (limit != null) {
				limitBuf.append("limit=" + limit + "&");
			}
			signature = Constants.SIGNATURE.GET + URL_GET + userDetails.getId()
					+ "/\n" + limitBuf + Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A")) {
				signature = signature.substring(0, signature.length() - 3);
			}
			String URL = urlHost + URL_GET + userDetails.getId() + "/?"
					+ limitBuf + Constants.HOST.PUBLIC_KEY + publicKey
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
				FriendParser parser = new FriendParser();

				result.setResult(parser.getFriends(response, baseBl,
						userDetails.getId()));

				int count = parser.getTotalFriend();
				ArrayList<Integer> requesters = parser.getRequester();
				ArrayList<String> status = parser.getStatus();
				ArrayList<String> friendShipId = parser.getFriendShipId();
				List<FriendsM2M> friendsM2Ms = baseBl.getListFromDBByField(
						FriendsM2M.class, FriendsM2M.USER, userDetails.getId());
				if (count <= 20 && count != 0 && friendsM2Ms != null
						&& friendsM2Ms.size() != count) {
					baseBl.deleteList(friendsM2Ms, FriendsM2M.class);
				}
				if (result.getResult().size() == 0) {
					baseBl.deleteList(friendsM2Ms, FriendsM2M.class);
					result.setError(true, response, response);
				} else {
					for (int i = 0; i < result.getResult().size(); i++) {
						FriendsM2M friendsM2M = new FriendsM2M(userDetails,
								result.getResult().get(i), count,
								requesters.get(i), status.get(i),
								friendShipId.get(i));
						baseBl.createOrUpdate(FriendsM2M.class, friendsM2M);
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
