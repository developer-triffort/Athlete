package com.athlete.activity.track.details;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.TabActivityMain;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.bl.FeedBL;
import com.athlete.bl.UserBL;
import com.athlete.bl.WorkoutBL;
import com.athlete.control.RoundedImageView;
import com.athlete.google.android.apps.mytracks.MapOverlay;
import com.athlete.google.android.apps.mytracks.MyMapFragment;
import com.athlete.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.athlete.google.android.apps.mytracks.content.Track;
import com.athlete.google.android.apps.mytracks.content.TrackDataHub;
import com.athlete.google.android.apps.mytracks.content.TrackDataType;
import com.athlete.google.android.apps.mytracks.util.ApiAdapterFactory;
import com.athlete.google.android.apps.mytracks.util.PreferencesUtils;
import com.athlete.google.android.apps.mytracks.util.StatsUtils;
import com.athlete.google.android.apps.mytracks.util.StringUtils;
import com.athlete.model.Comment;
import com.athlete.model.Feed;
import com.athlete.model.Feed2Type2User;
import com.athlete.model.PostPicture;
import com.athlete.model.TaskResult;
import com.athlete.model.WorkOut;
import com.athlete.model.WorkoutM2MTrack;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.SendCommentTask;
import com.athlete.services.task.SendLikeTask;
import com.athlete.services.task.delete.DeleteFeedTask;
import com.athlete.services.task.get.GetCommentListTask;
import com.athlete.services.task.get.GetFeedByIDTask;
import com.athlete.services.task.get.GetWorkoutByIDTask;
import com.athlete.util.AnalyticsUtils;
import com.athlete.util.CommonHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.maps.MapView;

@SuppressLint("ResourceAsColor")
public class BaseTrackDetails extends AddPhotoAndInitActivity {
	/**
	 * @author edBaev
	 */
	private boolean isTaskGetGPX;
	private ArrayList<Integer> likes;
	MyMapFragment mfg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		MainSearchLayout searchLayout = new MainSearchLayout(this, null);
		setContentView(searchLayout);
		initilizeMap();
		isClimb = true;
		asyncTaskManager = ((AthleteApplication) getApplication())
				.getTaskManager(BaseTrackDetails.this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			setAlbumStorageDirFactory(new FroyoAlbumDirFactory());
		} else {
			setAlbumStorageDirFactory(new BaseAlbumDirFactory());
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (trackDataHub != null) {
			trackDataHub.stop();
		}
	}

	public class MainSearchLayout extends LinearLayout {
		public MainSearchLayout(Context context, AttributeSet attributeSet) {
			super(context, attributeSet);
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.actv_route_detail, this);
		}

		@SuppressLint("DrawAllocation")
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
			final int actualHeight = getHeight();
			// Keyboard is show
			if (actualHeight > proposedheight) {
				scrollVertical.post(new Runnable() {
					public void run() {
						scrollVertical.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		init();
		AnalyticsUtils.sendPageViews(BaseTrackDetails.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FEED_DETAILS);
		initilizeMap();
	}

	private void checkFeed(final String id) {
		OnTskCpltListener getFeedByID = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<Feed> result;
				try {
					result = (TaskResult<Feed>) task.get();
					if (!result.isError() && result.getResult() != null) {
						setFeed(result.getResult());
						initFeed();
					} else {
						if (result.getError().equals("404")) {
							Toast.makeText(BaseTrackDetails.this,
									getString(R.string.post_not_exist),
									Toast.LENGTH_SHORT).show();
							delete();
						}
					}
				} catch (Exception e) {
				}
			}
		};
		GetFeedByIDTask getFeedByIdTask = new GetFeedByIDTask(
				BaseTrackDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), id,
				getBaseOperationsBL(), Integer.valueOf(getUserID()), 0);
		asyncTaskManager.executeTask(getFeedByIdTask, getFeedByID, null, true);
	}

