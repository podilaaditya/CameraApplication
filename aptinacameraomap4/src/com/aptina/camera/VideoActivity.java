package com.aptina.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aptina.R;
import com.aptina.camera.components.ModeSelector;
import com.aptina.camera.components.ShutterButton;
import com.aptina.camera.components.SizeHolder;
import com.aptina.camera.components.ThumbnailControl;
import com.aptina.camera.eventlisteners.PanelSlideListener;
import com.aptina.camera.eventlisteners.ResolutionChangeListener;
import com.aptina.camera.eventlisteners.SlideGestureListener;
import com.aptina.camera.eventlisteners.VideoCompositeListener;
import com.aptina.camera.eventlisteners.VideoCompositeListener.VideoGestureInterface;
import com.aptina.logger.Logger;
import com.aptina.miscellaneous.DateTimeUtils;
import com.aptina.miscellaneous.FileUtils;
import com.aptina.miscellaneous.PreferencesProvider;


/**
 * Implements main camera preview activity.
 */
@SuppressLint("NewApi")
public class VideoActivity extends Activity{

	/**
	 * TAG of this activity for logcat
	 */
	private static final String TAG = " VideoActivity";


    /**
     * Message type: update record time.
     */
    private static final int UPDATE_RECORD_TIME = 58643;
    /**
     * Message type: update video snapshot thumbnail
     */
    private static final int UPDATE_SNAPSHOT_THUMBNAIL = 68644;

    /**
     * Minimal available size on SD card. 20Mb in bytes. 
     */
    private static final long MINIMAL_AVAILABLE_CARD_SIZE_TO_SHOW_MESSAGE = 20*1024*1024L;

    /**
     * Minimal available size on SD card to start recording of new video. (1.5 mb).
     */
    private static final long MINIMAL_AVAILABLE_CARD_SIZE_TO_START_RECORD = 3*512*1024L;

    /**
     * Current date holder for KPI.
     */
    private Date mCurrentDate;

    /**
     * Previous date holder for KPI.
     */
    private Date mPreviousDate;

    /**
     * Camera preview view.
     */
    private SurfaceView mPreview = null;

    /**
     * Preview holder.
     */
    private SurfaceHolder mPreviewHolder = null;
    
    /**
     * Global variable for the parent of all the video.xml other views
     */
	private RelativeLayout mainVideoFrame;
	
	/**
	 * The surface of the preview
	 */
	private SurfaceView previewSurface;
	
	/**
	 * The time it takes for the preview screen to expand to 
	 * full screen after DVS has been turned on, in ms.
	 */
	private final int DVS_ANIMATION_TIME = 1000;
    /**
     * Variable to hold the current width(x) and height(y) of the android display
     */
    private Point dispDim = new Point();

    /**
     * Camera instance.
     */
    private Camera mCamera = null;

    /**
     * Media recorder instance.
     */
    private MediaRecorder mMediaRecorder;

    /**
     * Camcoder profile.
     */
    private CamcorderProfile[] mCamcorderProfiles;

    /**
     * In preview flag.
     */
    private boolean mPreviewing = false;

    /**
     * SD card availability flag.
     */
    private boolean mIsSdCardAvailable = false;

    /**
     * Video is in progress flag.
     */
    private static boolean mInProgress = false;

    /**
     * Left settings panel.
     */
    private RelativeLayout mPanel = null;

    /**
     * Left panel image.
     */
    private ImageView mSliderHandle = null;

    /**
     * Button to make photos.
     */
    private ShutterButton mShutterButton;

    /**
     * Thumbnail control.
     */
    private ThumbnailControl mGallery;

    /**
     * Switches between front/back cameras.
     */
    private ImageView mCameraChanger;

    /**
     * Resolution buttons.
     */
    private View mVideoResolutionButton = null;
    private View mSnapResolutionButton = null;
    /**
     * Chosen resolution drawer.
     */
    private SizeHolder mVideoResolutionValue = null;
    private SizeHolder mSnapResolutionValue = null;
    
    /**
     * PackageManager to check what gallery packages there are to use
     */
    private PackageManager myPackMan = null;
    /**
     * Resolution panel buttons listeners.
     */
    private ResolutionChangeListener mVideoResolutionChangeListener = null;
    private ResolutionChangeListener mSnapResolutionChangeListener = null;


    /**
     * Still/video Switch.
     */
    private View mSwitchButton;




    /**
     * Mode selector view.
     */
    private ModeSelector mVideoModeSelector = null;
    private ModeSelector mSnapModeSelector = null;

    /**
     * Record indicator.
     */
    private LinearLayout recordIndicator;

    /**
     * Textview to display record time.
     */
    private static TextView recordingText;

    /**
     * Surface holder callback.
     */
    private CustomSurfaceHolderCallback mPreviewHolderCallback = null;

    /**
     * Current camera index.
     */
    private int mCurrentCameraIndex = -1;

    /**
     * Array of camera parameters.
     */
    private Camera.Parameters[] mCurrentCameraParameters = null;

    /**
     * Array of default camera parameters.
     */
    private Camera.Parameters[] mDefaultCameraParameters = null;

    /**
     * Reset button view.
     */
    private View mResetButton; 

    /**
     * Text message.
     */
    private TextView mTextMessage;

    /**
     * Time when video recording was started.
     */
    private static long mRecordStartTime;

    /**
     * Last video file name.
     */
    private String currentFileName;

    /**
     * Handles messages inside the application.
     */
    private static VideoHandler mHandler;

    /**
     * Camera active mode.
     */
    private String mActiveMode;

    /**
     * Indicates that preview surface view was destroyed.
     */
    private boolean mSurfaceWasDestroyed = true;

    
    private boolean mSnapshotInProgress = false;
    
    private boolean mVideoRecordingInProgress = false;

    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
		
		

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if(mVideoRecordingInProgress){
				mSnapshotInProgress = false;
				new SavePhotoAction().execute(data);
			}

			mHandler.sendEmptyMessage(UPDATE_SNAPSHOT_THUMBNAIL);
			mCamera.startPreview();
			
