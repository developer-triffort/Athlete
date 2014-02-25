package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;

import com.athlete.Constants;
import com.athlete.activity.BaseActivity;
import com.athlete.bl.BaseBl;
import com.athlete.model.ProfileUser;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.ProfileParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetProfileTask extends BaseTask<TaskResult<ProfileUser>> {

	private String URL_GET = Constants.HOST.API_V + "/profile/";
	private User user;
	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseBl baseBl;
	private String urlHost;
	private Activity activity;
	private Handler handler = new Handler();

	public GetProfileTask(BaseActivity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, User user, String str) {
		super(str);
		this.mUserName = userName;
		this.mApikey = apikey;
		this.mAcces = "ApiKey " + mUserName + ":" + mApikey;
		this.baseBl = new BaseBl(activity);
		this.URL_GET += (user.getPreferenceID() + "/");
		this.user = user;
		this.urlHost = urlHost;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<ProfileUser> doInBackground(
			List<NameValuePair>... params) {
		TaskResult<ProfileUser> result = new TaskResult<ProfileUser>();
		try {

			String date = CommonHelper.getDateFormatYYYYMMDDtHHMMSS(new Date(
					getTime(activity)));

			String signature;

			signature = Constants.SIGNATURE.GET + URL_GET + "\n"
					+ Constants.HOST.PUBLIC_KEY + publicKey
					+ Constants.HOST.TIMESTAMP + URLEncoder.encode(date);
			signature = CommonHelper.computeHmac(signature, privateKey);
			if (signature.endsWith("%0A"))
				signature = signature.substring(0, signature.length() - 3);
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
				ProfileParser parser = new ProfileParser();
				ProfileUser profileUser = new ProfileUser();
				parser.getProfileUser(response, profileUser);
				result.setResult(profileUser);
				if (result.getResult() == null) {
					result.setError(true, response, response);
				} else {
					profileUser.setUser(user);
					baseBl.createOrUpdate(ProfileUser.class, profileUser);
				}
				return result;

			}

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}
}