	private void getFeedByID(final String id) {
		OnTskCpltListener getFeedByID = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<Feed> result;
				try {
					result = (TaskResult<Feed>) task.get();
					if (!result.isError() && result.getResult() != null) {
						setFeed(result.getResult());
						initFeedCorrect();
					}
				} catch (Exception e) {
				}
			}
		};
		GetFeedByIDTask getFeedByIdTask = new GetFeedByIDTask(
				BaseTrackDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), id,
				getBaseOperationsBL(), Integer.valueOf(getUserID()), 0);
		asyncTaskManager.executeTask(getFeedByIdTask, getFeedByID, null, true);
	}
	private void initilizeMap() {
		if (googleMap == null) {
			mfg = (MyMapFragment) getFragmentManager().findFragmentById(
					R.id.mapView);
			// mfg.setHasOptionsMenu(true);
			mapView = mfg.getMapView();
			googleMap = mfg.getMap();
			googleMap.setMyLocationEnabled(true);
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			googleMap.setIndoorEnabled(true);
			googleMap.setLocationSource(new LocationSource() {

				@Override
				public void deactivate() {
					onLocationChangedListener = null;
				}

				@Override
				public void activate(OnLocationChangedListener listener) {
					onLocationChangedListener = listener;
				}
			});

			googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition cameraPosition) {
					if (keepCurrentLocationVisible && currentLocation != null
							&& !isLocationVisible(currentLocation)) {
						keepCurrentLocationVisible = false;
						zoomToCurrentLocation = false;
					}
				}
			});
			// check if map is created successfully or not
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
					getDefaultLatLng(), googleMap.getMinZoomLevel()));
		}
	}
	protected void init() {
		setWorkoutBL(new WorkoutBL(getHelper()));
		setUserBL(new UserBL(getHelper()));
		setBaseOperationsBL(new BaseOperationsBL(getHelper()));
		setFeedBL(new FeedBL(getHelper()));
		
		setMapOverlay(new MapOverlay(this));
		comments = new LinkedList<Comment>();
		trackDataHub = TrackDataHub.newInstance(this);
		scrollVertical = (ScrollView) findViewById(R.id.scrollVertical);
		baseShared = getSharedPreferences(com.athlete.Constants.PREFERENCES,
				Context.MODE_PRIVATE);
		layoutParamsMap = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 1);
		layoutParamsMap.addRule(RelativeLayout.BELOW, R.id.linearTopRelative);
		paramsHorisontalScroll = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 0);
		paramsHorisontalScroll.addRule(RelativeLayout.BELOW, R.id.layoutBottom);
		setmTrack(new Track());
		progressBar = (RelativeLayout) findViewById(R.id.progressBar);
		splash = (ImageView) findViewById(R.id.imVLoading);
		imageLoader = ((AthleteApplication) getApplication()).getImageLoader();
		options = ((AthleteApplication) getApplication())
				.getDisplayImageOptions();
		corner3dp = CommonHelper.getPX(3, this);
		size50dp = CommonHelper.getPX(50, this);
		size40dp = CommonHelper.getPX(40, this);

		size120dp = CommonHelper.getPX(120, this);
		size100dp = CommonHelper.getPX(100, this);
		size35dp = CommonHelper.getPX(35, this);
		// Start animating the image
		animLarge = AnimationUtils.loadAnimation(this, R.anim.anim_progressbar);
		animLarge.setInterpolator(new LinearInterpolator());
		animLarge.setRepeatCount(Animation.INFINITE);
		animLarge.setDuration(Constants.ANIM_DURATION);
		setCurrentUser(getUserBL().getBy(getUserID()));
		initTextView();
		initLayout();
		findViewById(R.id.btnBack).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});

		int feedId = getIntent().getIntExtra(Constants.INTENT_KEY.FEED_ID, -1);

        TextView txtActivityType = (TextView) findViewById(R.id.txtActivityType);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/athlete-regular-webfont.ttf");
        txtActivityType.setTypeface(typeFace);

		setmImageRunType((TextView) findViewById(R.id.txtActivityType));


		getmImageRunType().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTxtBaloon.getVisibility() == View.GONE) {
					showRunType();
				}
			}
		});
		if (feedId == -1) {
			setWorkOutID(getIntent().getIntExtra(Constants.INTENT_KEY.ID, -1));

			setWorkOut(getWorkoutBL().getBy(String.valueOf(getWorkOutID())));
            WorkOut w = getWorkOut();
			if (getWorkOut() == null || getWorkOut().getIdUser() == null) {
				finish();
				return;
			}
			setUser(getUserBL().getBy(getWorkOut().getIdUser()));

			setFeed(getWorkOut().getFeed());
			if (getFeed() != null) {
				initFeed();
			}
			if (getWorkOut().getPost() != null) {
				checkFeed(getWorkOut().getPost());
			}
		} else {
			checkFeed(String.valueOf(feedId));
			setFeed(getFeedBL().getBy(String.valueOf(feedId)));
			if (getFeed() != null) {
				setUser(getFeed().getUser());
			}
			initFeed();
		}
		setCommentList(BaseTrackDetails.this);
		if (getWorkOut() != null) {
			CommonHelper.setType(getmImageRunType(), getWorkOut(), mTxtBaloon, getResources());
		} else {
			getmImageRunType().setVisibility(View.GONE);
			imageBtnCameraGalleryNoPhoto.setVisibility(View.GONE);
			imageBtnCameraGallery.setVisibility(View.GONE);
		}
		if (getWorkOut() == null) {
			findViewById(R.id.workoutLinear).setVisibility(View.GONE);
		} else {
			findViewById(R.id.climbOrCallories).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							isClimb = !isClimb;
							setClimbOrCalories();
						}
					});
		}
	}

	private void initFeed() {
		if (getFeed() == null) {
			setFeed(getBaseOperationsBL().getFromDBByField(Feed.class, Feed.ID,
					getWorkOut().getPost()));
			if (getFeed() == null) {
				getFeedByID(getWorkOut().getPost());
			}
		} else {
			initFeedCorrect();
		}
	}

	private void initFeedCorrect() {
		if (getFeed() != null) {
			likes = new ArrayList<Integer>(getFeed().getLikers());
			comments = getWorkoutBL().getListFromDBByField(Comment.class,
					Comment.FEED_ID, getFeed().getId());
			postPictures = getWorkoutBL().getListFromDBByField(
					PostPicture.class, PostPicture.FEED_ID, getFeed().getId());
		}
		myTracksProviderUtils = MyTracksProviderUtils.Factory.get(this);
		//mapView = (MapView) findViewById(R.id.mapView);
		//setOverlays(mapView.getOverlays());
		getOverlays().clear();
		//getOverlays().add(getMapOverlay());
		//mapView.invalidate();
		getMapOverlay().setShowEndMarker(true);
		//mapView.requestFocus();
		//ApiAdapterFactory.getApiAdapter().disableHardwareAccelerated(mapView);
		initOther();
		setTextView();
		hideMap();
		setPictureList();
		addCommentBody();

		getComments(getFeed().getId());
		getPicture(String.valueOf(getFeed().getId()));
		setMetricUnits(PreferencesUtils.getMetricUnit(BaseTrackDetails.this));
		findViewById(R.id.btnDelete).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (getFeed() != null) {
							alertDialog();
						}
					}
				});
		if (getWorkOut() != null) {
			List<WorkoutM2MTrack> list = getWorkoutBL().getListFromDBByField(
					WorkoutM2MTrack.class, WorkoutM2MTrack.WORKOUT,
					getWorkOut().getId());

			findViewById(R.id.imageBtnRoute).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							goTrackRoute();
						}
					});
			findViewById(R.id.linearMapViewClick).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							goTrackRoute();
						}
					});

			findViewById(R.id.imageBtnCharts).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (getmTrack() != null
									&& getmTrack().getId() != -1) {
								startActivityForResult(
										new Intent(BaseTrackDetails.this,
												ActivitySplitsDetails.class)
												.putExtra(
														Constants.INTENT_KEY.ID,
														getmTrack().getId())
												.putExtra(
														Constants.INTENT_KEY.USER_DETAILS,
														getUser().getId())
												.putExtra(
														Constants.INTENT_KEY.WORKOUT_ID,
														getWorkOutID()),
										Constants.RESULT_CODE_DELETE);
							}
						}
					});

			if (getWorkOut().getStaticMapUrl() != null
					&& getWorkOut().getStaticMapUrl().length() != 0)

				if (list == null || list.isEmpty()
						|| list.get(list.size() - 1).getTrackID() == 0) {
					if (!isTaskGetGPX) {
						isTaskGetGPX = true;
						getWorkoutGPXByID(getWorkOutID());
					}
				} else {
					setmTrack(myTracksProviderUtils.getTrack(list.get(
							list.size() - 1).getTrackID()));
					climb = getmTrack().getTripStatistics()
							.getTotalElevationGain();
					setClimbOrCalories();
					PreferencesUtils
							.setLong(this, R.string.selected_track_id_key,
									getmTrack().getId());
					trackDataHub.loadTrack(getmTrack().getId());
					if (trackDataHub != null)
						trackDataHub.start();
					trackDataHub.registerTrackDataListener(this, EnumSet.of(
							TrackDataType.SELECTED_TRACK,

							TrackDataType.SAMPLED_IN_TRACK_POINTS_TABLE,
							TrackDataType.LOCATION, TrackDataType.HEADING));
					showTrack(getmTrack());
				}
		}

	}

	private void goTrackRoute() {
		if (getmTrack() != null && getmTrack().getId() != -1) {
			startActivity(new Intent(BaseTrackDetails.this,
					ActivityRouteDetails.class)
					.putExtra(Constants.INTENT_KEY.ID, getmTrack().getId())
					.putExtra(Constants.INTENT_KEY.USER_DETAILS,
							getUser().getId())
					.putExtra(Constants.INTENT_KEY.WORKOUT_ID, getWorkOutID()));
		}
	}

	private void alertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_discard))
				.setMessage(getString(R.string.message_remove_workout))
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								deleteWorkout();

							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		builder.create().show();
	}

	private void showProgress() {
		progressBar.setVisibility(View.VISIBLE);
		splash.startAnimation(animLarge);
	}

	private void hideProgress() {
		splash.clearAnimation();
		progressBar.setVisibility(View.GONE);
	}

	private void deleteWorkout() {
		showProgress();
		imageBtnDelete.setVisibility(View.GONE);

		OnTskCpltListener deleteWorkout = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				imageBtnDelete.setVisibility(View.VISIBLE);
				hideProgress();
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();
					if (result.getResult()) {
						delete();
					}
				} catch (Exception e) {
				}
			}
		};
		DeleteFeedTask deleteWorkoutTask = new DeleteFeedTask(
				BaseTrackDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), getFeed().getId());
		asyncTaskManager.executeTask(deleteWorkoutTask, deleteWorkout, null,
				true);
	}

	private void delete() {
		if (getWorkOut() != null) {
			getWorkoutBL().delete(getWorkOut(), WorkOut.class);
		}
		getFeedBL().delete(getFeed(), Feed.class);
		PreferencesUtils.setBoolean(BaseTrackDetails.this,
				R.string.is_delete_featured, true);
		PreferencesUtils.setBoolean(BaseTrackDetails.this,
				R.string.is_delete_friend, true);
		PreferencesUtils.setBoolean(BaseTrackDetails.this,
				R.string.is_delete_local, true);
		getUserBL().deleteByField(Feed2Type2User.FEED, Feed2Type2User.class,
				getFeed());
		finish();
		startActivity(new Intent(BaseTrackDetails.this, TabActivityMain.class)
				.putExtra(Constants.INTENT_KEY.BOOLEAN_VALUE, true).setFlags(
						Intent.FLAG_ACTIVITY_CLEAR_TOP
								| Intent.FLAG_ACTIVITY_SINGLE_TOP));
	}

	private void initLayout() {
		layoutPicture = (LinearLayout) findViewById(R.id.layoutPicture);
		layoutComment = (LinearLayout) findViewById(R.id.layoutComment);
	}

	@Override
	public void onPause() {
		super.onPause();
		pauseTrackDataHub();
	}

	private synchronized void pauseTrackDataHub() {
		if (trackDataHub != null) {
			trackDataHub.unregisterTrackDataListener(this);
			trackDataHub = null;
		}
	}

	private synchronized boolean isSelectedTrackRecording() {
		return trackDataHub != null && trackDataHub.isSelectedTrackRecording();
	}

	private void getWorkoutGPXByID(final int id) {
		OnTskCpltListener getWorkoutByID = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<WorkOut> result;
				try {
					result = (TaskResult<WorkOut>) task.get();
					if (!result.isError() && result.getResult() != null) {
						getWorkOut()
								.setTrackID(result.getResult().getTrackID());

						getWorkoutBL().createOrUpdate(
								WorkoutM2MTrack.class,
								new WorkoutM2MTrack(getWorkOut().getId(),
										getWorkOut().getTrackID()));
						setmTrack(myTracksProviderUtils.getTrack(getWorkOut()
								.getTrackID()));
						trackDataHub.loadTrack(getmTrack().getId());
						trackDataHub.registerTrackDataListener(
								BaseTrackDetails.this,
								EnumSet.of(TrackDataType.SELECTED_TRACK,

								TrackDataType.SAMPLED_IN_TRACK_POINTS_TABLE,
										TrackDataType.LOCATION,
										TrackDataType.HEADING));
						showTrack(getmTrack());
						climb = getmTrack().getTripStatistics()
								.getTotalElevationGain();
						setClimbOrCalories();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		GetWorkoutByIDTask getWorkoutTask = new GetWorkoutByIDTask(
				BaseTrackDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), id, true);
		asyncTaskManager
				.executeTask(getWorkoutTask, getWorkoutByID, null, true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (trackDataHub != null) {
			trackDataHub.start();
		}
	}

	private void initOther() {
		paramsAvaForward = new LinearLayout.LayoutParams(size35dp, size35dp);
		roundedImageView = (RoundedImageView) findViewById(R.id.imAvaComment);
		roundedImageView.setCornerRadius(corner3dp, size50dp);

		imageBtnCameraGalleryNoPhoto = (ImageButton) findViewById(R.id.imageBtnCameraGallery2);
		imageBtnCameraGallery = (ImageButton) findViewById(R.id.imageBtnCameraGallery);

		imageBtnCameraGalleryNoPhoto
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						addPhoto();
					}
				});
		imageBtnCameraGallery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addPhoto();
			}
		});
		imageBtnDelete = (ImageButton) findViewById(R.id.btnDelete);

		if (getFeed() == null || getFeed().getUser() == null
				|| !getFeed().getUser().getId().equalsIgnoreCase(getUserID())) {
			imageBtnDelete.setVisibility(View.GONE);
			imageBtnCameraGalleryNoPhoto.setVisibility(View.GONE);
			imageBtnCameraGallery.setVisibility(View.GONE);
		}
		edTxtComment = (EditText) findViewById(R.id.editTextComment);
		horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
		if (getCurrentUser().getProfileImage225url().startsWith("http")) {
			imageLoader.displayImage(getCurrentUser().getProfileImage225url(),
					roundedImageView, options);
		} else {
			roundedImageView.setImageResource(R.drawable.avatar);
		}

		edTxtComment
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						boolean handled = false;
						if (actionId == EditorInfo.IME_ACTION_SEND) {
							String commentStr = edTxtComment.getText()
									.toString();
							if (commentStr != null & commentStr.length() > 0) {
								AnalyticsUtils
										.sendPageViews(
												BaseTrackDetails.this,
												AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FEED_DETAILS,
												AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
												AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
												AnalyticsUtils.GOOGLE_ANALYTICS.COMMENT,
												0);
								Comment comment = new Comment();
								comment.setUser(getCurrentUser());
								comment.setFeedId(getFeed().getId());
								comment.setComment(edTxtComment.getText()
										.toString());
								comments.add(comment);
								setCommentList(BaseTrackDetails.this);
								addComment(comment, comments.size());
								edTxtComment.setText("");
								edTxtComment.clearFocus();
								InputMethodManager imm = (InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(getCurrentFocus()
										.getWindowToken(), 0);
								handled = true;
							}
						}
						return handled;
					}
				});
	}

	protected void addPhoto() {
		addPictureMode();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			if (getFeed() != null) {
				handleBigCameraPhoto(roundedImageView,
						String.valueOf(getFeed().getId()));
			}
		} else if (requestCode == CAMERA_GALLERY && resultCode == RESULT_OK) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			if (cursor == null || getFeed() == null)
				return;
			else {
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				setCurrentPhotoPath(filePath);
				handleBigCameraPhoto(roundedImageView,
						String.valueOf(getFeed().getId()));
			}
		}
		if (resultCode == Constants.RESULT_CODE_DELETE) {
			finish();
		}
	}

	private void initTextView() {
		mTxtTitle = (TextView) findViewById(R.id.txtTitle);
		mTxtCountLikes = (TextView) findViewById(R.id.txtCountLikes);
		mTxtTitleRoute = (TextView) findViewById(R.id.txtTitleRoute);
		mTxtTimeRoute = (TextView) findViewById(R.id.txtTimeRoute);
		mTxtBaloon = (TextView) findViewById(R.id.txtBallon);

		mTxtTime = (TextView) findViewById(R.id.txtTime);
		mTxtTimeLabel = (TextView) findViewById(R.id.txtTimeLabel);
		mTxtClimbOrCalories = (TextView) findViewById(R.id.txtClimbOrCalories);
		mTxtClimbOrCaloriesLabel = (TextView) findViewById(R.id.txtClimbOrCaloriesLabel);
	}

	protected void setTextView() {
		setTitleTxt();
		setTxtLikes();
		if (getWorkOut() != null) {
			mTxtTitleRoute.setText(getWorkOut().getTitle());
		} else {
			mTxtTitleRoute.setTypeface(null, Typeface.NORMAL);
			mTxtTitleRoute.setText(getFeed().getBody());
		}
		if (getFeed() != null) {
			mTxtTimeRoute.setText(CommonHelper.getMonthDayDotTime(getFeed()
					.getCreatedDate(), BaseTrackDetails.this));
		}
		if (getWorkOut() != null) {

			mTxtTime.setText(StringUtils.formatElapsedTime((long) (getWorkOut()
					.getDuration() * Constants.ONE_SECOND)));

			if (mTxtTime.getText().toString().length() > 3) {
				mTxtTimeLabel.setText(getString(R.string.h_m_s));
			} else {
				mTxtTimeLabel.setText(getString(R.string.m_s));
			}
			// distance
			StatsUtils.setDistanceValue(BaseTrackDetails.this,
					R.id.txtDistance, R.id.txtDistanceUnit, getWorkOut()
							.getDistance(), isMetricUnits());
			// avg pace
			StatsUtils.setSpeedValue(BaseTrackDetails.this, R.id.txtAvgPace,
					getWorkOut().getDistance() / getWorkOut().getDuration(),
					isMetricUnits(), false);
		} else {
			mTxtTime.setVisibility(View.GONE);
			mTxtTimeLabel.setVisibility(View.GONE);
			findViewById(R.id.txtDistanceUnit).setVisibility(View.GONE);
			findViewById(R.id.txtAvgPace).setVisibility(View.GONE);
		}

	}

	private void setTitleTxt() {
		if (getUser() != null) {
			mTxtTitle.setText(getUser().getFirstName() + " "
					+ getUser().getLastName());
		}
	}

	private void setTxtLikes() {
		StringBuffer buffer = new StringBuffer();
		if (likes.contains(Integer.valueOf(getUserID()))) {
			buffer.append("<font color='#54A1C7'>" + getString(R.string.unlike)
					+ "</font>");
		} else {
			buffer.append("<font color='#54A1C7'>" + getString(R.string.like)
					+ "</font>");
		}
		if (getFeed() != null && likes != null && likes.size() != 0) {
			buffer.append("<font color='#ACABAA'> (</font>"
					+ String.valueOf(likes.size())
					+ "<font color='#ACABAA'>)</font>");

		}
		mTxtCountLikes.setText(Html.fromHtml(buffer.toString()));
		mTxtCountLikes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendLike();

			}
		});

	}

	private void getComments(int feedId) {

		OnTskCpltListener getComments = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {

				TaskResult<List<Comment>> result;
				try {
					result = (TaskResult<List<Comment>>) task.get();
					if (!result.isError() && result.getResult().size() > 0) {
						comments = result.getResult();
						addCommentBody();
						setCommentList(BaseTrackDetails.this);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		GetCommentListTask commentsTask = new GetCommentListTask(
				BaseTrackDetails.this, getBaseOperationsBL(), getURLHost(),
				getPublicKey(), getPrivateKey(), getUserName(), getApikey(),
				feedId);
		asyncTaskManager.executeTask(commentsTask, getComments, null, true);
	}

	private void addComment(Comment comment, final int position) {
		showProgress();
		OnTskCpltListener addComment = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {
				hideProgress();
				TaskResult<Comment> result;
				try {
					result = (TaskResult<Comment>) task.get();
					if (!result.isError()) {
						if (result.getResult() != null) {
							getBaseOperationsBL().createOrUpdate(Comment.class,
									result.getResult());
							setCommentList(BaseTrackDetails.this);
						}
					} else {
						comments.remove(position);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		SendCommentTask sendCommentsTask = new SendCommentTask(
				BaseTrackDetails.this, getURLHost(), getPublicKey(),
				getPrivateKey(), getUserName(), getApikey(), comment);
		asyncTaskManager.executeTask(sendCommentsTask, addComment, null, true);
	}

	private void setClimbOrCalories() {
		if (isClimb) {
			StatsUtils.setElevationValue(BaseTrackDetails.this,
					R.id.txtClimbOrCalories, climb, isMetricUnits());

		} else {
			mTxtClimbOrCalories.setText(String.valueOf(getWorkOut()
					.getCalories()));
		}
		setClimbOrCaloriesLabel();
	}

	private void showRunType() {

		final int hungred = 100;
		final long SPLASH_TIME = 200;

		mTxtBaloon.setVisibility(View.VISIBLE);
		AsyncTask<Void, Void, Void> splashTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					int waited = 0;
					while ((waited < SPLASH_TIME)) {
						Thread.sleep(Constants.ONE_SECOND);
						waited += hungred;
					}
				} catch (InterruptedException e) {
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				mTxtBaloon.setVisibility(View.GONE);
			}
		};
		splashTask.execute((Void[]) null);
	}

	private void setClimbOrCaloriesLabel() {
		StringBuffer buffer = new StringBuffer();
		if (isClimb) {
			if (isMetricUnits()) {
				buffer.append(getString(R.string.meter));
			} else {
				buffer.append(getString(R.string.ft));
			}
			buffer.append(" " + getString(R.string.climb));
		} else {
			buffer.append(getString(R.string.calories));
		}
		mTxtClimbOrCaloriesLabel.setText(buffer.toString());
	}

	private void sendLike() {
		AnalyticsUtils.sendPageViews(BaseTrackDetails.this,
				AnalyticsUtils.GOOGLE_ANALYTICS.SCREEN.FEED_DETAILS,
				AnalyticsUtils.GOOGLE_ANALYTICS.CATEGORY,
				AnalyticsUtils.GOOGLE_ANALYTICS.ACTION,
				AnalyticsUtils.GOOGLE_ANALYTICS.LIKE, 0);

		OnTskCpltListener sendLike = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {

				TaskResult<String> result;
				try {
					result = (TaskResult<String>) task.get();

					if (!result.isError()) {
						if (likes.contains(Integer.valueOf(getCurrentUser()
								.getId()))) {
							likes.remove(Integer.valueOf(getCurrentUser()
									.getId()));
						} else {
							likes.add(Integer.valueOf(getCurrentUser().getId()));
						}
						getFeed().setLikers(likes);
						getFeedBL().createOrUpdate(getFeed());
						setTxtLikes();
					}
				} catch (Exception e) {
				}
			}
		};

		SendLikeTask sendLikeTask = new SendLikeTask(BaseTrackDetails.this,
				getURLHost(), getPublicKey(), getPrivateKey(), getUserName(),
				getApikey(), getFeed().getId(), Constants.OBJECT_TYPE.POST);

		asyncTaskManager.executeTask(sendLikeTask, sendLike, null, true);
	}

	@Override
	public void onLocationStateChanged(LocationState locationState) {

	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onHeadingChanged(double heading) {
		if (getMapOverlay().setHeading((float) heading)) {
			mapView.postInvalidate();
		}

	}

	@Override
	public void onSelectedTrackChanged(final Track track) {
		/*BaseTrackDetails.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				boolean hasTrack = track != null;
				getMapOverlay().setTrackDrawingEnabled(hasTrack);
				if (hasTrack) {
					synchronized (this) {
						updateMap(track);
					}
					getMapOverlay().setShowEndMarker(
							!isSelectedTrackRecording());
				}
				mapView.invalidate();
			}
		});*/
		currentTrack = track;
		boolean hasTrack = track != null;
		if (hasTrack) {
			mapOverlay.setShowEndMarker(!isSelectedTrackRecording());
			synchronized (this) {
				if (track.getId() == markerTrackId) {
					showMarker(markerId);
					markerTrackId = -1L;
					markerId = -1L;
				} else {
					// Show the track
					showTrack(currentTrack);
				}
			}
		}

	}

	@Override
	public void onTrackUpdated(Track track) {

	}

	@Override
	public void onSampledOutTrackPoint(Location location) {

	}

	@Override
	public void onSegmentSplit(Location location) {
		getMapOverlay().addSegmentSplit();

	}

	@Override
	public void onNewTrackPointsDone() {
		mapView.postInvalidate();

	}

	@Override
	public boolean onMetricUnitsChanged(boolean metricUnits) {

		return false;
	}

	@Override
	public boolean onReportSpeedChanged(boolean reportSpeed) {

		return false;
	}

	@Override
	public boolean onMinRecordingDistanceChanged(int minRecordingDistance) {

		return false;
	}

	private void updateMap(Track track) {
        showTrack(track);
	}

}
