package com.athlete.activity.msg;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.adapter.BaseListAdapter;
import com.athlete.control.RoundedImageView;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.CreateNewConversationTask;
import com.athlete.services.task.SearchUsersTask;
import com.athlete.util.AnalyticsUtils;

public class ActivityNewMessage extends BaseActivity {
	/**
	 * @author edBaev
	 */
	private Button mBtnSendMessage;
	private EditText mEditWriteMessage;
	private ImageButton mImBtnAdd;
	private User currentUser;
	private RoundedImageView roundedImageView;
	private LinearLayout linear1, linear2, linearForEdit;
	private HashMap<String, User> userAddresse;
	private LinearLayout.LayoutParams paramsForTextView;
	private RelativeLayout progressBar;
	private ImageView splash;
	private EditText mEdText;
	private FrameLayout linearWithTextViewAndEditText;

	private View headerView;
	private ListView mListView;
	private BaseListAdapter<User> adapter;
	private List<User> searchUser;
	private List<User> friends;

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityNewMessage.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.CONVERSATION_CREATE);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_new_message);
		startTransferAnim();
		mUserName = getShared().getString(
				Constants.SharedPreferencesKeys.USER_NAME, null);
		mApiKey = getShared().getString(
				Constants.SharedPreferencesKeys.API_KEY, null);
		mId = getShared().getString(Constants.SharedPreferencesKeys.CURRENT_ID,
				null);
		currentUser = userBL.getBy(mId);
		String userID = getIntent().getStringExtra(
				Constants.INTENT_KEY.USER_DETAILS);
		setView();
		if (userID != null) {
			User user = userBL.getBy(userID);
			if (user != null) {
				userAddresse.put(userID, user);
			}
		}
		setTableUsers();
		setAdapter();
		changeMode();
	}

	@SuppressWarnings("deprecation")
	private void setTableUsers() {
		mEdText = new EditText(this);
		mEdText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mEdText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
		mEdText.setBackgroundDrawable(null);
		mEdText.setLayoutParams(paramsForTextView);
		mEdText.setText("");
		linearWithTextViewAndEditText.removeAllViews();
		linear1.removeAllViews();
		linear2.removeAllViews();
		final List<User> users = new LinkedList<User>();
		users.addAll(userAddresse.values());
		for (int i = 0; i < users.size(); i++) {
			TextView txt = new TextView(this);
			txt.setTextColor(Color.BLACK);
			txt.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.messages_to_name_bg));
			txt.setCompoundDrawablesWithIntrinsicBounds(null, null,
					getResources().getDrawable(R.drawable.messages_to_name_x),
					null);

			txt.setLayoutParams(paramsForTextView);
			txt.setText(users.get(i).getFirstName() + " "
					+ users.get(i).getLastName());
			txt.setTag(String.valueOf(i));
			if (i % 2 == 0) {
				linear1.addView(txt);
			} else {
				linear2.addView(txt);
			}
			txt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean canAdd = true;
					userAddresse.remove(users.get(
							Integer.parseInt((String) v.getTag())).getId());
					for (User user : searchUser) {
						if (user.getId()
								.equals(users.get(
										Integer.parseInt((String) v.getTag()))
										.getId())) {
							canAdd = false;
							break;
						}
					}
					if (canAdd) {
						searchUser.add(users.get(Integer.parseInt((String) v
								.getTag())));
					}
					setTableUsers();
				}
			});
		}
		if (users.size() % 2 == 0) {
			linearWithTextViewAndEditText.addView(mEdText);
		} else {
			linear2.addView(mEdText);
		}
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
					if (linearForEdit.getVisibility() != View.GONE) {
						chooseMode();
					}
					if (progressBar.getVisibility() != View.VISIBLE) {
						getUserByQuery(mEdText.getText().toString());
					}
				} else {
					if (linearForEdit.getVisibility() == View.GONE) {
						mBtnSendMessage.setVisibility(View.VISIBLE);
						splash.clearAnimation();
						progressBar.setVisibility(View.GONE);
						editMode();
					}
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	private void setView() {
		searchUser = new LinkedList<User>();
		friends = new LinkedList<User>();
		friends = friendsBL.getFriendByUserAccepted(getUserID());
		mListView = (ListView) findViewById(R.id.listView);
		headerView = getLayoutInflater().inflate(R.layout.header_new_message,
				null);
		linearForEdit = (LinearLayout) findViewById(R.id.linearForEdit);
		linearForEdit.setVisibility(View.GONE);
		mEditWriteMessage = (EditText) findViewById(R.id.editTextWriteMessage);
		mListView.addHeaderView(headerView);
		linearWithTextViewAndEditText = (FrameLayout) headerView
				.findViewById(R.id.linearWithTextViewAndEditText);
		splash = (ImageView) findViewById(R.id.imVLoading);
		progressBar = (RelativeLayout) findViewById(R.id.relativeCircle);
		paramsForTextView = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		paramsForTextView.setMargins(padding2DP, padding2DP, padding2DP,
				padding2DP);
		paramsForTextView.gravity = Gravity.CENTER_VERTICAL;
		userAddresse = new HashMap<String, User>();
		mImBtnAdd = (ImageButton) headerView.findViewById(R.id.btnPlus);
		roundedImageView = (RoundedImageView) findViewById(R.id.roundedAva);
		roundedImageView.setCornerRadius(corner2DP, size50dp);
		if (currentUser.getProfileImage225url().startsWith("http")) {
			imageLoader.displayImage(currentUser.getProfileImage225url(),
					roundedImageView, options);
		} else {
			roundedImageView.setImageResource(R.drawable.avatar);
		}
		mBtnSendMessage = (Button) findViewById(R.id.btnSend);

		linear1 = (LinearLayout) headerView.findViewById(R.id.linear1);
		linear2 = (LinearLayout) headerView.findViewById(R.id.linear2);

		mBtnBack = (ImageButton) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onBackPressed();
			}
		});
		mBtnSendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBtnSendMessage.setEnabled(false);
				InputMethodManager imm = (InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
						0);
				if (mEditWriteMessage.getText().toString() == null
						|| mEditWriteMessage.getText().toString().length() == 0) {
					Toast.makeText(ActivityNewMessage.this,
							getString(R.string.enter_message),
							Toast.LENGTH_SHORT).show();
					mBtnSendMessage.setEnabled(true);
				} else {
					if (userAddresse.size() == 0) {
						Toast.makeText(ActivityNewMessage.this,
								getString(R.string.enter_recipients),
								Toast.LENGTH_SHORT).show();
						mBtnSendMessage.setEnabled(true);
					} else {
						Integer[] usersID = new Integer[userAddresse.size()];
						int index = 0;
						for (Entry<String, User> mapEntry : userAddresse
								.entrySet()) {
							usersID[index] = Integer.parseInt(mapEntry.getKey());
							index++;
						}
						Toast.makeText(ActivityNewMessage.this,
								getString(R.string.toast_sending_message),
								Toast.LENGTH_LONG).show();
						AnalyticsUtils
								.sendPageViews(
										ActivityNewMessage.this,
										AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.CONVERSATION_CREATE,
										AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
										AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
										AnalyticsUtils.GOOGLE_ANALYTICS.COMPOSE,
										0);
						startNewConversation(mEditWriteMessage.getText()
								.toString(), usersID);
					}
				}
			}
		});
		mImBtnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeMode();
			}
		});
	}

	private void changeMode() {
		if (linearForEdit.getVisibility() == View.GONE) {
			editMode();
		} else {
			chooseMode();
		}
	}

	private void editMode() {
		searchUser = new LinkedList<User>();
		adapter.setList(searchUser);
		adapter.notifyDataSetChanged();
		linearForEdit.setVisibility(View.VISIBLE);
	}

	private void chooseMode() {
		searchUser = new LinkedList<User>();
		searchUser.addAll(friends);
		adapter.setList(searchUser);
		adapter.notifyDataSetChanged();
		linearForEdit.setVisibility(View.GONE);
	}

	private void setAdapter() {
		adapter = new BaseListAdapter<User>(ActivityNewMessage.this,
				searchUser, R.layout.item_addreesse) {
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
						if (!user.getId().equals(currentUser.getId())) {
							baseBl.createOrUpdate(User.class, user);
							userAddresse.put(user.getId(), user);
							setTableUsers();
							searchUser.remove(user);
							adapter.notifyDataSetChanged();
							editMode();
							hideKeyboard(ActivityNewMessage.this);
						}
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

	private void startProgress() {
		progressBar.setVisibility(View.VISIBLE);
		mBtnSendMessage.setVisibility(View.GONE);
		splash.startAnimation(anim);
	}

	private void stopProgress() {
		mBtnSendMessage.setVisibility(View.VISIBLE);
		splash.clearAnimation();
		progressBar.setVisibility(View.GONE);
	}

	private void startNewConversation(String message, Integer[] users) {
		editMode();
		startProgress();
		OnTskCpltListener startNewConversation = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				mBtnSendMessage.setEnabled(true);
				stopProgress();
				TaskResult<String> result;
				try {
					result = (TaskResult<String>) task.get();
					if (!result.isError() && result.getResult() != null) {
						setResult(Constants.REQUEST_CODE_TRANSFER,
								new Intent().putExtra(Constants.INTENT_KEY.ID,
										result.getResult()));
						onBackPressed();
					}
				} catch (Exception e) {
				}
			}
		};
		CreateNewConversationTask createNewConversationTask = new CreateNewConversationTask(
				ActivityNewMessage.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, message, users);
		getTaskManager().executeTask(createNewConversationTask,
				startNewConversation, null, true);
	}

	private void getUserByQuery(final String q) {
		adapter.clear();
		startProgress();
		OnTskCpltListener getSearchResult = new OnTskCpltListener() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<List<User>> result;
				if (mEdText.getText().toString().equals(q)) {
					stopProgress();
				}
				findViewById(R.id.relativeCircle).setVisibility(View.GONE);
				try {
					result = (TaskResult<List<User>>) task.get();
					if (result.isError()) {
						if (linearForEdit.getVisibility() == View.GONE) {
							searchUser = userBL.getListFromDBByFullname(q);
							adapter.setList(searchUser);
							adapter.notifyDataSetChanged();
						}
					} else {
						if (result.getResult().size() > 0) {
							if (linearForEdit.getVisibility() == View.GONE) {
								if (mEdText.getText().toString().equals(q)) {
									searchUser = result.getResult();
									adapter.setList(searchUser);
									adapter.notifyDataSetChanged();
								} else {
									getUserByQuery(mEdText.getText().toString());
								}
							}
						}
						if (result.getResult().size() == 0) {
							if (mEdText.getText().toString().equals(q)) {
								adapter.clear();
								adapter.notifyDataSetChanged();
								mBtnSendMessage.setVisibility(View.VISIBLE);
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
		@SuppressWarnings("deprecation")
		SearchUsersTask searchUsersTask = new SearchUsersTask(this,
				getURLHost(), getPublicKey(), getPrivateKey(), mUserName,
				mApiKey, URLEncoder.encode(q), currentUser.getId());
		getTaskManager().executeTask(searchUsersTask, getSearchResult, null,
				true);
	}

	@Override
	public void onBackPressed() {
		hideKeyboard(ActivityNewMessage.this);
		if (linearForEdit.getVisibility() != View.GONE) {
			super.onBackPressed();
		} else {
			editMode();
		}
	}
}
