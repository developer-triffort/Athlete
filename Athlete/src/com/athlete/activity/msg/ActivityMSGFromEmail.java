package com.athlete.activity.msg;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.ActivityWelcome;
import com.athlete.activity.BaseActivity;
import com.athlete.activity.user.ActivityUserDetails;
import com.athlete.adapter.BaseListAdapter;
import com.athlete.control.RoundedImageView;
import com.athlete.model.Conversation;
import com.athlete.model.ConversationM2MUser;
import com.athlete.model.Message;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.services.BaseTask;
import com.athlete.services.IAppFilter;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.ArchiveTheConversationTask;
import com.athlete.services.task.SendMessageTask;
import com.athlete.services.task.get.GetConversationOneTask;
import com.athlete.services.task.get.GetMessageOneTask;
import com.athlete.services.task.get.GetMessagesTask;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;

public class ActivityMSGFromEmail extends BaseActivity {
	/**
	 * @author edBaev
	 */
	private ImageButton mBtnDelete;
	private ListView mListView;
	private ImageButton mBtnRefresh;
	private ImageView splash;
	private RelativeLayout progressBar;
	private BaseListAdapter<Message> adapter;
	private List<Message> messages;
	private boolean isAllDialogsLoaded;
	private Animation animLarge;

	private Conversation currentConversation;
	private EditText mEditReply;
	private User currentUser;
	private RoundedImageView roundedImageView;
	private LinearLayout.LayoutParams paramsAvaForward;
	private StringBuffer users;
	private LinearLayout txtUsersLinear, linear1, linear2;
	private int converstionID;

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityMSGFromEmail.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.MESSAGE_SCREEN);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MainSearchLayout searchLayout = new MainSearchLayout(this, null);
		setContentView(searchLayout);
		if (getUserName() == null || getApikey() == null) {
			startActivity(new Intent(ActivityMSGFromEmail.this,
					ActivityWelcome.class));
			finish();
		} else {
			Uri data = getIntent().getData();

			List<String> params = data.getPathSegments();

			converstionID = Integer.valueOf(params.get(2));
			currentConversation = baseBl.getFromDBByField(Conversation.class,
					Conversation.ID, converstionID);

			mUserName = getShared().getString(
					Constants.SharedPreferencesKeys.USER_NAME, null);
			mApiKey = getShared().getString(
					Constants.SharedPreferencesKeys.API_KEY, null);
			mId = getShared().getString(
					Constants.SharedPreferencesKeys.CURRENT_ID, null);
			setView();
			if (currentConversation != null) {
				setViewConversation();
			} else {
				getConversationOneTask();
			}
		}
	}

	private void getConversationOneTask() {
		String urlGet = Constants.HOST.API_V + "/conversation/" + converstionID;
		startProgress();

		OnTskCpltListener getConversation = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<Conversation> result;
				hideProgress();
				try {
					result = (TaskResult<Conversation>) task.get();
					if (!result.isError() && result.getResult() != null) {
						currentConversation = result.getResult();
						setViewConversation();
					} else {
						finish();
					}
				} catch (Exception e) {
				}
			}
		};
		GetConversationOneTask getConversationOneTask = new GetConversationOneTask(
				ActivityMSGFromEmail.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, urlGet, mId);
		getTaskManager().executeTask(getConversationOneTask, getConversation,
				null, true);
	}

	private void setView() {
		users = new StringBuffer();
		txtUsersLinear = (LinearLayout) findViewById(R.id.txtAddresLinear);
		paramsAvaForward = new LinearLayout.LayoutParams(size35dp, size35dp);
		roundedImageView = new RoundedImageView(this);

		roundedImageView.setLayoutParams(paramsAvaForward);
		roundedImageView.setCornerRadius(corner2DP, size35dp);
		currentUser = userBL.getBy(mId);
		linear1 = (LinearLayout) findViewById(R.id.linear1);
		linear2 = (LinearLayout) findViewById(R.id.linear2);

		if (currentUser.getProfileImage225url().startsWith("http")) {
			imageLoader.displayImage(currentUser.getProfileImage225url(),
					roundedImageView, options);
		} else {
			roundedImageView.setImageResource(R.drawable.avatar);
		}
		mEditReply = (EditText) findViewById(R.id.editTextReply);
		mEditReply.setCompoundDrawablesWithIntrinsicBounds(
				roundedImageView.getDrawable(), null, null, null);
		animLarge = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		animLarge.setInterpolator(new LinearInterpolator());
		animLarge.setRepeatCount(Animation.INFINITE);
		animLarge.setDuration(Constants.ANIM_DURATION);
		messages = new LinkedList<Message>();
		splash = (ImageView) findViewById(R.id.imVLoading);
		mBtnRefresh = (ImageButton) findViewById(R.id.btnResresh);
		progressBar = (RelativeLayout) findViewById(R.id.relativeCircle);

		mBtnRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				getMessages(null, true);
			}
		});

		mBtnDelete = (ImageButton) findViewById(R.id.btnDelete);
		mListView = (ListView) findViewById(R.id.listMessages);
		mBtnBack = (ImageButton) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				onBackPressed();
			}
		});
	}

	private void setViewConversation() {
		List<ConversationM2MUser> f = baseBl.getListFromDBByField(
				ConversationM2MUser.class, ConversationM2MUser.CONVERSATION,
				currentConversation.getId());
		LinearLayout linearLayout = new LinearLayout(this);
		for (int i = 0; i < f.size(); i++) {
			users = new StringBuffer();
			TextView txt = new TextView(this);
			final User us = f.get(i).getUser();

			users.append("<font color='#54A1C7'>" + us.getFirstName() + " "
					+ us.getLastName() + "</font>");
			if (i != f.size() - 1) {
				users.append("<font color='#ACABAA'> & </font>");
			}
			txt.setText(Html.fromHtml(users.toString()));

			txt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(ActivityMSGFromEmail.this,
							ActivityUserDetails.class).putExtra(
							Constants.INTENT_KEY.USER_DETAILS, us.getId()));
				}
			});

			if (linearLayout.getWidth() <= (txtUsersLinear.getWidth() + txt
					.getWidth()) && i != f.size() - 1) {
			}
			if (i % 2 == 0) {
				linear1.addView(txt);

			} else {
				linear2.addView(txt);
			}
		}
		for (int i = 0; i < currentConversation.getPastUsersInvolved().size(); i++) {
			TextView txt = new TextView(this);
			txt.setText(" & "
					+ currentConversation.getPastUsersInvolved().get(i));
			if ((i + f.size()) % 2 == 0) {
				linear1.addView(txt);
			} else {
				linear2.addView(txt);
			}
		}

		mEditReply
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						boolean handled = false;
						if (actionId == EditorInfo.IME_ACTION_SEND) {
							if (mEditReply.getText().toString() != null
									& mEditReply.getText().toString().length() > 0) {
								Toast.makeText(
										ActivityMSGFromEmail.this,
										getString(R.string.toast_sending_message),
										Toast.LENGTH_LONG).show();
								AnalyticsUtils
										.sendPageViews(
												ActivityMSGFromEmail.this,
												AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.MESSAGE_SCREEN,
												AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
												AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
												AnalyticsUtils.GOOGLE_ANALYTICS.COMPOSE,
												0);
								sendMessage(mEditReply.getText().toString(),
										currentConversation.getId());
								mEditReply.setText("");
								mEditReply.clearFocus();
								InputMethodManager imm = (InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(getCurrentFocus()
										.getWindowToken(), 0);
								handled = true;
							}
						}
						return handled;
					}
				});

		messages = baseBl.getListFromDBByField(Message.class,
				Message.CONVERSATION, currentConversation);
		Collections.reverse(messages);
		if (messages != null || !messages.isEmpty()) {
			isAllDialogsLoaded = true;
			setAdapter();
		}
		getMessages(null, true);

		mBtnDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(ActivityMSGFromEmail.this,
						getString(R.string.toast_archiving_conversation),
						Toast.LENGTH_LONG).show();
				archiveConversation(currentConversation.getId());
			}
		});
	}

	private void startProgress() {
		progressBar.setVisibility(View.VISIBLE);
		mBtnRefresh.setVisibility(View.GONE);
		splash.startAnimation(anim);
	}

	private void sendMessage(String message, int conversationID) {
		startProgress();
		OnTskCpltListener getMessages = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<String> result;
				try {
					result = (TaskResult<String>) task.get();
					if (!result.isError() && result.getResult() != null) {
						getMessageOne(result.getResult());
					}
				} catch (Exception e) {
				}
			}
		};
		SendMessageTask messageTask = new SendMessageTask(
				ActivityMSGFromEmail.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, conversationID, message);
		getTaskManager().executeTask(messageTask, getMessages, null, true);
	}

	private void hideProgress() {
		mBtnRefresh.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		splash.clearAnimation();
	}

	private void getMessages(String later_than, final boolean update) {
		startProgress();
		OnTskCpltListener getMessages = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<List<Message>> result;
				hideProgress();
				try {
					result = (TaskResult<List<Message>>) task.get();
					if (result.isError()) {
						isAllDialogsLoaded = true;
						if (adapter != null)
							adapter.notifyDataSetChanged();
					} else {
						if (result.getResult().size() > 0) {
							if (update)
								messages.clear();
							isAllDialogsLoaded = result.getResult().size() == messages
									.size();
							messages.addAll(result.getResult());
							if (result.getResult().size() == 20) {
								{
									isAllDialogsLoaded = false;
								}
							} else {
								isAllDialogsLoaded = true;
							}
							Parcelable state = mListView.onSaveInstanceState();
							setAdapter();
							mListView.onRestoreInstanceState(state);
						}
					}
				} catch (Exception e) {
				}
			}
		};
		GetMessagesTask messageTask = new GetMessagesTask(
				ActivityMSGFromEmail.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, later_than,
				currentConversation);
		getTaskManager().executeTask(messageTask, getMessages, null, true);
	}

	private void getMessageOne(String URL) {
		OnTskCpltListener getMessages = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<Message> result;
				hideProgress();
				try {
					result = (TaskResult<Message>) task.get();
					if (!result.isError() && result.getResult() != null) {
						result.getResult().setUser(currentUser);
						result.getResult().setConversation(currentConversation);
						baseBl.createOrUpdate(Message.class, result.getResult());
						messages.add(0, result.getResult());

						Parcelable state = mListView.onSaveInstanceState();
						setAdapter();
						mListView.onRestoreInstanceState(state);
					}
				} catch (Exception e) {
				}
			}
		};
		GetMessageOneTask messageTask = new GetMessageOneTask(
				ActivityMSGFromEmail.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, URL);
		getTaskManager().executeTask(messageTask, getMessages, null, true);
	}

	private void setAdapter() {
		adapter = new BaseListAdapter<Message>(this, messages,
				R.layout.item_msgs_list, new IAppFilter<Message>() {
					@Override
					public boolean performFiltering(CharSequence constraint,
							Message item) {
						return false;
					}
				}) {
			@Override
			public int getCount() {
				return super.getCount()
						+ ((!isAllDialogsLoaded && !adapter.isFilter()) ? 1 : 0);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				final Message item = getItem(position);
				if (position == getCount() - 1 && !isAllDialogsLoaded
						&& !adapter.isFilter()) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.row_update_footer,
							null);
					view.setId(R.layout.row_update_footer);
					View splash = view.findViewById(R.id.imVLoading);
					splash.startAnimation(animLarge);
					getMessages(
							(String.valueOf(getItem(position - 1).getId())),
							false);
					return view;
				}
				if (convertView == null
						|| convertView.getId() != R.layout.item_msgs_list) {
					holder = new ViewHolder();
					convertView = getLayout();
					convertView.setId(R.layout.item_msgs_list);
					holder.imAva = (RoundedImageView) convertView
							.findViewById(R.id.imViewAva);
					holder.imAva.setCornerRadius(corner2DP, size40dp);
					holder.txtName = (TextView) convertView
							.findViewById(R.id.txtFullName);
					holder.txtTime = (TextView) convertView
							.findViewById(R.id.txtTime);
					holder.txtTitle = (TextView) convertView
							.findViewById(R.id.txtTitle);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				if (item.getUser() != null
						&& item.getUser().getProfileImage225url()
								.startsWith("http")) {
					imageLoader.displayImage(item.getUser()
							.getProfileImage225url(), holder.imAva, options);
				} else {
					holder.imAva.setImageResource(R.drawable.avatar);
				}
				holder.imAva.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (item.getUser() != null) {
							startActivity(new Intent(ActivityMSGFromEmail.this,
									ActivityUserDetails.class).putExtra(
									Constants.INTENT_KEY.USER_DETAILS, item
											.getUser().getId()));
						}
					}
				});
				holder.txtTitle.setText(item.getMessage());
				holder.txtTime.setText(CommonHelper.getDateHHMM(CommonHelper
						.getLongYYYYMMDDtHHMMSS(item.getCreatedDate()),
						ActivityMSGFromEmail.this));
				if (item.getUser() != null) {
					holder.txtName.setText(item.getUser().getFirstName() + " "
							+ item.getUser().getLastName());
				} else {
					holder.txtName.setText(Html.fromHtml("<font color='"
							+ getResources().getColor(R.color.grey) + "'>"
							+ item.getUserDeletedName() + " "
							+ getString(R.string.user_not_exist) + "</font>"));
				}

				return convertView;
			}
		};
		mListView.setAdapter(adapter);
		mListView.setSelection(messages.size() - 1);
	}

	public class MainSearchLayout extends LinearLayout {
		public MainSearchLayout(Context context, AttributeSet attributeSet) {
			super(context, attributeSet);
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.actv_msgs, this);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
			final int actualHeight = getHeight();
			if (actualHeight > proposedheight && messages != null
					&& messages.size() > 0) {
				setAdapter();
			}
		}
	}

	private void archiveConversation(int id) {
		OnTskCpltListener archive = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (!result.isError() && result.getResult()) {
						baseBl.delete(currentConversation, Conversation.class);
						finish();
					}
				} catch (Exception e) {
				}
			}
		};
		ArchiveTheConversationTask archiveTheConversationTask = new ArchiveTheConversationTask(
				ActivityMSGFromEmail.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, id);
		getTaskManager().executeTask(archiveTheConversationTask, archive, null,
				true);
	}
}
