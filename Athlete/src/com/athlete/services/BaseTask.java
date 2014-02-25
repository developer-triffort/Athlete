package com.athlete.services;

import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;

import android.app.Activity;
import android.os.AsyncTask;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.util.SntpClient;

/**
 * @author edBaev
 * */
public abstract class BaseTask<Result> extends
		AsyncTask<List<NameValuePair>, String, Result> {

	private Result mResult;
	private String mProgressMessage;
	private final long time2013 = 1360343000000L;
	private IProgressTracker mProgressTracker;
	private boolean isProgress;
	protected String publicKey;
	protected String privateKey;

	public BaseTask(String mProgressMessage) {

		this.mProgressMessage = mProgressMessage;
		if (mProgressMessage != null) {
			isProgress = true;
		}
	}

	protected BaseTask() {
	}

	public long getTime(final Activity activity) {
		Date date = ((AthleteApplication) activity.getApplication()).getDate();
		if (date == null) {

			SntpClient client = new SntpClient();
			for (String str : Constants.HOSTS_TIME) {
				if (client.requestTime(str)) {
					break;
				}
			}
			Date date2 = new Date(client.getNtpTime());
			if (date2 != null && client.getNtpTime() > time2013) {
				((AthleteApplication) activity.getApplication()).setDate(date2);
				return client.getNtpTime();
			} else {
				((AthleteApplication) activity.getApplication())
						.setDate(new Date());
				return new Date().getTime();
			}

		}
		long time = ((AthleteApplication) activity.getApplication()).getTime();

		return time;
	}

	public String getmProgressMessage() {
		return mProgressMessage;
	}

	public void setProgressTracker(IProgressTracker mProgressTracker) {
		this.mProgressTracker = mProgressTracker;
		if (this.mProgressTracker != null) {
			this.mProgressTracker.onProgress();
			if (this.mResult != null) {
				this.mProgressTracker.onComplete(this);
			}
		}
	}

	public boolean isProgressDialogNeeded() {
		return isProgress;
	}

	protected IProgressTracker getProgressTracker() {
		return this.mProgressTracker;
	}

	/* UI Thread */
	@Override
	protected void onCancelled() {
		this.mProgressTracker = null;
	}

	/* UI Thread */
	@Override
	protected void onProgressUpdate(String... values) {
		this.mProgressMessage = values[0];
		if (this.mProgressTracker != null) {
			this.mProgressTracker.onProgress();
		}
	}

	/* UI Thread */
	@Override
	protected void onPostExecute(Result result) {
		this.mResult = result;
		if (this.mProgressTracker != null) {
			this.mProgressTracker.onComplete(this);
		}
		this.mProgressTracker = null;
	}
}
