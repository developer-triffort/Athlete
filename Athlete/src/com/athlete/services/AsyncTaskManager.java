package com.athlete.services;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.Window;

import com.athlete.R;

/**
 * @author edBaev
 * */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AsyncTaskManager implements OnCancelListener, IProgressTracker {
	private Dialog mDialog;
	private final Context context;
	private HashMap<BaseTask<Object>, OnTskCpltListener> taskList;
	private boolean isDestroy;
	private boolean isCanCancel;

	public AsyncTaskManager(Context context) {
		this.context = context;
		taskList = new HashMap<BaseTask<Object>, OnTskCpltListener>();
	}

	public synchronized void executeTask(BaseTask asyncTask, OnTskCpltListener onTaskCompleteListener,
			List<NameValuePair> params, boolean isCanCancel) {
		if (isDestroy)
			return;
		this.isCanCancel = isCanCancel;
		createProgressDialog(asyncTask);
	
		taskList.put(asyncTask, onTaskCompleteListener);
		asyncTask.setProgressTracker(this);
		((BaseTask<Object>) asyncTask).execute(params);
	}

	@Override
	public synchronized void onCancel(DialogInterface paramDialogInterface) {
		Set<BaseTask<Object>> set = taskList.keySet();
		for (BaseTask<Object> item : set) {
			if (item.isProgressDialogNeeded()) {
				item.cancel(true);
				taskList.remove(item);
			}
		}
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	public synchronized void finishAll() {
		isDestroy = true;

		Set<BaseTask<Object>> set = taskList.keySet();
		for (BaseTask<Object> item : set) {
			item.cancel(true);
		}
		taskList.clear();

		if (mDialog != null)
			mDialog.dismiss();
	}

	private void createProgressDialog(BaseTask task) {
		if (mDialog == null) {
			if (task.isProgressDialogNeeded()) {
				String progressMessage = task.getmProgressMessage();
				if (progressMessage == null) {
					mDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
					mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

					mDialog.setContentView(R.layout.progress);

				} else {
					mDialog = new ProgressDialog(context);
					((ProgressDialog) mDialog).setMessage(progressMessage);
				}
				mDialog.setCancelable(isCanCancel);
				mDialog.setOnCancelListener(this);
				mDialog.setCanceledOnTouchOutside(false);
			}
		}
	}

	@Override
	public synchronized void onProgress() {
		if (mDialog != null && !mDialog.isShowing()) {
			mDialog.show();
		}
	}

	@Override
	public synchronized void onComplete(Object task) {
		if (task instanceof BaseTask) {
			if (taskList.get(task) != null) {
				taskList.get(task).onTaskComplete((BaseTask<Object>) task);
			}
			taskList.remove(task);

			boolean isProgressDialogDismiss = true;
			Set<BaseTask<Object>> set = taskList.keySet();
			for (BaseTask<Object> item : set) {
				if (item.isProgressDialogNeeded()) {
					isProgressDialogDismiss = false;
				}
			}
			if (isProgressDialogDismiss) {
				if (mDialog != null) {
					mDialog.dismiss();
					mDialog = null;
				}
			}
		}
	}

}
