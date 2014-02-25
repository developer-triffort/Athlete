package com.athlete.activity.track.details;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.athlete.AthleteApplication;
import com.athlete.Constants;
import com.athlete.R;
import com.athlete.activity.user.ActivityUserDetails;
import com.athlete.bl.BaseOperationsBL;
import com.athlete.bl.FeedBL;
import com.athlete.bl.UserBL;
import com.athlete.bl.WorkoutBL;
import com.athlete.control.RoundedImageView;
import com.athlete.google.android.apps.mytracks.MapOverlay;
import com.athlete.google.android.apps.mytracks.content.Track;
import com.athlete.google.android.apps.mytracks.content.TrackDataHub;
import com.athlete.google.android.apps.mytracks.services.TrackRecordingServiceConnection;
import com.athlete.model.Comment;
import com.athlete.model.Feed;
import com.athlete.model.PostPicture;
import com.athlete.model.TaskResult;
import com.athlete.model.User;
import com.athlete.model.WorkOut;
import com.athlete.services.AsyncTaskManager;
import com.athlete.services.BaseTask;
import com.athlete.services.OnTskCpltListener;
import com.athlete.services.task.SendPicturePostTask;
import com.athlete.services.task.get.GetPicturePostTask;
import com.google.android.maps.Overlay;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DecodingType;

public class AddPhotoAndInitActivity extends BaseActivityMap {
	protected static final int CAMERA_REQUEST = 1888;
	protected static final int CAMERA_GALLERY = 2;
	private String mCurrentPhotoPath;

	protected static final String JPEG_FILE_PREFIX = "IMG_";
	protected static final String JPEG_FILE_SUFFIX = ".PNG";
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private File file;

	// True to keep my location visible.
	private boolean keepMyLocationVisible, metricUnits;
	private Track mTrack;
	private int workOutID;
	private WorkOut workOut;
	private Feed feed;
	private User user;
	private User currentUser;
	private WorkoutBL workoutBL;
	private UserBL userBL;
	private FeedBL feedBL;
	private BaseOperationsBL baseOperationsBL;
	private List<Overlay> overlays;
	private TextView mImageRunType;
	protected HorizontalScrollView horizontalScrollView;
	protected LinearLayout layoutPicture, layoutComment;
	protected ImageButton imageBtnCameraGalleryNoPhoto, imageBtnCameraGallery,
			imageBtnDelete;
	protected EditText edTxtComment;
	protected TextView mTxtTitle, mTxtCountLikes, mTxtTitleRoute,
			mTxtTimeRoute, mTxtBaloon, mTxtTime, mTxtTimeLabel,
			mTxtClimbOrCalories, mTxtClimbOrCaloriesLabel;
	protected RelativeLayout.LayoutParams layoutParamsMap;
	protected List<Comment> comments;
	protected List<PostPicture> postPictures;
	protected TrackRecordingServiceConnection trackRecordingServiceConnection;
	protected TrackDataHub trackDataHub;
	protected ImageLoader imageLoader;
	protected DisplayImageOptions options;
	protected int corner3dp;
	protected int size50dp, size35dp, size40dp, size100dp, size120dp;
	protected SharedPreferences baseShared;
	protected LinearLayout.LayoutParams paramsAvaForward;
	protected RelativeLayout.LayoutParams paramsHorisontalScroll;
	protected RoundedImageView roundedImageView;

	protected RelativeLayout progressBar;
	protected ImageView splash;
	protected Animation animLarge;
	// The current location. Set in onCurrentLocationChanged.
	protected Location currentLocation;
	// True to zoom to my location. Only apply when keepMyLocationVisible is
	// true.
	protected boolean zoomToMyLocation;
	protected ScrollView scrollVertical;
	protected double climb;
	protected AsyncTaskManager asyncTaskManager;
	protected boolean isClimb;

