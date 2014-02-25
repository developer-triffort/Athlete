package com.athlete.activity.user;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.adapter.BaseListAdapter;
import com.athlete.control.RoundedImageView;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.SearchUsersTask;

public class ActivitySearchUsers extends BaseActivity {
	/**
	 * @author edBaev
	 */
	private ListView mListView;
	private List<User> users = new LinkedList<User>();
	private BaseListAdapter<User> adapter;
	private RelativeLayout progressBar;
	private ImageView splash;
	private EditText mEdText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_search_users);
		startTransferAnim();
		setView();
	}

	private void setView() {
		mListView = (ListView) findViewById(R.id.listView);
		splash = (ImageView) findViewById(R.id.imVLoading);
		progressBar = (RelativeLayout) findViewById(R.id.relativeCircle);
		mUserName = getShared().getString(
				Constants.SharedPreferencesKeys.USER_NAME, null);
		mApiKey = getShared().getString(
				Constants.SharedPreferencesKeys.API_KEY, null);
		mEdText = (EditText) findViewById(R.id.edTxtSearch);

		mEdText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						&& progressBar.getVisibility() == View.GONE
						&& mEdText.getText().toString().length() > 0) {

					getUserByQuery(mEdText.getText().toString());

					return true;
				}
				return false;
			}
		});
		mEdText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mEdText.getText().toString().length() > 0) {
					if (progressBar.getVisibility() != View.VISIBLE) {
						getUserByQuery(mEdText.getText().toString());
					}
				} else {
					clearAll();
				}
			}
		});
		findViewById(R.id.imViewClear).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEdText.setText("");
						clearAll();

					}
				});
		findViewById(R.id.btnCancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						hideKeyboard(ActivitySearchUsers.this);
						onBackPressed();
					}
				});
		setAdapter();
	}

	private void clearAll() {
		progressBar.setVisibility(View.GONE);
		users.clear();
		adapter.notifyDataSetChanged();
	}

	private void setAdapter() {
		adapter = new BaseListAdapter<User>(ActivitySearchUsers.this, users,
				R.layout.item_addreesse) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				final User user = getItem(position);

				if (convertView == null) {
					holder = new ViewHolder();
					convertView = getLayout();
					holder.txtName = (TextView) convertView
							.findViewById(R.id.txtFullName);
					holder.txtArea = (TextView) convertView
							.findViewById(R.id.txtArea);
					holder.imAva = (RoundedImageView) convertView
							.findViewById(R.id.avaFriend);
					holder.imAva.setCornerRadius(corner2DP, size50dp);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View paramView) {
						startActivity(new Intent(ActivitySearchUsers.this,
								ActivityUserDetails.class).putExtra(
								Constants.INTENT_KEY.USER_DETAILS, user.getId()));
					}
				});
				if (user.getProfileImage225url().startsWith("http")) {
					imageLoader.displayImage(user.getProfileImage225url(),
							holder.imAva, options);
				} else {
					holder.imAva.setImageResource(R.drawable.avatar);
				}
				holder.txtName.setText(user.getFirstName() + " "
						+ user.getLastName());

				return convertView;

			}
		};
		adapter.setNotifyOnChange(true);
		mListView.setAdapter(adapter);
	}

	private void getUserByQuery(final String q) {

		progressBar.setVisibility(View.VISIBLE);
		splash.startAnimation(anim);
		OnTskCpltListener getSearchResult = new OnTskCpltListener() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<List<User>> result;
				if (mEdText.getText().toString().equals(q)) {
					splash.clearAnimation();
					progressBar.setVisibility(View.GONE);
				}
				findViewById(R.id.relativeCircle).setVisibility(View.GONE);
				try {
					result = (TaskResult<List<User>>) task.get();
					if (result.isError()
							&& mEdText.getText().toString().equals(q)) {
						users = userBL.getListFromDBByFullname(q);
						adapter.setList(users);
						adapter.notifyDataSetChanged();

					} else {
						if (result.getResult().size() > 0) {

							if (mEdText.getText().toString().equals(q)) {
								users = result.getResult();
								adapter.setList(users);
								adapter.notifyDataSetChanged();
							} else {
								getUserByQuery(mEdText.getText().toString());
							}

						}
						if (result.getResult().size() == 0) {
							if (mEdText.getText().toString().equals(q)) {
								users.clear();
								adapter.notifyDataSetChanged();

								splash.clearAnimation();
								progressBar.setVisibility(View.GONE);
							} else {
								getUserByQuery(mEdText.getText().toString());
							}
						}
					}
				} catch (Exception e) {
				}
			}
		};

		if (q.length() == 0) {
			splash.clearAnimation();
			progressBar.setVisibility(View.GONE);
			return;
		}
		@SuppressWarnings("deprecation")
		SearchUsersTask searchUsersTask = new SearchUsersTask(this,
				getURLHost(), getPublicKey(), getPrivateKey(), mUserName,
				mApiKey, URLEncoder.encode(q), getUserID());
		getTaskManager().executeTask(searchUsersTask, getSearchResult, null,
				true);
	}
}