			if(runningTest && signal != null){
            	signal.countDown();
            }
	
		}
	};

	private VideoCompositeListener videoGestureListener = new VideoCompositeListener();
	public VideoCompositeListener getVideoGestureListener(){
		return videoGestureListener;
	}
	//DVS mode of the camera on activity onCreate
	private String mDVSMode = CameraInfo.VIDEO_MODE_DVS_OFF;
	private VideoGestureInterface mVideoGestureInterface = new VideoGestureInterface(){

		@Override
		public void OnSlideGesture(int slide_dir) {
			switch(slide_dir){
			case SlideGestureListener.SLIDE_RIGHT:
				if(mDVSMode != CameraInfo.VIDEO_MODE_DVS_HIGH){
					switchToDVSMode(CameraInfo.VIDEO_MODE_DVS_HIGH);
					Toast.makeText(getApplicationContext(), "Setting DVS to High", Toast.LENGTH_SHORT).show();
				}
				break;
			case SlideGestureListener.SLIDE_DOWN:
				if(mDVSMode != CameraInfo.VIDEO_MODE_DVS_OFF){
					switchToDVSMode(CameraInfo.VIDEO_MODE_DVS_OFF);
					Toast.makeText(getApplicationContext(), "Setting DVS to Off", Toast.LENGTH_SHORT).show();
				}
				break;
			case SlideGestureListener.SLIDE_LEFT:
				if(mDVSMode != CameraInfo.VIDEO_MODE_DVS_LOW){
					switchToDVSMode(CameraInfo.VIDEO_MODE_DVS_LOW);
					Toast.makeText(getApplicationContext(), "Setting DVS to Low", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			
		}

		@Override
		public void OnDoubleTapGesture(MotionEvent event) {
			Log.i(TAG,"onVideoSnapshotGesture()");
			if (mSnapshotInProgress || (!mVideoRecordingInProgress) || !snapshot_supported_at_resolution) { 
				Log.w(TAG, "Cannot take video snapshot");
				return;
			}
			if (mCamera != null  && mVideoRecordingInProgress) {
				Log.i(TAG, "Taking video snapshot");
				mCamera.takePicture(null, null, null, mJpegCallback);
			}else{
				Log.i(TAG, "Not recording, no picture taking");
			}
			
		}

		@Override
		public void OnSmileGesture() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void OnFrownDetected() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void OnZoomGesture(float zoom_scale) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Camera getCamera() {
			// TODO Auto-generated method stub
			return null;
		}


		
	};
	
	private void switchToDVSMode(String dvsMode){
    	RelativeLayout sv = (RelativeLayout) findViewById(R.id.main_video_frame);

    	ViewGroup.LayoutParams sv_p = sv.getLayoutParams();
    	SurfaceView previewSurface = (SurfaceView) findViewById(R.id.preview);
       	ViewGroup.LayoutParams preview_p = previewSurface.getLayoutParams();
       	
       	//TODO need to plug in the DVS camera parameters, waiting on bangalore
       	if(preview_p.height == ViewGroup.LayoutParams.MATCH_PARENT &&
       			preview_p.width == ViewGroup.LayoutParams.MATCH_PARENT){
       		preview_p.width = (int)(sv_p.width * 0.75);
        	preview_p.height = (int)(sv_p.height * 0.75);

        	previewSurface.setLayoutParams(preview_p);
        	animatePreviewExpanansion(0.75f,1f);
       	}else{
       		preview_p.width = ViewGroup.LayoutParams.MATCH_PARENT;
        	preview_p.height = ViewGroup.LayoutParams.MATCH_PARENT;

        	previewSurface.setLayoutParams(preview_p);
       	}
    	
		mDVSMode = dvsMode;


		
	}
//	private OnTouchListener videoSnapshotListener = new OnTouchListener(){
//
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			if (mSnapshotInProgress || (!mVideoRecordingInProgress) || !snapshot_supported_at_resolution) { //GIRSH: Dont process if snap is ongoing or recording is not on
//				return false;
//			}
//			if (mCamera != null && ( event != null ) &&(event.getAction() == MotionEvent.ACTION_DOWN) && mVideoRecordingInProgress) {
//				mCamera.takePicture(null, null, null, mJpegCallback);
//				
//				
//				
//			}else{
//				Log.i(TAG, "Not recording, no picture taking");
//			}
//			return false;
//		}
//		
//	};

    /**
     * Gallery view click listener.
     */
    private OnClickListener mGalleryListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	try {
                Uri uri = mGallery.getUri();
                Intent showImageOrVideo = null;
                List<ApplicationInfo> packs = myPackMan.getInstalledApplications(0);        
                if (uri != null) {
                	//Check to see what activities can display our images, use default android if non of them exist/are-installed
                    if(isPackageExists("com.cooliris.media", packs)){
                  		showImageOrVideo = new Intent("com.cooliris.media.action.REVIEW", uri);
                  	}else if(isPackageExists("com.htc.album", packs)){
                  		showImageOrVideo = new Intent("com.htc.album.action.VIEW_PHOTO_FROM_CAMERA");
                  		showImageOrVideo.setDataAndType(Uri.withAppendedPath(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "1"), "image/*");
                  	}else{
                  		Log.i(TAG,"default com.android.camera");
                  		showImageOrVideo = new Intent("com.android.camera.action.REVIEW", uri);
                  	}

                  	startActivity(showImageOrVideo);
                }
            } catch (Exception e) {
            	Logger.logApplicationException(e, "mGallery: could not open gallery");
            }
        }
    };
    /**
     * Check if package is installed and callable
     */
    private boolean isPackageExists(String targetPackage, List<ApplicationInfo> packs){
            for (ApplicationInfo packageInfo : packs) {
            	if(packageInfo.packageName.equals(targetPackage)){

            		return true;
            	}
        }        
        return false;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video);

        //Set the minimal resolution sizes as points to use in CameraInfo
        CameraInfo.initMinResolutionSizes();
        
        //Initiate the preview screen with buttons
        initPreview();
        
        
    }
    /* For Functional test*/
    public ShutterButton getShutterButton() {
		return mShutterButton;
	}
    public boolean getVideoRecordingInProgress(){
    	return mVideoRecordingInProgress;
    }
    public View getResetButton(){
    	return mResetButton;
    }
    public View getVideoResolutionButton(){
    	return mVideoResolutionButton;
    }
    
    public boolean runningTest = false;
    public CountDownLatch signal;
    public void setCountDownLatch(int n){
    	signal = new CountDownLatch(n);
    }
    public CountDownLatch getCountDownLatch(){
    	return signal;
    }
    public void incrementLatch(){
    	signal.countDown();
    }
    private String PhotoFileName;
    public String getSavedPhotoFileName(){
    	return PhotoFileName;
    }
    
    private void initPreview(){
    	mPreview = (SurfaceView)findViewById(R.id.preview);
        mPreview.setKeepScreenOn(true);

        mVideoRecordingInProgress = false;
        mSnapshotInProgress = false;
        //Register slide listeners for 
//        videoGestureListener.registerListener(new SlideGestureListener(SlideGestureListener.SLIDE_RIGHT));
//        videoGestureListener.registerListener(new SlideGestureListener(SlideGestureListener.SLIDE_DOWN));
//        videoGestureListener.registerListener(new SlideGestureListener(SlideGestureListener.SLIDE_LEFT));
        videoGestureListener.setCallback(mVideoGestureInterface);
        mPreview.setOnTouchListener(videoGestureListener);

        
        myPackMan = getPackageManager();  
        mGallery = (ThumbnailControl) findViewById(R.id.gallery_thumbnail);
        mGallery.setOnClickListener(mGalleryListener);

        mShutterButton = (ShutterButton) findViewById(R.id.btn_shutter);
        mShutterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreviewing && mIsSdCardAvailable) {
                    // KPI logging.
                    updateKpiTime();
                    Logger.logMessage("ShutterButton.onClick() called at " + DateTimeUtils.formatDate(mCurrentDate));

                    if (mInProgress) {
                        stopVideoRecording();
                    } else {
                        if (FileUtils.getAvailableBytes() > MINIMAL_AVAILABLE_CARD_SIZE_TO_START_RECORD) {
                            startVideoRecording();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.sdcard_not_enough_space, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
//            	mCamera.stopPreview();
//            	Camera.Parameters mParam = mCamera.getParameters();
//
//            	if (mParam.isVideoStabilizationSupported()) {
//            		boolean on = mParam.getVideoStabilization();
//            		mParam.setVideoStabilization(!on);
//            	    mCamera.setParameters(mParam);
//            	    mCamera.startPreview();
//
//            	    Log.i(TAG, "getVideoStabilization() : " + mCamera.getParameters().getVideoStabilization());
//            	}else{
//            		Log.w(TAG, "Video Stabilization not supported");
//            	}
           
            }
        });
        
        
        mSnapModeSelector = (ModeSelector)findViewById(R.id.mode_selector);
        mVideoModeSelector = (ModeSelector)findViewById(R.id.mode_selector);

        mCameraChanger = (ImageView) findViewById(R.id.img_camera_change);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            int numberOfCameras = Camera.getNumberOfCameras(); 

            if (numberOfCameras > 1) {
                mCameraChanger.setVisibility(View.VISIBLE);
                mCameraChanger.setOnClickListener(new CameraChangerOnClickListener());
            }
        }

        mPanel = (RelativeLayout)findViewById(R.id.panel_layout);
        if (mPanel != null) {
            PanelSlideListener listener = new PanelSlideListener(mPanel, getApplicationContext());
            mPanel.setOnTouchListener(listener);        

            mSliderHandle = (ImageView)findViewById(R.id.slider_handle);		
            mSliderHandle.setOnTouchListener(listener);
            mSliderHandle.setOnClickListener(listener);
        }

        mVideoResolutionButton = findViewById(R.id.video_resolution_button);		
        mVideoResolutionValue = (SizeHolder)findViewById(R.id.video_resolution_value);
        
        mSnapResolutionButton = findViewById(R.id.snapshot_resolution_button);
        mSnapResolutionValue = (SizeHolder)findViewById(R.id.snapshot_resolution_value);


        mResetButton = findViewById(R.id.reset_button);
        mResetButton.setOnClickListener(resetButtonListener);


        mTextMessage = (TextView) findViewById(R.id.txt_message);

        mSwitchButton = findViewById(R.id.switch_button);
        mSwitchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(cameraIntent);
                finish();
            }
        });

        recordIndicator = (LinearLayout) findViewById(R.id.video_recording_indicator);
        recordingText = (TextView) findViewById(R.id.record_time);
        mHandler = new VideoHandler();
    }
    @Override
    public void onResume() {
        super.onResume();        
        loadPreferences();
        startCamera();
        mainVideoFrame = (RelativeLayout) findViewById(R.id.main_video_frame);
        previewSurface = (SurfaceView) findViewById(R.id.preview);
        if (!mSurfaceWasDestroyed) {
            startCameraPreview(mPreview.getWidth(), mPreview.getHeight());
        }
        mVideoRecordingInProgress = false;
        mSnapshotInProgress = false;
        checkSdCard();
        addSdCardIntentFilters();
  
    }

    @Override
    public void onPause() {
        if (mInProgress) {
            stopVideoRecording();
        }
        stopCamera();
        unregisterReceiver(mReceiver);
        super.onPause();
    }
    
    private OnClickListener resetButtonListener = new OnClickListener(){
    	@Override
        public void onClick(View v) {
            mCamera.stopPreview();                
            Camera.Parameters params = mDefaultCameraParameters[mCurrentCameraIndex];
            mCamera.setParameters(params);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                if (mCamcorderProfiles == null) {
                    mCamcorderProfiles = new CamcorderProfile[Camera.getNumberOfCameras()];
                }

                mCamcorderProfiles[mCurrentCameraIndex] = CamcorderProfile.get(mCurrentCameraIndex, CamcorderProfile.QUALITY_HIGH);
            } else {
                if (mCamcorderProfiles == null) {
                    mCamcorderProfiles = new CamcorderProfile[1];
                }

                mCamcorderProfiles[mCurrentCameraIndex] = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            }
            
            Size[] vsizes = getVideoSizes();
            Size vsize = vsizes[0]; 
            
            Size[] ssizes = getImageSizes();
            Size ssize = ssizes[0]; 

            mCamcorderProfiles[mCurrentCameraIndex]= adaptCamcorderForNewResolution(mCamcorderProfiles[mCurrentCameraIndex], vsize.width, vsize.height);

            // Update UI
            mActiveMode = CameraInfo.CAMERA_MODE_AUTO;
            
            mVideoModeSelector.setVisibility(View.GONE);
            mSnapModeSelector.setVisibility(View.GONE);
            
            mVideoResolutionChangeListener.setSizes( vsizes);
            mVideoResolutionValue.assignSize(mCamera.new Size(mCamcorderProfiles[mCurrentCameraIndex].videoFrameWidth, mCamcorderProfiles[mCurrentCameraIndex].videoFrameHeight)); 
            
            mSnapResolutionChangeListener.setSizes( ssizes);
            mSnapResolutionValue.assignSize(ssize); 
            params.setPictureSize(ssize.width, ssize.height);
            mCamera.setParameters(params);


            startPreviewWithOptimalParameters(mPreview.getWidth(), mPreview.getHeight());
        }
    	
    };
 
	private void animateVideoSnap(Bitmap bit) {


		final ImageView image = (ImageView) findViewById(R.id.video_snap_anim);

        image.setImageBitmap(bit);
        Animation a = AnimationUtils.loadAnimation(this, R.anim.scale_gone);
       
        a.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {
            	image.setVisibility(View.VISIBLE);

            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
            	image.setVisibility(View.GONE);   	
            	image.setImageBitmap(null);
            }
        });
        
        image.startAnimation(a);
	}

	private void animatePreviewExpanansion(float fromVal, float toVal){
        ValueAnimator va = ValueAnimator.ofFloat(fromVal, toVal);
        va.setDuration(DVS_ANIMATION_TIME);
       
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        	public void onAnimationUpdate(ValueAnimator animation) {
        		
        		Float value = (Float) animation.getAnimatedValue();
        		Log.i(TAG, "animation value float : " + value.floatValue());

            	ViewGroup.LayoutParams sv_p = mainVideoFrame.getLayoutParams();
            	
               	ViewGroup.LayoutParams preview_p = previewSurface.getLayoutParams();
               	preview_p.width = (int)(sv_p.width * value.floatValue());
            	preview_p.height = (int)(sv_p.height * value.floatValue());
            	previewSurface.setLayoutParams(preview_p);

        	}
        });

        
        va.start();