	protected void hideMap() {
		if (getWorkOut() == null || getWorkOut().getStaticMapUrl() == null
				|| getWorkOut().getStaticMapUrl().length() == 0) {

			mapView.setLayoutParams(layoutParamsMap);
			mapView.setVisibility(View.INVISIBLE);
			findViewById(R.id.layoutTop).setVisibility(View.INVISIBLE);
			findViewById(R.id.imageBtnRoute).setVisibility(View.INVISIBLE);
			findViewById(R.id.imageBtnCharts).setVisibility(View.INVISIBLE);
			findViewById(R.id.layoutBottom).setVisibility(View.INVISIBLE);
			findViewById(R.id.linearMapViewClick).setVisibility(View.GONE);
		}

		if (postPictures == null || postPictures.size() == 0) {
			horizontalScrollView.setLayoutParams(paramsHorisontalScroll);
		}
	}

	protected void setCommentList(final Context ctx) {
		if (comments != null && getFeed() != null) {
			{
				layoutComment.removeAllViews();
			}

			for (final Comment comment : comments) {
				View view = getLayoutInflater().inflate(R.layout.item_comment,
						null);
				if (comment.getId() == 0) {
					view.setBackgroundColor(Color.WHITE);
				} else {
					view.setBackgroundColor(getResources().getColor(
							R.color.comment_color));
				}
				RoundedImageView roundedImageView = (RoundedImageView) view
						.findViewById(R.id.imViewAva);
				TextView txtFullName = (TextView) view
						.findViewById(R.id.txtFullName);
				TextView txtBody = (TextView) view.findViewById(R.id.txtBody);
				roundedImageView.setCornerRadius(corner3dp, size40dp);
				if (comment.getUser() != null) {
					imageLoader
							.displayImage(comment.getUser()
									.getProfileImage225url(), roundedImageView,
									options);
					roundedImageView
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									startActivity(new Intent(ctx,
											ActivityUserDetails.class)
											.putExtra(
													Constants.INTENT_KEY.USER_DETAILS,
													comment.getUser().getId()));
								}
							});
					txtFullName.setText(comment.getUser().getFirstName() + " "
							+ comment.getUser().getLastName());
				}
				txtBody.setText(comment.getComment());
				layoutComment.addView(view);
			}
			if (comments.size() != getFeed().getCommentCount()) {
				getFeed().setCommentCount(comments.size());
				getFeedBL().createOrUpdate(getFeed());
			}
		}
	}

	protected void addCommentBody() {
		if (getWorkOut() != null && comments != null
				&& getWorkOut().getPostBody() != null
				&& getWorkOut().getPostBody().length() > 0) {
			Comment workoutBody = new Comment();
			workoutBody.setUser(getUser());
			if (getWorkOut().getPostBody().equalsIgnoreCase("null")) {
				workoutBody.setComment("None");
			} else {
				workoutBody.setComment(getWorkOut().getPostBody());
			}
			comments.add(0, workoutBody);
		}
	}

	private void addPhoto() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = null;
		try {
			f = setUpPhotoFile();
			setCurrentPhotoPath(f.getAbsolutePath());
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			e.printStackTrace();
			f = null;
			setCurrentPhotoPath(null);
		}
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}

	protected String getUserName() {
		return getSharedPreferences(com.athlete.Constants.PREFERENCES,
				Context.MODE_PRIVATE).getString(
				Constants.SharedPreferencesKeys.USER_NAME, null);
	}

	protected String getUserID() {
		return getSharedPreferences(com.athlete.Constants.PREFERENCES,
				Context.MODE_PRIVATE).getString(
				Constants.SharedPreferencesKeys.CURRENT_ID, null);
	}

	protected String getApikey() {
		return getSharedPreferences(com.athlete.Constants.PREFERENCES,
				Context.MODE_PRIVATE).getString(
				Constants.SharedPreferencesKeys.API_KEY, null);
	}

	protected File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		setCurrentPhotoPath(f.getAbsolutePath());

		return f;
	}

	protected File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		return imageF;
	}

	protected String getAlbumName() {
		return getString(R.string.album_name);
	}

	protected File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			storageDir = getAlbumStorageDirFactory().getAlbumStorageDir(
					getAlbumName());
			if (storageDir != null && !storageDir.mkdirs()
					&& !storageDir.exists()) {
				Log.d("CameraSample", "failed to create directory");
				return null;
			}
		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	private void addGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, CAMERA_GALLERY);
	}

	protected void handleBigCameraPhoto(RoundedImageView mImageView,
			String postID) {

		if (getCurrentPhotoPath() != null) {
			// setPic(mImageView);
			galleryAddPic(postID);
			setCurrentPhotoPath(null);
		}

	}

	private void galleryAddPic(String postID) {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(getCurrentPhotoPath());
		setFile(f);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
		uploadPhoto(postID);
	}

	private void uploadPhoto(final String postID) {

		OnTskCpltListener upload = new OnTskCpltListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onTaskComplete(
					@SuppressWarnings("rawtypes") BaseTask task) {
				TaskResult<Boolean> result;
				try {
					result = (TaskResult<Boolean>) task.get();

					if (result.getResult()) {
						getPicture(postID);
					}
				} catch (Exception e) {

				}
			}
		};
		SendPicturePostTask sendPhotoTask = new SendPicturePostTask(
				AddPhotoAndInitActivity.this,
				getString(R.string.lbl_please_wait), getFile(), getURLHost(),
				getPublicKey(), getPrivateKey(), getApikey(), getUserName(),
				postID);
		((AthleteApplication) getApplication()).getTaskManager(
				AddPhotoAndInitActivity.this).executeTask(sendPhotoTask,
				upload, null, false);
	}

	protected void getPicture(String feedId) {

		OnTskCpltListener getPicture = new OnTskCpltListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onTaskComplete(BaseTask task) {

				TaskResult<List<PostPicture>> result;
				try {
					result = (TaskResult<List<PostPicture>>) task.get();

					if (!result.isError() && result.getResult().size() > 0) {
						postPictures = result.getResult();
						setPictureList();
					}
				} catch (Exception e) {
				}
			}
		};

		GetPicturePostTask picturePostTask = new GetPicturePostTask(
				AddPhotoAndInitActivity.this,
				new BaseOperationsBL(getHelper()), getURLHost(),
				getPublicKey(), getPrivateKey(), getUserName(), getApikey(),
				feedId);
		((AthleteApplication) getApplication()).getTaskManager(
				AddPhotoAndInitActivity.this).executeTask(picturePostTask,
				getPicture, null, true);
	}

	protected void setPictureList() {
		DisplayImageOptions optionsGallery = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.manual_run_icon).cacheInMemory()
				.cacheOnDisc().decodingType(DecodingType.MEMORY_SAVING).build();
		if (postPictures != null && !postPictures.isEmpty()) {
			imageBtnCameraGalleryNoPhoto.setVisibility(View.GONE);
			paramsHorisontalScroll = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, size120dp);
			paramsHorisontalScroll.addRule(RelativeLayout.BELOW,
					R.id.layoutBottom);
			horizontalScrollView.setLayoutParams(paramsHorisontalScroll);
			layoutPicture.removeAllViews();
			int i = 0;
			for (final PostPicture postPicture : postPictures) {
				final View view = getLayoutInflater().inflate(
						R.layout.item_feed_picture, null);
				RoundedImageView roundedImageView = (RoundedImageView) view
						.findViewById(R.id.imViewPicture);
				final int j = i;
				roundedImageView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(AddPhotoAndInitActivity.this,
								ActivityGallery.class)
								.putExtra(Constants.INTENT_KEY.FEED_ID,
										getFeed().getId()).putExtra(
										Constants.INTENT_KEY.ID, j));
					}
				});
				roundedImageView.setCornerRadius(corner3dp, size100dp);
				imageLoader.displayImage(postPicture.getFeed(),
						roundedImageView, optionsGallery);
				layoutPicture.addView(view);
				i++;
			}
		}
	}

	protected void addPictureMode() {

		final String[] items = getAvaMode();
		AlertDialog.Builder builder = new AlertDialog.Builder(
				AddPhotoAndInitActivity.this);

		builder.setSingleChoiceItems(items, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						if (item == 0) {
							addPhoto();
						} else {
							addGallery();
						}
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private String[] getAvaMode() {
		String[] result = new String[] { getString(R.string.lbl_attach_photo),
				getString(R.string.lbl_attach_gallery) };
		return result;
	}

	public String getCurrentPhotoPath() {
		return mCurrentPhotoPath;
	}

	public void setCurrentPhotoPath(String mCurrentPhotoPath) {
		this.mCurrentPhotoPath = mCurrentPhotoPath;
	}

	public AlbumStorageDirFactory getAlbumStorageDirFactory() {
		return mAlbumStorageDirFactory;
	}

	public void setAlbumStorageDirFactory(
			AlbumStorageDirFactory mAlbumStorageDirFactory) {
		this.mAlbumStorageDirFactory = mAlbumStorageDirFactory;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public MapOverlay getMapOverlay() {
		return mapOverlay;
	}

	public void setMapOverlay(MapOverlay mapOverlay) {
		this.mapOverlay = mapOverlay;
	}

	public boolean isKeepMyLocationVisible() {
		return keepMyLocationVisible;
	}

	public void setKeepMyLocationVisible(boolean keepMyLocationVisible) {
		this.keepMyLocationVisible = keepMyLocationVisible;
	}

	public boolean isMetricUnits() {
		return metricUnits;
	}

	public void setMetricUnits(boolean metricUnits) {
		this.metricUnits = metricUnits;
	}

	public Track getmTrack() {
		return mTrack;
	}

	public void setmTrack(Track mTrack) {
		this.mTrack = mTrack;
	}

	public int getWorkOutID() {
		return workOutID;
	}

	public void setWorkOutID(int workOutID) {
		this.workOutID = workOutID;
	}

	public WorkOut getWorkOut() {
		return workOut;
	}

	public void setWorkOut(WorkOut workOut) {
		this.workOut = workOut;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public WorkoutBL getWorkoutBL() {
		return workoutBL;
	}

	public void setWorkoutBL(WorkoutBL workoutBL) {
		this.workoutBL = workoutBL;
	}

	public UserBL getUserBL() {
		return userBL;
	}

	public void setUserBL(UserBL userBL) {
		this.userBL = userBL;
	}

	public FeedBL getFeedBL() {
		return feedBL;
	}

	public void setFeedBL(FeedBL feedBL) {
		this.feedBL = feedBL;
	}

	public BaseOperationsBL getBaseOperationsBL() {
		return baseOperationsBL;
	}

	public void setBaseOperationsBL(BaseOperationsBL baseOperationsBL) {
		this.baseOperationsBL = baseOperationsBL;
	}

	public List<Overlay> getOverlays() {
		return overlays;
	}

	public void setOverlays(List<Overlay> overlays) {
		this.overlays = overlays;
	}

	public TextView getmImageRunType() {
		return mImageRunType;
	}

	public void setmImageRunType(TextView mImageRunType) {
		this.mImageRunType = mImageRunType;
	}

}

abstract class AlbumStorageDirFactory {
	public abstract File getAlbumStorageDir(String albumName);
}

final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

	// Standard storage location for digital camera files
	private static final String CAMERA_DIR = "/dcim/";

	@Override
	public File getAlbumStorageDir(String albumName) {
		return new File(Environment.getExternalStorageDirectory() + CAMERA_DIR
				+ albumName);
	}
}

final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {

	@Override
	public File getAlbumStorageDir(String albumName) {
		// TODO Auto-generated method stub
		return new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				albumName);
	}
}
