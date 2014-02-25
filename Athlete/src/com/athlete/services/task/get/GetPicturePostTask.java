package com.athlete.services.task.get;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;

import com.athlete.Constants;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.model.PostPicture;
import com.athlete.model.TaskResult;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.PictureParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetPicturePostTask extends
		BaseTask<TaskResult<List<PostPicture>>> {

	private String mUserName;
	private String mApikey;
	private String mAcces;
	private BaseOperationsBL baseBl;
	private String URL_GET = Constants.HOST.API_V + "/picture/";
	private String urlHost;
	private String POST_ID = "post_id=";
	private Activity activity;

	public GetPicturePostTask(Activity activity, BaseOperationsBL baseBl,
			String urlHost, String publicKey, String privateKey,
			String userName, String apikey, String feedID) {
		super(null);
		this.mUserName = userName;
		this.mApikey = apikey;
		this.urlHost = urlHost;
		this.mAcces = "ApiKey " + mUserName + ":" + mApikey;
		this.baseBl = baseBl;
		this.POST_ID += feedID;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.activity = activity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<List<PostPicture>> doInBackground(
			List<NameValuePair>... params) {
		StringBuffer earLaterLimit = new StringBuffer();
		earLaterLimit.append(POST_ID);
		earLaterLimit.append("&");
		TaskResult<List<PostPicture>> result = new TaskResult<List<PostPicture>>();
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
				PictureParser pictureParser = new PictureParser();
				result.setResult(pictureParser
						.getPostPictures(response, baseBl));
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
