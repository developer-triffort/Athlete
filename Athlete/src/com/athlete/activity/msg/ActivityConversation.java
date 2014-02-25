package com.athlete.activity.msg;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.BaseActivity;
import com.athlete.activity.TabActivityMain;
import com.athlete.activity.user.ActivityUserDetails;
import com.athlete.adapter.BaseListAdapter;
import com.athlete.control.RoundedImageView;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.model.Conversation;
import com.athlete.model.ConversationM2MUser;
import com.athlete.model.TaskResult;
import com.athlete.services.BaseTask;
import com.athlete.services.IAppFilter;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.PatchReadConversationTask;
import com.athlete.services.task.get.GetConversationOneTask;
import com.athlete.services.task.get.GetConversationTask;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;

public class ActivityConversation extends BaseActivity {
	/**
	 * @author edBaev
	 */
	private ImageButton mBtnNewMessage;
	private ListView mListView;
	private ImageButton mBtnRefresh;
	private ImageView splash;
	private RelativeLayout progressBar;
	private BaseListAdapter<Conversation> adapter;
	private List<Conversation> conversations;
	private boolean isAllDialogsLoaded;
	private Animation animLarge;
	private HashMap<Integer, StringBuffer> hashMapUsers;
	private int mPosition = -1;
	private TabActivityMain activityMain;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actv_conversation);
		activityMain = ((TabActivityMain) getParent());
		mUserName = getShared().getString(
				Constants.SharedPreferencesKeys.USER_NAME, null);
		mApiKey = getShared().getString(
				Constants.SharedPreferencesKeys.API_KEY, null);
		mId = getShared().getString(Constants.SharedPreferencesKeys.CURRENT_ID,
				null);
		setView();
		conversations = baseBl.getListFromDBByFieldWithLimit(
				Conversation.class, Conversation.CURR_USER_ID, mId);
		if (conversations != null && !conversations.isEmpty()) {
			Collections.reverse(conversations);
			setAdapter();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		AnalyticsUtils.sendPageViews(ActivityConversation.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.CONVERSATION_LIST);
		startTransferAnim();
	}

	private void setView() {
		hashMapUsers = new HashMap<Integer, StringBuffer>();
		animLarge = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		animLarge.setInterpolator(new LinearInterpolator());
		animLarge.setRepeatCount(Animation.INFINITE);
		animLarge.setDuration(Constants.ANIM_DURATION);
		conversations = new LinkedList<Conversation>();
		splash = (ImageView) findViewById(R.id.imVLoading);
		mBtnRefresh = (ImageButton) findViewById(R.id.btnResresh);
		progressBar = (RelativeLayout) findViewById(R.id.relativeCircle);
		mBtnRefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View paramView) {
				getConversation(null, true);

			}
		});
		mBtnNewMessage = (ImageButton) findViewById(R.id.btnNewMessage);
		mBtnNewMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(ActivityConversation.this,
						ActivityNewMessage.class),
						Constants.REQUEST_CODE_TRANSFER);
			}
		});
		mListView = (ListView) findViewById(R.id.listMessages);
		getConversation(null, true);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_TRANSFER
				&& resultCode == Constants.REQUEST_CODE_TRANSFER) {
			isAllDialogsLoaded = true;
			getConversationOneTask((String) data
					.getSerializableExtra(Constants.INTENT_KEY.ID));
		}
		if (requestCode == Constants.REQUEST_CODE_DELETE
				&& resultCode == Constants.REQUEST_CODE_DELETE) {
			baseBl.delete((Conversation) data
					.getSerializableExtra(Constants.INTENT_KEY.ID),
					Conversation.class);
			if (mPosition != -1) {
				conversations.remove(mPosition);
			}
			setAdapter();
		}
	}

	private void startProgress() {
		progressBar.setVisibility(View.VISIBLE);
		mBtnRefresh.setVisibility(View.GONE);
		splash.startAnimation(anim);
	}

	private void stopProgress() {
		mBtnRefresh.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
	}

	private void getConversationOneTask(String urlGet) {
		startProgress();

		OnTskCpltListener getConversation = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<Conversation> result;
				stopProgress();
				try {
					result = (TaskResult<Conversation>) task.get();
					if (!result.isError() && result.getResult() != null) {
						conversations.add(0, result.getResult());
						Parcelable state = mListView.onSaveInstanceState();
						setAdapter();
						mListView.onRestoreInstanceState(state);
						isAllDialogsLoaded = true;
					}
				} catch (Exception e) {
				}
			}
		};
		GetConversationOneTask getConversationOneTask = new GetConversationOneTask(
				ActivityConversation.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, urlGet, mId);
		getTaskManager().executeTask(getConversationOneTask, getConversation,
				null, true);
	}

	private void getConversation(String later_than, final boolean update) {
		startProgress();
		OnTskCpltListener getConversation = new OnTskCpltListener() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<List<Conversation>> result;
				stopProgress();
				try {
					result = (TaskResult<List<Conversation>>) task.get();
					if (result.isError()) {
						isAllDialogsLoaded = true;
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
					} else {
						if (result.getResult().size() > 0) {
							if (update)
								conversations.clear();
							isAllDialogsLoaded = result.getResult().size() == conversations
									.size();
							conversations.addAll(result.getResult());
							if (result.getResult().size() == 20)
								isAllDialogsLoaded = false;
							else
								isAllDialogsLoaded = true;
							Parcelable state = mListView.onSaveInstanceState();
							setAdapter();
							List<Conversation> conv = baseBl
									.getListFromDBByField(Conversation.class,
											Conversation.HAS_UNREAD_MESSAGES,
											true);
							if (conv != null) {
								PreferencesUtils.setString(
										ActivityConversation.this,
										R.string.notify_msg,
										String.valueOf(conv.size()));
								activityMain.setTextNotify(String.valueOf(conv
										.size()));
							}
							mListView.onRestoreInstanceState(state);
						} else {
							PreferencesUtils.setString(
									ActivityConversation.this,
									R.string.friends, "0");
							activityMain.setTextNotify("0");
						}
					}
				} catch (Exception e) {
				}
			}
		};

		GetConversationTask conversationTask = new GetConversationTask(
				ActivityConversation.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mUserName, mApiKey, later_than, mId, null);
		getTaskManager().executeTask(conversationTask, getConversation, null,
				true);
	}

	private void setAdapter() {
		adapter = new BaseListAdapter<Conversation>(this, conversations,
				R.layout.item_msg, new IAppFilter<Conversation>() {
					@Override
					public boolean performFiltering(CharSequence constraint,
							Conversation item) {
						return false;
					}
				}) {
			@Override
			public int getCount() {
				return super.getCount()
						+ ((!isAllDialogsLoaded && !adapter.isFilter() && mBtnRefresh
								.getVisibility() == View.VISIBLE) ? 1 : 0);
			}

			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				ViewHolder holder;
				final Conversation item = getItem(position);
				if (position == getCount() - 1 && !isAllDialogsLoaded
						&& !adapter.isFilter()
						&& mBtnRefresh.getVisibility() == View.VISIBLE) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.row_update_footer,
							null);
					view.setId(R.layout.row_update_footer);
					View splash = view.findViewById(R.id.imVLoading);
					splash.startAnimation(animLarge);

					getConversation(
							(String.valueOf(getItem(position - 1).getId())),
							false);
					return view;
				}
				if (convertView == null
						|| convertView.getId() != R.layout.item_msg) {
					holder = new ViewHolder();
					convertView = getLayout();
					convertView.setId(R.layout.item_msg);
					holder.imAva = (RoundedImageView) convertView
							.findViewById(R.id.imViewAva);
					holder.imAva.setCornerRadius(corner2DP, size40dp);
					holder.txtName = (TextView) convertView
							.findViewById(R.id.txtFullName);
					holder.txtTime = (TextView) convertView
							.findViewById(R.id.txtTime);
					holder.txtTitle = (TextView) convertView
							.findViewById(R.id.txtTitle);
					holder.itemList = (LinearLayout) convertView
							.findViewById(R.id.itemList);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				if (item.isHasUnreadMessages()) {
					holder.itemList
							.setBackgroundResource(R.drawable.background_unread_item);
				} else {
					holder.itemList
							.setBackgroundResource(R.drawable.background_feed_item);
				}
				StringBuffer users = new StringBuffer();
				/*
				 * display list of users
				 */
				if (hashMapUsers.containsKey(item.getId())) {
					users = hashMapUsers.get(item.getId());
				} else {
					List<ConversationM2MUser> f = baseBl.getListFromDBByField(
							ConversationM2MUser.class,
							ConversationM2MUser.CONVERSATION, item.getId());
					for (int i = 0; i < f.size(); i++) {
						if (f.get(i).getUser() != null)
							if (!f.get(i).getUser().getId().equals(mId)) {
								if (users.length() != 0) {
									users.append(", ");
								}
								if (f.size() > 2) {
									users.append(f.get(i).getUser()
											.getFirstName());
								} else {
									users.append(f.get(i).getUser()
											.getFirstName()
											+ " ");
									users.append(f.get(i).getUser()
											.getLastName());
									break;
								}
							}
					}
					if (item.getPastUsersInvolved() != null) {
						for (String str : item.getPastUsersInvolved()) {
							if (users.length() > 0) {
								users.append(", ");
							}
							users.append(str);

						}
					}
					hashMapUsers.put(item.getId(), users);
				}
				if (item.getLastMessage().getUser() != null
						&& item.getLastMessage().getUser()
								.getProfileImage225url().startsWith("http")) {
					imageLoader.displayImage(item.getLastMessage().getUser()
							.getProfileImage225url(), holder.imAva, options);
				} else {
					holder.imAva.setImageResource(R.drawable.avatar);
				}
				holder.imAva.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (item.getLastMessage().getUser() != null) {
							startActivity(new Intent(ActivityConversation.this,
									ActivityUserDetails.class)
									.putExtra(
											Constants.INTENT_KEY.USER_DETAILS,
											item.getLastMessage().getUser()
													.getId()));
						}
					}
				});
				holder.txtTitle.setText(item.getLastMessage().getMessage());
				holder.txtTime.setText(CommonHelper.getDateHHMM(CommonHelper
						.getLongYYYYMMDDtHHMMSS(item.getLastMessage()
								.getCreatedDate()), ActivityConversation.this));
				holder.txtName.setText(users.toString());
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPosition = position;
						if (item.isHasUnreadMessages()) {
							patchRead(item, position);
						}
						startActivityForResult(new Intent(
								ActivityConversation.this, ActivityMSGs.class)
								.putExtra(Constants.INTENT_KEY.ID, item),
								Constants.REQUEST_CODE_DELETE);
					}
				});
				return convertView;
			}
		};
		mListView.setAdapter(adapter);
	}

	private void patchRead(final Conversation conversation, final int position) {
		OnTskCpltListener patchRead = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (result.isError()) {
						isAllDialogsLoaded = true;
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
					} else {
						if (result.getResult()) {
							conversation.setHasUnreadMessages(false);
							baseBl.createOrUpdate(Conversation.class,
									conversation);
							conversations.set(position, conversation);
							String not = PreferencesUtils.getString(
									ActivityConversation.this,
									R.string.notify_msg, "0");
							if (!not.equalsIgnoreCase("0")) {
								int count = Integer.valueOf(not) - 1;
								PreferencesUtils.setString(
										ActivityConversation.this,
										R.string.notify_msg,
										String.valueOf(count));
								activityMain.setTextNotify(String
										.valueOf(count));
							}
							adapter.notifyDataSetChanged();
						}
					}
				} catch (Exception e) {
				}
			}
		};
		PatchReadConversationTask conversationTask = new PatchReadConversationTask(
				ActivityConversation.this, getURLHost(), getPublicKey(),
				getPrivateKey(), mApiKey, mUserName, conversation.getId(),
				conversation.getLastMessage().getId());
		getTaskManager().executeTask(conversationTask, patchRead, null, true);

	}
}
