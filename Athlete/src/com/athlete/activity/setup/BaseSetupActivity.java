package com.athlete.activity.setup;

import org.json.JSONObject;

import android.os.Bundle;
import android.widget.Toast;

import com.athlete.R;
import com.athlete.activity.auth.ActivityBaseAuth;
import com.athlete.bl.BaseBl;
import com.athlete.bl.UserBL;
import com.athlete.model.ProfileUser;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.SignUpDetailsTask;

public class BaseSetupActivity extends ActivityBaseAuth implements
		OnTskCpltListener {
	protected ProfileUser profileUser;
	protected User currentUser;
	protected JSONObject jsonObjSend;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userBL = new UserBL(getHelper());
		baseBl = new BaseBl(BaseSetupActivity.this);
		currentUser = userBL.getBy(getUserID());
		profileUser = baseBl.getFromDBByField(ProfileUser.class,
				ProfileUser.ID, currentUser.getProfileID());
		if (profileUser == null) {
			profileUser = new ProfileUser();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onTaskComplete(@SuppressWarnings("rawtypes") BaseTask task) {
		TaskResult<Boolean> result;
		try {
			result = (TaskResult<Boolean>) task.get();
			if (result.isError()) {
				String toast;
				if (result.getError_description() != null
						&& result.getError_description().length() > 0) {
					toast = result.getError_description();
				} else {
					toast = getString(R.string.toast_non_internet);
				}
				Toast.makeText(BaseSetupActivity.this, toast,
						Toast.LENGTH_SHORT).show();
			}

		} catch (Exception e) {
		}
	}

	protected void updateUser(String type) {
		SignUpDetailsTask detailsTask;
		if (type != null) {
			detailsTask = new SignUpDetailsTask(BaseSetupActivity.this,
					getURLHost(), getPublicKey(), getPrivateKey(), getApikey(),
					getUserName(), currentUser.getId(), jsonObjSend, type);
		} else {
			detailsTask = new SignUpDetailsTask(BaseSetupActivity.this,
					getURLHost(), getPublicKey(), getPrivateKey(), getApikey(),
					getUserName(), currentUser.getProfileID(), jsonObjSend);
		}
		getTaskManager().executeTask(detailsTask, BaseSetupActivity.this, null,
				true);
	}

}
