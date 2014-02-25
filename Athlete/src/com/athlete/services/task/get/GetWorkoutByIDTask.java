package com.athlete.services.task.get;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.NameValuePair;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.athlete.google.android.apps.mytracks.io.file.GpxImporter;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.model.TaskResult;
import com.athlete.model.WorkOut;
import com.athlete.parser.ErrorParser;
import com.athlete.parser.WorkOutParser;
import com.athlete.services.BaseTask;
import com.athlete.util.CommonHelper;
import com.athlete.util.HttpUtil;

/**
 * @author edBaev
 * */
public final class GetWorkoutByIDTask extends BaseTask<TaskResult<WorkOut>> {

	private String URL_GET = Constants.HOST.API_V2 + "/workout/";

	private String mUserName;
	private String mApikey;
	private String mAcces;
	private boolean isGPX;
	private Activity activity;
	private String urlHost;

	public GetWorkoutByIDTask(Activity activity, String urlHost,
			String publicKey, String privateKey, String userName,
			String apikey, int workoutID, boolean isGPX) {
		super(null);
		this.mUserName = userName;
		this.mApikey = apikey;
		this.mAcces = "ApiKey " + mUserName + ":" + mApikey;
		this.URL_GET += workoutID + "/";
		this.isGPX = isGPX;
		this.urlHost = urlHost;
		this.activity = activity;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected TaskResult<WorkOut> doInBackground(List<NameValuePair>... params) {
		TaskResult<WorkOut> result = new TaskResult<WorkOut>();
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

			String response;
			if (isGPX) {
				response = HttpUtil.getGPX(URL, mAcces);
			} else {
				response = HttpUtil.get(URL, mAcces);
			}

			if (response == null) {
				result.setError(true, response, response);
				return result;
			}
			CommonHelper.dumpString(response);

			if (!isGPX) {
				if (new JSONObject(response).has(ErrorParser.ERROR)) {
					ErrorParser parser = new ErrorParser();
					response = parser.errorParser(response);
					result.setError(true, response, response);
					return result;
				} else {
					WorkOutParser parser = new WorkOutParser();

					result.setResult(parser
							.getWorkout(new JSONObject(response)));
					if (result.getResult() == null) {
						result.setError(true, response, response);
					}
					return result;

				}
			} else {
				long trackID = importGPXFile(response);
				WorkOut out = new WorkOut();
				out.setTrackID(trackID);
				result.setResult(out);
				return result;
			}

		} catch (Exception e) {
			result.setError(true, e.getClass().getSimpleName(), e.getMessage());

		}
		return result;
	}

	public long importGPXFile(String xml) throws ParserConfigurationException,
			SAXException, IOException {
		File file = write("track.gpx", xml);
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();

		GpxImporter gpxImporter = new GpxImporter(
				MyTracksProviderUtils.Factory.get(activity),
				PreferencesUtils.getInt(activity,
						R.string.min_recording_distance_key, 2));

		long[] trackIds = new long[0];

		try {
			long start = System.currentTimeMillis();
			saxParser.parse(new FileInputStream(file), gpxImporter);

			long end = System.currentTimeMillis();
			Log.d(Constants.TAG, "Total import time: " + (end - start) + "ms");

			trackIds = gpxImporter.getImportedTrackIds();
			if (trackIds.length == 0) {
				throw new IOException("No track imported.");
			}
		} finally {
			// Delete the current track if not finished
			gpxImporter.rollbackUnfinishedTracks();
		}
		if (trackIds.length == 1) {
			Log.d(Constants.TAG, String.valueOf(trackIds[0]));
		}

		return trackIds[trackIds.length - 1];
	}

	public File write(String fileName, String data) {
		Writer writer;
		File root = Environment.getExternalStorageDirectory();
		File outDir = new File(root.getAbsolutePath() + File.separator
				+ "EZ_time_tracker");
		if (!outDir.isDirectory()) {
			outDir.mkdir();
		}
		try {
			if (!outDir.isDirectory()) {
				throw new IOException(
						"Unable to create directory EZ_time_tracker. Maybe the SD card is mounted?");
			}
			File outputFile = new File(outDir, fileName);
			writer = new BufferedWriter(new FileWriter(outputFile));
			writer.write(data);

			writer.close();

		} catch (IOException e) {
			Log.w("eztt", e.getMessage(), e);

		}
		return new File(outDir + "/" + fileName);

	}

}