//        mainVideoFrame.setBackgroundDrawable(null);
	}

    /**
     * Async task to save photo on SD card.
     */
    private class SavePhotoAction extends AsyncTask<byte[], Object, Integer> {

		@Override
		protected Integer doInBackground(final byte[]... jpeg) {
			 try {

	                String state = android.os.Environment.getExternalStorageState();

	                if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
	                	Log.e(TAG, "media not mounted");
	                    throw new IOException("Couldn't find SD card.");
	                }

	                if ((jpeg[0] == null) || (jpeg[0].length == 0)) {
	                	Log.e(TAG, "data == " + jpeg[0]);
	                    Logger.logMessage("SavePhotoAction.doInBackground(): no data received");
	                    return 0;
	                }

	                if (FileUtils.getAvailableBytes() < jpeg[0].length) {
	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                            Toast.makeText(getApplicationContext(), R.string.sdcard_not_enough_space, Toast.LENGTH_LONG).show();
	                        }
	                    });
	                    return 0;
	                }

	                // Update thumbnail.
	                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BitmapFactory.Options options = new BitmapFactory.Options();                            
                                if (mCurrentCameraParameters[mCurrentCameraIndex].getPictureSize().height > CameraInfo.THUMBNAIL_IMAGE_HEIGHT) {
                                    options.inSampleSize = Math.round(mCurrentCameraParameters[mCurrentCameraIndex].getPictureSize().height / CameraInfo.THUMBNAIL_IMAGE_HEIGHT);
                                } else {
                                    options.inSampleSize = 1;
                                }
                                mGallery.setThumbnail(BitmapFactory.decodeByteArray(jpeg[0], 0, jpeg[0].length, options), null);
                                mGallery.show();
                                
                            } catch (Exception ex) {
                                Logger.logApplicationException(ex, "SavePhotoAction.doInBackground(): failed to update thumbnail");
                            }
                        }

                    });
	                runOnUiThread(new Runnable() {
		                @Override
		                public void run() {
		                    try {
		                    	//Decrease image size so as to avoid out of memory errors when image capture dimensions are large
		                    	BitmapFactory.Options options = new BitmapFactory.Options();                            
                                if (mCurrentCameraParameters[mCurrentCameraIndex].getPictureSize().height > CameraInfo.THUMBNAIL_IMAGE_HEIGHT) {
                                    options.inSampleSize = Math.round(mCurrentCameraParameters[mCurrentCameraIndex].getPictureSize().height / (CameraInfo.THUMBNAIL_IMAGE_HEIGHT * 3));
                                } else {
                                    options.inSampleSize = 1;
                                }
		                    	//Show snapshot animation
		    	                animateVideoSnap(BitmapFactory.decodeByteArray(jpeg[0], 0, jpeg[0].length, options));
		                        
		                    } catch (Exception ex) {
		                        Logger.logApplicationException(ex, "animateVideoSnap();");
		                    }
		                }

		            });
	                
	
	                PhotoFileName = getPhotoPath(null);

	                File directory = new File(PhotoFileName).getParentFile();
	                if (!directory.exists() && !directory.mkdirs()) {
	                	throw new IOException("Couldn't create path to file.");
	                }
	   
	                try {
	                    FileOutputStream fileOutputStream = new FileOutputStream(PhotoFileName);
	                    fileOutputStream.write(jpeg[0]);
	                    fileOutputStream.close();

	                    // KPI Logging.
	                    updateTime();
	                    Logger.logMessage("Saved photo in " + PhotoFileName + " at " + DateTimeUtils.formatDate(mCurrentDate) + ", time diff: " + getTimeDiff());
	                }
	                catch (java.io.IOException e) {
	                    Logger.logApplicationException(e, "CameraActivity.SavePhotoAction() failed. Couldn't save the file");
	                }

	                String name = PhotoFileName.substring(PhotoFileName.lastIndexOf("/") + 1);

	                ContentValues values = new ContentValues();
	                values.put(Images.Media.TITLE, name);
	                values.put(Images.Media.DISPLAY_NAME, name);
	                values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
	                values.put(Images.Media.MIME_TYPE, "image/jpeg");
	                values.put(Images.Media.DATA, PhotoFileName);
	                values.put(Images.Media.SIZE, jpeg[0].length);

	                final Uri uri = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
	
	                // KPI logging.
	                updateTime();
	                Logger.logMessage("Thumbnail loaded at " + DateTimeUtils.formatDate(mCurrentDate) + ", time diff: " + getTimeDiff());

	                runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                    	mGallery.setUri(uri);                            
	                    	mGallery.show();
	
	                    }
	                });

	                System.gc();
	                return 0;
	                
			 }catch(final Exception e) {
	                Logger.logApplicationException(e, "VideoActivity.SavePhotoAction.doInBackground() failed.");
	         }
			 if(runningTest && signal != null){
	            	signal.countDown();
			 }
			return null;
			
		}
		
		private void updateTime() {
			mPreviousDate = mCurrentDate;
        	mCurrentDate = new Date();
			
		}

		/**
         * Retrieves name for photo (without extension).
         * 
         * @return photo name.
         */
        private String getPhotoName() {
            return "IMG-" + DateTimeUtils.getPhotoFormatDate(new Date());
        }
        
        /**
         * Retrieves path for photo or null if failed.
         * 
         * @param index Index of burst/hdr photo.
         * @param mode current mode.
         * @param givenName already given name that should be used for result path. Can be null.
         * 
         * @return path for photo. 
         */
        private String getPhotoPath(String givenName) {
            try {

                String fileName = givenName == null ? getPhotoName() : givenName;

                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/" + fileName + ".jpg";;

                int num = 1;
                while ((new File(path)).exists()) {
                    path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/" + fileName + "-" + num + ".jpg";
                    num++;
                }
                File directory = new File(path).getParentFile();
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IOException("Couldn't create path to file: " + path);
                }
                new File(path).createNewFile();

                return path;
            }
            catch(final Exception e) {
                Logger.logApplicationException(e, "VideoActivity.SavePhotoAction.getPhotoPath() failed.");
            }
            return null;
        }
    	
    }

    /**
     * Registers SD card events receiver. 
     */
    private void addSdCardIntentFilters() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
    }

    /**
     * Intent events receiver.
     * Handles SD card events.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkSdCard();
        }
    };

    /**
     * Checks SD card state.
     */
    private void checkSdCard() {
        if (FileUtils.isSdCardMounted()) {
            mTextMessage.setVisibility(View.GONE);
            mIsSdCardAvailable = true;
            mGallery.update(getContentResolver(), ThumbnailControl.TYPE_VIDEO);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGallery.show();
                }
            });
            checkSdSize();
        } else {
            mTextMessage.setText(R.string.sdcard_unavailable);
            mTextMessage.setVisibility(View.VISIBLE);
            mIsSdCardAvailable = false;
        }
    }

    /**
     * Checks available size on SD card and displays warning message in needed.
     */
    private void checkSdSize() {
        try {
            long availableBytes = FileUtils.getAvailableBytes();
            if (availableBytes <= MINIMAL_AVAILABLE_CARD_SIZE_TO_SHOW_MESSAGE) {
                mTextMessage.setText(R.string.sdcard_small_size);
                mTextMessage.setVisibility(View.VISIBLE);
            } else {
                mTextMessage.setVisibility(View.GONE);
            }
        } catch(final Exception e) {
            Logger.logApplicationException(e, "VideoActivity.checkSdSize(): Failed.");
        }
    }

    /**
     * Loads application preferences.
     */
    private void loadPreferences() {        
        Logger.setLogging(PreferencesProvider.isLoggingOn(getApplicationContext()));
    }


    @Override
    public boolean onMenuOpened (int featureId, Menu menu) {
        MenuItem item =  menu.findItem(R.id.mi_logging);
        boolean isLoggingOn = Logger.isLoggingOn();
        if (isLoggingOn) {
            item.setTitle(R.string.disable_logging_message);
        } else {
        }

        return(super.onMenuOpened(featureId, menu));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.options, menu);

        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    	case R.id.mi_send_log_file:
    		sendLogFile();
    		break;
    	case R.id.mi_logging:
    		toggleLogging();
    		break;
    	case R.id.mi_about:
    		startActivity(new Intent(getApplicationContext(), AboutActivity.class));
    		break;

    	}
        return(super.onOptionsItemSelected(item));
    }
    /**
     * Send log file function for debug options
     */
    private void sendLogFile(){
    	Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.log_send_email)});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Aptina Camera Log Information");

        String path = Environment.getExternalStorageDirectory()+ "/" + "AptinaLogInfo.txt";
        if (FileUtils.isSdCardMounted() && FileUtils.isFileExist(path)) {
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
        } else {
            sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, Logger.getContent());
        }
        sendIntent.setType("text/html");
        startActivity(Intent.createChooser(sendIntent, "Send mail..."));
    }
    /**
     * Toggle the application logging to a file from the debug options menu
     */
    private void toggleLogging(){
    	 boolean isLoggingOn = !Logger.isLoggingOn();
         Logger.setLogging(isLoggingOn);
         if (isLoggingOn) {
             Toast.makeText(getApplicationContext(), R.string.logging_is_enabled_message, Toast.LENGTH_SHORT).show();
         } else {
             Toast.makeText(getApplicationContext(), R.string.logging_is_disabled_message, Toast.LENGTH_SHORT).show();
         }

         PreferencesProvider.setLogging(getApplicationContext(), isLoggingOn);
    }

    /**
     * Starts camera.
     */
    private void startCamera() {
        try {
            mPreviewHolderCallback = new CustomSurfaceHolderCallback();

            mPreviewHolder = mPreview.getHolder();
            mPreviewHolder.addCallback(mPreviewHolderCallback);
            mPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                int numberOfCameras = Camera.getNumberOfCameras();
                if (mCurrentCameraIndex < 0) {
                    mCurrentCameraIndex = getBackCameraIndex();
                } else { 
                    mCurrentCameraIndex = mCurrentCameraIndex % numberOfCameras;
                }
                mCamera = Camera.open(mCurrentCameraIndex);

                if (mCurrentCameraParameters == null) {
                    mCurrentCameraParameters = new Camera.Parameters[numberOfCameras];
                }

                if (mDefaultCameraParameters == null) {
                    mDefaultCameraParameters = new Camera.Parameters[numberOfCameras];
                }
            }
            
            if (mCamera == null) {
                mCamera = Camera.open();
                mCurrentCameraIndex = 0;
            }
        } catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.startCamera() failed during camera initialization.");
        }

        try {
            if (mDefaultCameraParameters == null) {
                mDefaultCameraParameters = new Camera.Parameters[1];
            }

            if (mDefaultCameraParameters[mCurrentCameraIndex] == null) {
                mDefaultCameraParameters[mCurrentCameraIndex] = mCamera.getParameters();
            }

            if (mCurrentCameraParameters == null) {
                mCurrentCameraParameters = new Camera.Parameters[1];
                mCurrentCameraParameters[0] = mCamera.getParameters();
            } else if (mCurrentCameraParameters[mCurrentCameraIndex] == null) {
                mCurrentCameraParameters[mCurrentCameraIndex] = mCamera.getParameters();
            } else {

                mCamera.setParameters(mCurrentCameraParameters[mCurrentCameraIndex]);
                
            }
        } catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.startCamera() failed during setting camera parameters");
        }

        try {


            // Hide mode dialog.
            mVideoModeSelector.setVisibility(View.GONE);
            mSnapModeSelector.setVisibility(View.GONE);

            if (mCamcorderProfiles == null || mCamcorderProfiles[mCurrentCameraIndex] == null) {
                initCamcorders();
            }
     
            Size[] availableVideoSizes = getVideoSizes();
            Size[] availableSnapSizes = getImageSizes();
            // Inits resolution changers.
            if (mVideoResolutionChangeListener == null || mSnapResolutionChangeListener == null) {
            	mVideoResolutionChangeListener = new ResolutionChangeListener(getApplicationContext(), availableVideoSizes, mVideoModeSelector, true);
            	mSnapResolutionChangeListener = new ResolutionChangeListener(getApplicationContext(), availableSnapSizes, mSnapModeSelector, false);
            	
            	mVideoResolutionButton.setOnClickListener(mVideoResolutionChangeListener);
            	mSnapResolutionButton.setOnClickListener(mSnapResolutionChangeListener);
            	
                mVideoResolutionChangeListener.setCallbackTarget(videoResolutionCallback);
                mSnapResolutionChangeListener.setCallbackTarget(snapshotResolutionCallback);

                // Saves best resolution as default.
                mDefaultCameraParameters[mCurrentCameraIndex] = mCamera.getParameters();
            }

            mVideoResolutionChangeListener.setSizes(availableVideoSizes);
            mSnapResolutionChangeListener.setSizes(availableSnapSizes);
            
            mVideoResolutionValue.assignSize(availableVideoSizes[0]);
            mSnapResolutionValue.assignSize(availableSnapSizes[0]);

        } catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.startCamera() failed during components initializing");
        }
    }

    /**
     * Stops preview and releases camera.
     */
    private void stopCamera() {
        try {
            mCurrentCameraParameters[mCurrentCameraIndex] = mCamera.getParameters();

            if (mPreviewing) {
                mCamera.stopPreview();
            }

            mCamera.release();
            mCamera = null;
            mPreviewing = false;
        } catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.stopCamera() failed");
        }
    }

    /**
     * Initializes recorder.
     */
    private void initRecorder() {
        if (mMediaRecorder != null) return;

        try {
            if (mCamcorderProfiles == null || mCamcorderProfiles[mCurrentCameraIndex] == null) {
                initCamcorders();
            }

            mMediaRecorder = new MediaRecorder();

            mMediaRecorder.setCamera(mCamera);

            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            try {
                mMediaRecorder.setProfile(mCamcorderProfiles[mCurrentCameraIndex]);
            } catch(Exception e) {
            	e.printStackTrace();
            }

            currentFileName = getVideoPath(null);
            mMediaRecorder.setOutputFile(currentFileName);
            mMediaRecorder.setPreviewDisplay(mPreviewHolder.getSurface());

            long availableBytes = FileUtils.getAvailableBytes();
            if (availableBytes > 1024*1024 && availableBytes < 1024*1024*1024) { // If we have at least 1Mb.
                availableBytes -= 1024*1024;
                mMediaRecorder.setMaxFileSize(availableBytes);
            }

      
            mMediaRecorder.prepare();

            mMediaRecorder.setOnErrorListener(new OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    stopVideoRecording();
                }
            });

            mMediaRecorder.setOnInfoListener(new OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                        stopVideoRecording();
                    }
                }
            });
        } catch (Exception e) {
            Logger.logApplicationException(e, "VideoActivity.initRecorder() failed.");
        }
    }

    /**
     * Starts video recording.
     * 
     * @return true if success, false otherwise.
     */
    private boolean startVideoRecording() {
        try {
            mCamera.stopPreview();
            mCamera.unlock();
            initRecorder();
            mMediaRecorder.start();          
            mRecordStartTime = System.currentTimeMillis();
            mInProgress = true;
            setPanelEnabled(false);
            mGallery.setOnClickListener(null);
            mCameraChanger.setOnClickListener(null);

            recordIndicator.setVisibility(View.VISIBLE);

            updateRecordTime();
            mVideoRecordingInProgress = true;
            return true;
        } catch (final Exception e) {
            Logger.logApplicationException(e, "VideoActivity.startVideoRecording(): Failed.");
        }
        if(runningTest && signal != null){
        	signal.countDown();
        }

        return false;
    }

    /**
     * Stops video recording.
     * 
     * @return true if success, false otherwise.
     */
    private boolean stopVideoRecording() {
        try {            
            recordIndicator.setVisibility(View.GONE);
            try {
                mMediaRecorder.stop();
                mCamera.lock();
            } catch(final Exception e) {
            	Log.e(TAG, "Exception : " + e.getMessage());
            }
 
            mInProgress = false;
            setPanelEnabled(true);
            mGallery.setOnClickListener(mGalleryListener);
            mCameraChanger.setOnClickListener(new CameraChangerOnClickListener());

            try {
                mMediaRecorder.release();
            } catch(final Exception e) {
            }

            mMediaRecorder = null;
            mVideoRecordingInProgress = false;
            mCamera.startPreview();
            return true;
        }
        catch (final Exception e) {
            Logger.logApplicationException(e, "VideoActivity.stopVideoRecording(): Failed.");
        } finally {

            new SaveVideoAction().execute(currentFileName);
            checkSdSize();
        }
        return false;
    }

    /**
     * Async task to save video & update thumbnail.
     */
    private class SaveVideoAction extends AsyncTask<String, Object, Object> {

        /**
         * Thumbnail bitmap.
         */
        private Bitmap mBitmap = null;

        /**
         * Video file URI.
         */
        private Uri mUri = null;

        @Override
        protected Object doInBackground(String... params) {
            try {
                String filePath = params[0];
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

                String mimeType = "video/3gpp";
                if(fileName.endsWith(".3gp")){
                    mimeType = "video/3gpp";
                } else if(fileName.endsWith(".mp4")){
                    mimeType = "video/mp4";
                }

                ContentValues values = new ContentValues();
                values.put(Video.Media.TITLE, fileName);
                values.put(Video.Media.DISPLAY_NAME, fileName);
                values.put(Video.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(Video.Media.MIME_TYPE, mimeType);
                values.put(Video.Media.DATA, filePath);
                mUri = getContentResolver().insert(Video.Media.EXTERNAL_CONTENT_URI, values);
                String stringUri = mUri.toString(); 
                int videoId = Integer.parseInt(stringUri.substring(stringUri.lastIndexOf("/")+1));
                mBitmap = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), videoId, MediaStore.Video.Thumbnails.MINI_KIND, null);
            } catch (final Exception e) {
                Logger.logApplicationException(e, "VideoActivity.addToGallery(): Failed.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object data) {
            if ((mBitmap != null) && (mUri != null)) {
                mGallery.setThumbnail(mBitmap, mUri);
                mGallery.show();
            }
        }
    }

    /**
     * Switches to next available camera.
     * 
     * @param cameraIndex camera index.
     */
    private void switchCamera() {
        // KPI logging.
        updateKpiTime();
        Logger.logMessage("Switching cameras started at " + DateTimeUtils.formatDate(mCurrentDate));

        stopCamera();
        mCurrentCameraIndex++;
        startCamera();
        try {
            mCamera.setPreviewDisplay(mPreviewHolder);
        } catch (IOException e) {
            Logger.logApplicationException(e, "CameraActivity.switchCamera() failed");
        }
        startPreviewWithOptimalParameters(mPreviewHolder.getSurfaceFrame().width(), mPreviewHolder.getSurfaceFrame().height());

        // KPI Logging.
        updateKpiTime();
        Logger.logMessage("Camera preview started at " + DateTimeUtils.formatDate(mCurrentDate) + ", time diff: " + getTimeDiff());
    }

    /**
     * Looking for first available back camera.
     * 
     * @return index of first available back camera.
     */
    private int getBackCameraIndex() {
        try {
            for (int i=0; i < Camera.getNumberOfCameras(); i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);

                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    return i;
                }
            }
        } catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.getBackCameraIndex() failed.");
        }
        return 0;
    }


    private boolean isSizeSupported(Size[] mSizeList, int width, int height) {
            int i, arraySiz;
            boolean isSupported = false;
            arraySiz = mSizeList.length;

            for(i = 0; i < arraySiz; i++){
                    if((mSizeList[i].width == width) && 
                       (mSizeList[i].height == height)){
                            isSupported = true;
                            break;
                    }
            }
            return isSupported;
    }
    /**
     * Sets optimal preview size and starts preview.
     *  
     * @param width preview view width.
     * @param height preview view height.
     */
    private void startPreviewWithOptimalParameters(int width, int height) {
    	Log.e(TAG, "optimal params : " + width + ", " + height);
        try {

            Parameters parameters = mCamera.getParameters();
            parameters.set("capture", "video");
//            parameters.setFocusMode(CameraInfo.CAMERA_MODE_AUTO);
         
            if (CameraInfo.VIDEO_MODE_DVS.equals(mActiveMode)) {
                parameters.set(CameraInfo.VIDEO_MODE_DVS, CameraInfo.CAMERA_MODE_ENABLED_DEFAULT_VALUE);
            } else {
                parameters.set(CameraInfo.VIDEO_MODE_DVS, CameraInfo.CAMERA_MODE_DISABLED_DEFAULT_VALUE);
            }

            

             if(isSizeSupported(getPreviewSizes(), width, height)){
            	Log.i(TAG, "getPreviewSizes()[0] returning : " + width + ", " + height);
                preserveAspectRatio(dispDim.x, dispDim.y, width, height);
                parameters.setPreviewSize(width, height);
            } else {
                Camera.Size size = getPreviewSizes()[0];
                Log.i(TAG, "getPreviewSizes()[0] returning : " + size.width + ", " + size.height);
                preserveAspectRatio(dispDim.x, dispDim.y, size.width, size.height);
                parameters.setPreviewSize(size.width, size.height);
            }


        	//See if Video snapshot is supported at this recording resolution
            parameters = toggleVideoSnapshot(parameters);
            mCamera.setParameters(parameters);
 
            mCurrentCameraParameters[mCurrentCameraIndex] = parameters;

            mCamera.startPreview();
            mPreviewing = true;

        }
        catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.startPreviewWithOptimalParameters() failed.");
        }
    }
    /**
     * Function sets the margins of the preview surface in order to preserve aspect ratio 
     * with the preview size the camera returns, instead of stretching the image to 
     * fit the screen dimensions
     */
    private void preserveAspectRatio(int surface_width, int surface_height, int preview_width, int preview_height){
    	float surface_ratio = surface_width / (float)surface_height;
    	float preview_ratio = preview_width / (float)preview_height;
    	float width_ratio = surface_width / (float)preview_width;
    	float height_ratio = surface_height / (float)preview_height;
    	Log.i(TAG, "width_ratio : " + width_ratio + ", height_ratio : " + height_ratio);
  

    	ViewGroup.LayoutParams sv_p = mainVideoFrame.getLayoutParams();
 
    	if(surface_ratio != preview_ratio){
    		if(width_ratio > height_ratio){
    			sv_p.height = surface_height;

        		double Kh = surface_height/(float)preview_height;
        		sv_p.width = (int) (preview_width * Kh);
        		Log.i(TAG, "sv_p.width : " + sv_p.width );
    		}else{
        		sv_p.width = surface_width;
        		
        		double Kw = surface_width/(float) preview_width;
        		sv_p.height = (int) (preview_height * Kw);
        		Log.i(TAG, "sv_p.height : " + sv_p.height );
    		}

    	}

    	mainVideoFrame.setLayoutParams(sv_p);
    	ViewGroup.LayoutParams  preview_p = previewSurface.getLayoutParams();
    	preview_p.width = sv_p.width;
    	preview_p.height = sv_p.height;
    	previewSurface.setLayoutParams(preview_p);


    }
    /**
     * Retrieves Array of available preview sizes
     * 
     * @return Array of available preview sizes.
     */
    private Size[] getPreviewSizes(){
    	return CameraInfo.getZSLPreviewSizes(mDefaultCameraParameters[mCurrentCameraIndex], mCamera, mActiveMode, true);
    }

    /**
     * Retrieves Array of available image sizes
     * 
     * @return Array of available image sizes.
     */
    public Size[] getImageSizes(){ 
    	return CameraInfo.sortPictureSizes(mDefaultCameraParameters[mCurrentCameraIndex], mCamera, mActiveMode);
    }

    /**
     * Sets preview display and starts preview.
     * 
     * @param width width of preview holder.
     * @param height height of preview holder.
     */
    public void startCameraPreview(int width, int height) {
        try {
            mCamera.setPreviewDisplay(mPreviewHolder);
        }
        catch (Exception e) {
            Logger.logApplicationException(e, "CameraActivity.startCameraPreview() failed");
        }

        startPreviewWithOptimalParameters(width, height);

        //Set the view gone after parent R.id.layout has been initialized so that it has a size greater than (0,0), which
        //messes up the first video snapshot animation
        ImageView image = (ImageView) findViewById(R.id.video_snap_anim);
        image.setVisibility(View.GONE);


		
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGallery.update(getContentResolver(), ThumbnailControl.TYPE_VIDEO);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mGallery.show();
                    }
                });
            }
        }).start();
    }

    /**
     * Callback from surface holder.
     */
    private class CustomSurfaceHolderCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        	Log.e(TAG, "surfaceChanged w,h : " + width + ", " + height);
            mPreviewHolder = holder;
            if (!mPreviewing) {
            	dispDim.x = width;
            	dispDim.y = height;
            	
                mSurfaceWasDestroyed = false;
                startCameraPreview(width, height);
            } else {
                try {
                    mCamera.setPreviewDisplay(mPreviewHolder);
                } catch (IOException e) {
                    Logger.logApplicationException(e, "CustomSurfaceHolderCallback.surfaceChanged: failed to start preview");
                }
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceWasDestroyed = true;
        }
    };

    /**
     * Retrieves name for photo (without extension).
     * 
     * @return photo name.
     */
    private String getVideoName() {
        return "MOV-" + DateTimeUtils.getPhotoFormatDate(new Date());
    }

    /**
     * Retrieves path for video or null if failed.
     * 
     * @param givenName already given name that should be used for result path. Can be null.
     * 
     * @return path for video. 
     */
    private String getVideoPath(String givenName) {
        try {
            String fileName = givenName == null ? getVideoName() : givenName;
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/" + fileName + ".3gp";

            File directory = new File(path).getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Couldn't create path to file: " + path);
            }
            new File(path).createNewFile();
            return path;
        }
        catch(final Exception e) {
            Logger.logApplicationException(e, "VideoActivity.SavePhotoAction.getPhotoPath() failed.");
        }
        return null;
    }

    /**
     * Listener to change camera (front/back).
     */
    private class CameraChangerOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switchCamera();
        }

    }

    /**
     * Sets panel buttons clickable/disabled.
     * 
     * @param enabled <b>true</b> to enable panel buttons, <b>false</b> to disable.
     */
    private void setPanelEnabled(boolean enabled) {
    	mVideoResolutionButton.setClickable(enabled);
    	mSnapResolutionButton.setClickable(enabled);
        mResetButton.setClickable(enabled);
        mSwitchButton.setClickable(enabled);
    }

    /**
     * Updates previos & current time properties.
     */
    private void updateKpiTime() {
        mPreviousDate = mCurrentDate;
        mCurrentDate = new Date();
    }

    /**
     * Updates record time.
     */
    private static void updateRecordTime() {
        if (!mInProgress) {
            return;
        }

        long recordTime = System.currentTimeMillis() - mRecordStartTime;
        long next_update_delay = 1000 - (recordTime % 1000);
        String recordTimeString = getFormattedRecordString(recordTime);
        recordingText.setText(recordTimeString);
        mHandler.sendEmptyMessageDelayed(
                UPDATE_RECORD_TIME, next_update_delay);
    }

    /**
     * Retrieves record time as string.
     * 
     * @param recordTime current record time in milliseconds.
     * 
     * @return record time as string.
     */
    private static String getFormattedRecordString(long recordTime) {
        recordTime /= 1000;
        String hours = Long.toString(recordTime / (60*60));
        if (hours.length() < 2) hours = "0" + hours;
        String mins = Long.toString((recordTime % (60*60)) / 60);
        if (mins.length() < 2) mins = "0" + mins;
        String secs = Long.toString(recordTime % 60);
        if (secs.length() < 2) secs = "0" + secs;
        return String.format("%s:%s:%s", hours, mins, secs);
    }


    /**
     * Returns difference between current and previous timestamps in milliseconds.
     * 
     * @return Difference between current & previously saved timestamps.
     */
    private long getTimeDiff() {
        return (mCurrentDate.getTime() - mPreviousDate.getTime());
    }

    /**
     * Initializes camcorders.
     */
    private void initCamcorders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (mCamcorderProfiles == null) {
                mCamcorderProfiles = new CamcorderProfile[Camera.getNumberOfCameras()];
            }

            if (mCamcorderProfiles[mCurrentCameraIndex] == null) {
                mCamcorderProfiles[mCurrentCameraIndex] = CamcorderProfile.get(mCurrentCameraIndex, CamcorderProfile.QUALITY_HIGH);
            }
        } else {
            if (mCamcorderProfiles == null) {
                mCamcorderProfiles = new CamcorderProfile[1];
            }

            if (mCamcorderProfiles[mCurrentCameraIndex] == null) {
                mCamcorderProfiles[mCurrentCameraIndex] = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            }
        }

        Size size = getVideoSizes()[0];

        mCamcorderProfiles[mCurrentCameraIndex] = adaptCamcorderForNewResolution(mCamcorderProfiles[mCurrentCameraIndex], size.width, size.height);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mVideoModeSelector.getVisibility() == View.VISIBLE) {
                	mVideoModeSelector.setVisibility(View.GONE);
                    return true;
                }
            }
        }
        catch(final Exception e) {
            Logger.logApplicationException(e, "VideoActivity.onKeyDown() failed");
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Retrieves Array of available sizes.
     * 
     * @return Array of available sizes.
     */
    public Size[] getVideoSizes() { 
    	mActiveMode = CameraInfo.CAMERA_MODE_AUTO;
  
        Size[] videoSizes = CameraInfo.getVideoSizes(mCamera.getParameters());    

        return videoSizes;
        
    }
    
    private ResolutionChangeListener.Callback videoResolutionCallback = new ResolutionChangeListener.Callback(){

		@Override
		public void onResolutionSelected(Size resolution, int index) {
	    	Size[] size = getVideoSizes();
	        try {

//	        	preserveAspectRatio(dispDim.x,dispDim.y,size[index].width,size[index].height);
	            if (mCamcorderProfiles == null) {
	                initCamcorders();
	            }
	
	            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
	                mCamcorderProfiles[mCurrentCameraIndex] = CamcorderProfile.get(mCurrentCameraIndex, CamcorderProfile.QUALITY_HIGH);
	            } else {
	                mCamcorderProfiles[mCurrentCameraIndex] = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
	            }
	            mCamcorderProfiles[mCurrentCameraIndex] = adaptCamcorderForNewResolution(mCamcorderProfiles[mCurrentCameraIndex], size[index].width, size[index].height);
	
	            mCamera.stopPreview();

	            startPreviewWithOptimalParameters(mCamcorderProfiles[mCurrentCameraIndex].videoFrameWidth,
	                    mCamcorderProfiles[mCurrentCameraIndex].videoFrameHeight);
	        } catch(final Exception e) {
	            Logger.logApplicationException(e, "VideoActivity.onResolutionSelected(): Failed.");
	        }
			
		}

		@Override
		public Size getCurrentResolution() {
			return mCamera.new Size(mCamcorderProfiles[mCurrentCameraIndex].videoFrameWidth, mCamcorderProfiles[mCurrentCameraIndex].videoFrameHeight);
		}
    	
    };
    
    
    
    private ResolutionChangeListener.Callback snapshotResolutionCallback = new ResolutionChangeListener.Callback(){

		@Override
		public void onResolutionSelected(Size resolution, int index) {
			mCurrentCameraParameters[mCurrentCameraIndex].setPictureSize(resolution.width, resolution.height);
			mCamera.setParameters(mCurrentCameraParameters[mCurrentCameraIndex]);
			
		}

		@Override
		public Size getCurrentResolution() {
			Camera.Size pic = mCamera.getParameters().getPictureSize();
			return mCamera.new Size(pic.width, pic.height);
		}
    	
    };
    
    
    /**
     * Check if video snapshot is supported at the video recording
     * resolution, if not disable video snapshot parameter and change 
     * snapshot resolution selector button to NA
     * 
     * @param videoResolution Resolution to check for snapshot support
     */
    boolean snapshot_supported_at_resolution = false;
    private Camera.Parameters toggleVideoSnapshot(Camera.Parameters params){
    	Size videoResolution = params.getPreviewSize();
    	snapshot_supported_at_resolution = CameraInfo.snapshotSupportedRes(videoResolution);

    	RelativeLayout snap_button = (RelativeLayout) findViewById(R.id.snapshot_resolution_button);
    	if(snapshot_supported_at_resolution){
    		params.set("videosnapshot-mode", "enable");
    		snap_button.setVisibility(View.VISIBLE);
    		snap_button.setClickable(true);
    	}else{
    		params.set("videosnapshot-mode", "disable");
    		snap_button.setVisibility(View.GONE);
    		snap_button.setClickable(false);
    	}
    	return params;
    }

    /**
     * Adapts video parameters in camcorder for new resolution.
     * 
     * @param camcorderProfile Current camcorder profile.
     * @param newWidth New width for adapting.
     * @param newHeight New height for adapting.
     * 
     * @return Adapted camcorder.
     */
    private CamcorderProfile adaptCamcorderForNewResolution(CamcorderProfile camcorderProfile, int newWidth, int newHeight) {
        int width = camcorderProfile.videoFrameWidth;
        int height = camcorderProfile.videoFrameHeight;
        int bitRate = camcorderProfile.videoBitRate;
        camcorderProfile.videoFrameWidth = newWidth;
        camcorderProfile.videoFrameHeight = newHeight;
        camcorderProfile.videoBitRate = Math.round((((float)(newWidth * newHeight))/((float)width * height))*bitRate);
        return camcorderProfile;
    }

    /**
     * Custom handler that handles application messages.
     */
    private static class VideoHandler extends Handler { 
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
	            case UPDATE_RECORD_TIME:
	                updateRecordTime();
	                break;
            
            default:
                Logger.logMessage("VideoActivity.VideoHand.handleMessage() received unhandled message: " + msg.what);
                break;
            }
        }

		
    }
    
  //For functional test
    public Size getCurrentVideoResolution() {
		return mCamera.new Size(mCamcorderProfiles[mCurrentCameraIndex].videoFrameWidth, mCamcorderProfiles[mCurrentCameraIndex].videoFrameHeight);
	}

    public Size getCurrentSnapshotResolution() {
		Camera.Size pic = mCamera.getParameters().getPictureSize();
		return mCamera.new Size(pic.width, pic.height);
	}
    
    public void VideoResSelected(Size resolution, int index) {
    	Size[] size = getVideoSizes();
        try {
            if (mCamcorderProfiles == null) {
                initCamcorders();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                mCamcorderProfiles[mCurrentCameraIndex] = CamcorderProfile.get(mCurrentCameraIndex, CamcorderProfile.QUALITY_HIGH);
            } else {
                mCamcorderProfiles[mCurrentCameraIndex] = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            }
            mCamcorderProfiles[mCurrentCameraIndex] = adaptCamcorderForNewResolution(mCamcorderProfiles[mCurrentCameraIndex], size[index].width, size[index].height);

            mCamera.stopPreview();

            startPreviewWithOptimalParameters(mCamcorderProfiles[mCurrentCameraIndex].videoFrameWidth,
                    mCamcorderProfiles[mCurrentCameraIndex].videoFrameHeight);
        } catch(final Exception e) {
            Logger.logApplicationException(e, "VideoActivity.onResolutionSelected(): Failed.");
        }
    }

}

