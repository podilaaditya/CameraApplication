package com.aptina.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aptina.R;
import com.aptina.camera.components.BurstScroll;
import com.aptina.camera.components.FocusMenu;
import com.aptina.camera.components.FocusMenu.MenuLayoutInterface;
import com.aptina.camera.components.ModeSelector;
import com.aptina.camera.components.ShutterButton;
import com.aptina.camera.components.SizeHolder;
import com.aptina.camera.components.ThumbnailControl;
import com.aptina.camera.components.ZoomControl;
import com.aptina.camera.eventlisteners.CameraCompositeListener;
import com.aptina.camera.eventlisteners.GestureCompositeListener.GestureInterface;
import com.aptina.camera.eventlisteners.ModeSelectionListener;
import com.aptina.camera.eventlisteners.PanelSlideListener;
import com.aptina.camera.eventlisteners.ResolutionChangeListener;
import com.aptina.logger.Logger;
import com.aptina.miscellaneous.DateTimeUtils;
import com.aptina.miscellaneous.FileUtils;
import com.aptina.miscellaneous.PreferencesProvider;

/**
 * Implements main camera preview activity.
 */
@SuppressLint("NewApi")
public class CameraActivity extends Activity implements ModeSelectionListener.Callback, ResolutionChangeListener.Callback, ZoomControl.ZoomingCompletedCallback {
	/**
	 * Logging
	 */
	private static final String TAG = "CameraActivity";

    /**
     * HDR & Burst thumbnail preview width.
     */
    private static final int BURST_PREVIEW_WIDTH = 640;

    /**
     * HDR & Burst thumbnail preview height;
     */
    private static final int BURST_PREVIEW_HEIGHT = 480;

    /**
     * HDR & Burst thumbnail preview width.
     */
    private static final int PANORAMA_PREVIEW_WIDTH = 600;

    /**
     * HDR & Burst thumbnail preview height;
     */
    private static final int PANORAMA_PREVIEW_HEIGHT = 96;


    /**
     * Minimal available size on SD card. 20Mb in bytes. 
     */
    private static final long MINIMAL_AVAILABLE_CARD_SIZE = 20*1024*1024L;


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
     * Focus menu view, needs to be populated by FocusMenu class
     */
    private LinearLayout mFocusMenuView = null;
    
    /**
     * The list of focuses available, fits inside mFocusMenuView
     */
    private LinearLayout mFocusMenuList = null;
    
    /**
     * The text of the burst 
     */
    private TextView mBurstText = null;
    /**
     * Camera instance.
     */
    private Camera mCamera = null;

    /**
     * In preview flag.
     */
    private boolean mPreviewing = false;

    /**
     * SD card availability flag.
     */
    private boolean mIsSdCardAvailable = false;
    /**
     * Video Activity flag.
     */
    private boolean mInVideoActivity = false;

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
    public ShutterButton mShutterButton;

    /**
     * Thumbnail control.
     */
    private ThumbnailControl mGallery;

    /**
     * Resolution button.
     */
    private View mResolutionButton = null;
    /**
     * Chosen resolution drawer.
     */
    private SizeHolder mResolutionValue = null;

    /**
     * Resolution panel button listener.
     */
    private ResolutionChangeListener mResolutionChangeListener = null;

    /**
     * Mode selector view.
     */
    private View mModeSelectionButton = null;

    /**
     * Still/video Switch.
     */
    private View mSwitchButton;

    /**
     * Options view.
     */
    private View mOptionsButton;

    /**
     * Mode selection listener.
     */
    private ModeSelectionListener mModeSelectionListener = null;

    /**
     * Zoom control.
     */
    private ZoomControl mZoomControl;

    /**
     * Mode selector view.
     */
    private ModeSelector mModeSelector = null;

    /**
     * Burst thumnail viewer and burst # selector
     */
    private BurstScroll mBurstScroll = null;
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
     * Amount of images in burst.
     * Used for HDR and burst modes. 
     */
    private int mBurstImages = 0;

    /**
     * Common name for all burst/HDR photos inside one set.
     */
    private String mBurstFileName;

    /**
     * Thumbnail to display panorama preview.
     */
    private ImageView mPanoramaThumb;

    /**
     * Progress dialog.
     */
    private ProgressDialog mProgressDialog;

    /**
     * Reset button view.
     */
    private View mResetButton; 

    /**
     * Text message.
     */
    private TextView mTextMessage;
    
    /**
     * Active mode.
     */
    private String mActiveMode = null;
    
    /**
     * The focus mode that camera was on before switching to ZSL
     * mode. Hold this value so we can restore it when user switches back from
     * ZSL
     */
    private String mNonZSLFocusMode = CameraInfo.FOCUS_MODE_AUTO;
    
    /**
     * Previous mode selection is stored to enable stop start in better manner
     */
    private String mPreviewModeSelected = null;

    /**
     * Sould pool for audio playback.
     */
    private SoundPool mSoundPool = null;

    /**
     * A callback for autofocus trigger
     */
    private CustomAutoFocusCallBack mAutoFocusCallback = null;
    /**
     * Sound ID for thumbnail update sound.
     */
    private int mThumbnailSoundId = 0;

    /**
     * Sound ID for shutter sound.
     */
    private int mShutterSoundId = 0;

    /**
     * Current thumbnail number.
     */
    private int mThumbnailNumber = CameraInfo.BURST_MODE_DEFAULT_VALUE;
    /**
     * Amount of times we retry to start enableSelectMode
     */
    private static int enableSelectModeRetryTimes = 1;
    /**
     * Thumbnails data list.
     */
    private ArrayList<byte[]> mThumbnailsDataList = new ArrayList<byte[]>();
    
    /**
     * PackageManager to check what gallery packages there are to use
     */
    private PackageManager myPackMan = null;
    /**
     * Indicates that preview surface view was destroyed.
     */
    private boolean mSurfaceWasDestroyed = true;

    /**
     * Buffer for last HDR photo.
     */
    private byte[] mLastPhotoBuffer = null;

    /**
     * Flag indicating whether we should restore focus mode after taking picture.
     */
    private boolean mRestoreFocusMode = false;

    /**
     * Current focus mode.
     */
    private String mFocusMode = null;
    
    /**
     * Variable to hold the current width(x) and height(y) of the android display
     */
    private Point dispDim = new Point();

    /**
     * Store the application context for when we need it inside interfaces
     */
    private Context mContext = null;
    /**
     * Variable to hold our focus mode dialog
     */
    private FocusMenu mFocusMenu;
    /**
     * Variable for front/back camera switch button
     */
    private ImageView mCameraChanger = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.camera);
        /**
         * Dismiss the keyguard for activity so that is doesn't interfere with camera preview and capture when screen is locked
         */
        dismissKeyGuard();
        
        mContext = this;
        
        myPackMan = getPackageManager();  
        
        //Set the minimal resolution sizes as points to use in CameraInfo
        CameraInfo.initMinResolutionSizes();
  

        
        mFocusMenu = new FocusMenu(this, mMenuLayoutInterface);

        /**
         * Initialize the preview views and buttons of activity
         */
        initPreView();
        
        /**
         * Initialize the panel views and buttons of the activity
         */
        initPanelView();


    }

    /**
     * Dismiss the key guard for activity so that is doesn't interfere with camera preview and capture when screen is locked
     * Some phones prevent camera from use when screen is off or locked
     */
    private void dismissKeyGuard(){
    	
    	Window win = getWindow();  
    	win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); 
    	win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
    
    private CameraCompositeListener mCameraGestureListener = new CameraCompositeListener();
	public CameraCompositeListener getCameraGestureListener(){
		return mCameraGestureListener;
	}
	private GestureInterface mGestureInterface = new GestureInterface(){

		@Override
		public void OnSlideGesture(int slide_dir) {
		}

		@Override
		public void OnDoubleTapGesture(MotionEvent event) {
			Log.i(TAG, "OnDoubleTapGesture");
			roiTouch(event);
		}

		@Override
		public void OnSmileGesture() {
		}
		
		@Override
		public void OnFrownDetected() {
		}
		
		@Override
		public void OnZoomGesture(float zoom_scale) {
			if(mActiveMode == CameraInfo.CAMERA_MODE_AUTO){
        		setZoomDistance(zoom_scale);
        	}
		}

		@Override
		public Camera getCamera() {
			return mCamera;
		}



		
	};
	 /**
     * Handle region of interest motion event, triggers after double tap
     * Single touch causes it to fire off a lot by accident during pinch zoom
     * @param event
     */
    private void roiTouch(MotionEvent event){
    	Parameters parameters = mCamera.getParameters();
        if ("continuous-picture".equalsIgnoreCase(parameters.getFocusMode())) {
        
            try {
                Size previewSize = parameters.getPreviewSize();
                int prevScreenWidth = mPreview.getWidth();
                int prevScreenHeigth = mPreview.getHeight();
                parameters.set("focus-roi-x", Math.round(event.getX()*previewSize.width/prevScreenWidth));
                parameters.set("focus-roi-y", Math.round(event.getY()*previewSize.height/prevScreenHeigth));
                //Changes for Roi Based Exposure: 
                //1.Realesing previously held ae locks if any (sleep of 1s added to realese it properly). 
                //2.Holding the AE lock 
                if (parameters.isAutoExposureLockSupported()) {
                	parameters.setAutoExposureLock(false);
                	mCamera.setParameters(parameters);
                    Thread.sleep(CameraInfo.EXPOSURE_WAIT_TIME);//Needed to allow exposure to change
                    parameters.setAutoExposureLock(true);
                    mCamera.setParameters(parameters);
                }
                
                //End
                mCurrentCameraParameters[mCurrentCameraIndex] = parameters;
                mCamera.autoFocus(null);
//                Toast.makeText(mCamContext, "AE locked", Toast.LENGTH_SHORT).show();
            } catch(final Exception e) {
                Logger.logApplicationException(e, "CameraActivity.Preview.onTouch(): failed during setting focus.");
            }
        }// end of if
    }
	/**
	 * The maximum digital zoom supported by the device
	 */
	private int MAX_ZOOM;
	/**
	 * The divider of MAX_ZOOM that gives us the ZOOM_RATIO
	 */
	private int MAX_ZOOM_DIVIDOR = 4;
	/**
	 * The constant ratio that we multiply our pinch scale ratio on MotionEvent.ACTION_MOVE.
	 * The ratio should be set to a value so that one full pinch gesture will be able
	 * to go from 0 to MAX_ZOOM;
	 */
	private float ZOOM_RATIO;
    /**
     * Function that calculates how much to zoom based on input scale.
     * The scale, which is the ratio of the current distance between the
     * two fingers with the last distance between the two fingers, is multiplied
     * with the ZOOM_RATIO to give us the zoom that we need to add/subtract from the 
     * current zoom level. The ZOOM_RATIO is defined as the MAX_ZOOM/(A constant > 1)
     * @param scale
     * @return
     */
    private void setZoomDistance(float scale){
//    	Log.i(TAG, "Raw Pinch Scale : " + scale);
    	Camera.Parameters params = mCamera.getParameters();
    	//Replace camera_zoom_level with to_zoom when ACSS-1058 is fixed
    	int current_zoom = params.getZoom();
    	int zoom_level = current_zoom;
//    	Log.i(TAG, "current zoom = "+ zoom_level);
    	
		
		//Modify scale for negative and positive zooming
    	scale -= 1;

		
		//Modified scale for zooming
//    	Log.i(TAG, "Modified Pinch Scale : " + scale);
		zoom_level += scale * ZOOM_RATIO;
		
		if(zoom_level > MAX_ZOOM){
			zoom_level = MAX_ZOOM;
		}else if(zoom_level < 0){
			zoom_level = 0;
		}
//		Log.i(TAG, "setting zoom to = " + zoom_level + ", out of a max of = " + MAX_ZOOM);
		mZoomControl.setZoom(zoom_level);

    }
    public void setZoomRatios(){
		MAX_ZOOM = mCamera.getParameters().getMaxZoom();
		ZOOM_RATIO = MAX_ZOOM/MAX_ZOOM_DIVIDOR;
	}
	
	public int getMaxZoom(){
		return MAX_ZOOM;
	}
	
	public int getMaxZoomDividor(){
		return MAX_ZOOM_DIVIDOR;
	}
	
	public float getZoomRatio(){
		return ZOOM_RATIO;
	}
//	    private ZoomGestureListener mZoomGestureListener;	
//	    private ZoomGestureListener.ZoomInterface zoomInterface = new ZoomGestureListener.ZoomInterface(){
//	    	/**
//	         * Function to return the current camera 
//	         */
//	    	@Override
//	        public Camera getCamera(){
//	        	return mCamera;
//	        }
//	
//	        /**
//	         * Set camera's parameters
//	         */
//	        @Override
//	        public void setCameraParams(Camera.Parameters params){
//	        	mCamera.setParameters(params);
//	        }
//	        
//	        /**
//	         * Get the current active mode, need to disable zooming in
//	         * non auto modes since it is not yet supported
//	         */
//	        @Override
//	        public String getActiveMode(){
//	        	return mActiveMode;
//	        }
//	        
//	        /**
//	         * Set the camera parameters array
//	         */
//	        @Override
//	        public void setParamsArray(Camera.Parameters params){
//	        	mCurrentCameraParameters[mCurrentCameraIndex] = params;
//	        }
//	        
//	        /**
//	         * Set the autofocus callback of the camera to null
//	         */
//	        @Override
//	        public void setAutoFocusNull(){
//	        	 mCamera.autoFocus(null);
//	        }
//	        
//	        /**
//	         * Set the zoom level
//	         */
//	        @Override
//	        public void changeZoom(int to_zoom){
//	        	mZoomControl.setZoom(to_zoom);
//	        }
//	    	
//	    };
	    
//	    public ZoomGestureListener getZoomGestureListener(){
//	    	return mZoomGestureListener;
//	    }
	    public Camera getCamera(){
	    	return mCamera;
	    }
	    public boolean isPreviewing(){
	    	return mPreviewing;
	    }
	    public boolean isSDCardAvailable(){
	    	return mIsSdCardAvailable;
	    }
	    public ShutterButton getShutterButton(){
	    	return mShutterButton;
	    }
	    public ImageView getCameraChanger(){
	    	return mCameraChanger;
	    }
	    public View getResetButton(){
	    	return mResetButton;
	    }
	    public View getSwitchButton(){
	    	return mSwitchButton;
	    }
	    public boolean inVideoActivity(){
	    	return mInVideoActivity;
	    }
	    public  CustomAutoFocusCallBack getAutofocusCallback(){
	    	return mAutoFocusCallback;
	    }
	    public String getCurrentActiveMode(){
	    	return mActiveMode;
	    }
	   
	    private OnClickListener mShutterButtonListener = new OnClickListener(){
	    	@Override
            public void onClick(View v) {
            	if(mCamera.getParameters().getZoom() !=0 ){
            		Toast.makeText(getApplicationContext(), R.string.zero_zoom_label, Toast.LENGTH_LONG).show();
            		
            	}else{
            		if (mPreviewing && mIsSdCardAvailable) {
                        // KPI logging.
                        updateTime();
                        Logger.logMessage("ShutterButton.onClick() called at " + DateTimeUtils.formatDate(mCurrentDate));

                        //TODO INCLUDE cleanScroll
//                        mBurstScroll.cleanScroll();
                        mPreviewing = false;
                        mShutterButton.setClickable(false);
                        if (CameraInfo.FOCUS_MODE_ROIFOCUS.equalsIgnoreCase(mCamera.getParameters().getFocusMode())) {
                            takePicture(mCamera);
                        } else {
                        	mAutoFocusCallback = new CustomAutoFocusCallBack();
                            mCamera.autoFocus(mAutoFocusCallback);
                        }
                        if(CameraInfo.CAMERA_MODE_AUTO.equals(mActiveMode)){
                        	mShutterButton.setClickable(false);
                        }
                    }
            	}
                
            }
	    	
	    };
    
	    MenuLayoutInterface mMenuLayoutInterface = new MenuLayoutInterface() {
	    	/**
			 * Add each focus option view to the focus menu section
			 * of camera.xml
			 * @param view The focus menu option
			 */
			@Override
			public void addViewToLayout(View view) {
				mFocusMenuList.addView(view);
			}
			
			/**
			 * Toggle the focus menu section of camera.xml visible
			 * once all of the options items have been created and added
			 * to the main view. Menu panel button should be used to toggle
			 * 
			 * @param visible Toggle boolean
			 */
			@Override
			public void setVisible(boolean visible) {
				mFocusMenuView.setVisibility(visible ? View.VISIBLE : View.GONE);
			}
			
			/**
			 * Call to change the focus mode of the camera and exit the
			 * focus men view group
			 * 
			 * @index the index of the focus view item
			 */
			@Override
			public void OnFocusClick(int index) {
				mCurrentCameraParameters[mCurrentCameraIndex].setFocusMode(focus_options[index].toString());
				mFocusMode = focus_options[index].toString();
				mCamera.setParameters(mCurrentCameraParameters[mCurrentCameraIndex]);
				Log.i(TAG, "Focus mode selected : " + focus_options[index].toString());
				Toast.makeText(mContext,  focus_options[index].toString(), Toast.LENGTH_SHORT).show();
				removeFocusOptions();
				
			}
		};

		/**
		 * Public get method for our test
		 */
		public LinearLayout getFocusMenuList(){
			return mFocusMenuList;
		}
		public MenuLayoutInterface getFocusMenuInterface(){
			return mMenuLayoutInterface;
		}
		public LinearLayout getFocusMenuView(){
			return mFocusMenuView;
		}
		public FocusMenu getFocusMenu(){
			return mFocusMenu;
		}
    	/**
         * Initialize the preview views and buttons of activity
         */
        private void initPreView(){

        	
        	mPreview = (SurfaceView)findViewById(R.id.preview);
        	mPreview.setKeepScreenOn(true);

//        	mZoomGestureListener = new ZoomGestureListener(mPreview, getApplicationContext());
//        	mZoomGestureListener.setCallbackTarget(zoomInterface);
        	mCameraGestureListener.setCallback(mGestureInterface);
        	mPreview.setOnTouchListener(mCameraGestureListener);

        	
        	mGallery = (ThumbnailControl) findViewById(R.id.gallery_thumbnail);
            mGallery.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	Log.i(TAG, " gallery clicked"); 
                	List<ApplicationInfo> packs = myPackMan.getInstalledApplications(0);             
                     try {
                        Uri uri = mGallery.getUri();
                        Intent showImageOrVideo = null;

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
            });

            mShutterButton = (ShutterButton) findViewById(R.id.btn_shutter);
            mShutterButton.setOnClickListener(mShutterButtonListener);
//          mShutterButton.setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View v) {
//					Intent gridGalleryintent = new Intent(getApplicationContext(), GridGalleryActivity.class);
//                  startActivity(gridGalleryintent);
//					
//				}
//          	
//          });
//            mBurstText = (TextView) findViewById(R.id.bursts_to_capture_text)
//            mBurstText.setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View arg0) {
//					Intent gridGalleryintent = new Intent(getApplicationContext(), GridGalleryActivity.class);
//					startActivity(gridGalleryintent);
//				}
//      
//            });
            mModeSelector = (ModeSelector)findViewById(R.id.mode_selector);

            mBurstScroll = (BurstScroll) findViewById(R.id.burst_scroll);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                int numberOfCameras = Camera.getNumberOfCameras(); 

                if (numberOfCameras > 1) {
                    mCameraChanger = (ImageView) findViewById(R.id.img_camera_change);
                    mCameraChanger.setVisibility(View.VISIBLE);
                    mCameraChanger.setOnClickListener(new CameraChangerOnClickListener());
                }
            }
        }
   
        /**
         * Check if package is installed and callable
         */
        private boolean isPackageExists(String targetPackage, List<ApplicationInfo> packs){
                for (ApplicationInfo packageInfo : packs) {
                	if(packageInfo.packageName.equals(targetPackage)){
                
                		Log.i(TAG, "package exists: " + packageInfo.toString());
                		return true;
                	}
            }        
            return false;
        }
        

        
        /**
         * Initialize the panel views and buttons of the activity
         */
        private void initPanelView(){
        	mPanel = (RelativeLayout)findViewById(R.id.panel_layout);
            if (mPanel != null) {
                PanelSlideListener listener = new PanelSlideListener(mPanel, getApplicationContext());
                mPanel.setOnTouchListener(listener);        

                mSliderHandle = (ImageView)findViewById(R.id.slider_handle);		
                mSliderHandle.setOnTouchListener(listener);
                mSliderHandle.setOnClickListener(listener);
            }

            mResolutionButton = findViewById(R.id.resolution_button);		
            mResolutionValue = (SizeHolder)findViewById(R.id.snapshot_resolution_value);

            mModeSelectionButton = findViewById(R.id.mode_button);
            mOptionsButton = findViewById(R.id.options_button);

            mPanoramaThumb = (ImageView) findViewById(R.id.panorama_thumb);
            
            mFocusMenuView = (LinearLayout) findViewById(R.id.focus_menu_one);
            mFocusMenuList = (LinearLayout) findViewById(R.id.ll_focuses);
            mOptionsButton.setOnClickListener(optionsDialogListener);
//            mOptionsButton.setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View v) {
//					Intent gridGalleryintent = new Intent(getApplicationContext(), GridGalleryActivity.class);
//                    startActivity(gridGalleryintent);
//					
//				}
//            	
//            });
           
            mResetButton = findViewById(R.id.reset_button);
            mResetButton.setOnClickListener(new OnClickListener() {            
                @Override
                public void onClick(View v) {
                	resetCamera();
                }
            });

            mTextMessage = (TextView) findViewById(R.id.txt_message);

            mSwitchButton = findViewById(R.id.switch_button);
            mSwitchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent videoIntent = new Intent(getApplicationContext(), VideoActivity.class);
                    startActivity(videoIntent);
                    mInVideoActivity = true;
                    finish();
                }
            });
        }
        

        
        /** Camera Focus Settings Arrays */
     	 private String[] focus_options;
     	//Public get for our FocusMenuFunctionalTests
     	 public String[] getFocusOptionsArray(){
     		 return focus_options;
     	 }
         /**
          * Get the focus modes and populate the custom dialog class.
          * Set click listeners for the different focus options views
          */
      	 private void initFocusOptions(){
 			List<String> focusModes = null;
 			if (mCamera != null && (focusModes = mCamera.getParameters().getSupportedFocusModes()) != null && !focusModes.isEmpty()) {
 				List<String> list = new ArrayList<String>();
 				for(String s : focusModes) {
 	        		if(!s.equals(CameraInfo.FOCUS_MODE_ROIFOCUS) && !s.equals(CameraInfo.FOCUS_CONTINUOUS_VIDEO)) {
 	        			list.add(s);
 	        		}
 	        	}
 				focusModes = list;

 				focus_options = focusModes.toArray(new String[focusModes.size()]);
 				//get current active focus.
 				String focusMode = mCamera.getParameters().getFocusMode();

 				mFocusMenu.setFocusModesArray(focus_options, focusMode);
 
 				for(int i = 0; i < focusModes.size(); i++){
 					if(focusModes.get(i).equalsIgnoreCase(focusMode)){
 						Log.i(TAG, "Current focus is : " + focusModes.get(i));
 						break;
 					}
 				}

 			}
      	  
          	
         }
      	 
      	private void removeFocusOptions(){
      		focus_options = null;
      		mFocusMenuList.removeAllViews();
      		mFocusMenuView.setVisibility(View.GONE);
      	}
     
	   	private OnClickListener optionsDialogListener = new OnClickListener() {
			@Override
			public void onClick(View argv) {
	
				if(mFocusMenuView.getVisibility() == View.GONE){
					initFocusOptions();
				}else{
					removeFocusOptions();
				}

			}
	
	    };
	    /**
	     * Public get method for test FocusMenuFunctionalTests
	     */
	    public View getOptionsButton(){
	    	return mOptionsButton;
	    }
	    @Override
	    public void onResume() {
	        super.onResume();        
	        loadPreferences();
	        Log.i(TAG, " Current mode : " + mActiveMode);
	        
	        //Initialize  mActiveMode so that is is not null whenever the activity starts
	        if(mActiveMode == null){
	        	mActiveMode = CameraInfo.CAMERA_MODE_AUTO;
	        }
	        startCamera();
	
	        if (!mSurfaceWasDestroyed) {
	            startCameraPreview(mPreview.getWidth(), mPreview.getHeight());
	        }
	        handleViews();

	        mBurstScroll.cleanScroll();
	        mPanoramaThumb.setImageBitmap(null);
	
	        // Create sound pool and add necessary sound effects.
	        if (mSoundPool == null) {
	            mSoundPool = new SoundPool(CameraInfo.BURST_MODE_DEFAULT_VALUE, AudioManager.STREAM_MUSIC, 0);
	            mThumbnailSoundId = mSoundPool.load(this, R.raw.thumbnail_sound, 1);
	            mShutterSoundId = mSoundPool.load(this, R.raw.shutter_sound, 1);
	        }
	
	        checkSdCard();
	        addSdCardIntentFilters();
	        
	    }
	
	    @Override
	    public void onPause() {
	        stopCamera();
	        unregisterReceiver(mReceiver);
	        super.onPause();
	
	        // Deallocate sound pool to free memory.
	        if (mSoundPool != null) {
	            mSoundPool.release();
	            mSoundPool = null;
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
	            mGallery.update(getContentResolver(), ThumbnailControl.TYPE_IMAGE);
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

            if (availableBytes <= MINIMAL_AVAILABLE_CARD_SIZE) {
                mTextMessage.setText(R.string.sdcard_small_size);
                mTextMessage.setVisibility(View.VISIBLE);
            } else {
                mTextMessage.setVisibility(View.GONE);
            }
        } catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.checkSdSize(): Failed.");
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
    	Log.i(TAG, "MENU onMenuOpened");
        MenuItem item =  menu.findItem(R.id.mi_logging);
        boolean isLoggingOn = Logger.isLoggingOn();
        if (isLoggingOn) {
            item.setTitle(R.string.disable_logging_message);
        } else {
            item.setTitle(R.string.enable_logging_message);
        }

        return(super.onMenuOpened(featureId, menu));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.i(TAG, "MENU onCreateOptionsMenu");
        new MenuInflater(this).inflate(R.menu.options, menu);

        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.i(TAG, "MENU onOptionsItemSelected");
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
    	Log.i(TAG, "startCamera()");
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
                setZoomRatios();
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
            listSupportedFocusModes(mCamera);
           
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
            mZoomControl = (ZoomControl) findViewById(R.id.zoom_layout);
            mZoomControl.init(mCamera);
            if (mCamera.getParameters().isSmoothZoomSupported() || mCamera.getParameters().isZoomSupported()) {
                mCamera.setZoomChangeListener(mZoomControl);
            } else {
                mZoomControl.setVisibility(View.GONE);
            }

            // Hide mode dialog.
            mModeSelector.setVisibility(View.GONE);
            handleViews();
            Size[] availableSizes = getAvailableSizes();

            // Inits resolution changer.
            if (mResolutionChangeListener == null) {
                mResolutionChangeListener = new ResolutionChangeListener(getApplicationContext(), availableSizes, mModeSelector);
                mResolutionButton.setOnClickListener(mResolutionChangeListener);
                mResolutionChangeListener.setCallbackTarget(this);

                Camera.Parameters params = mCamera.getParameters();
                Camera.Size bestPictureSize = CameraInfo.sortPictureSizes(mDefaultCameraParameters[mCurrentCameraIndex], mCamera, mActiveMode)[0];
                params.setPictureSize(bestPictureSize.width, bestPictureSize.height);

                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(params);

                // Saves best resolution as default.
                mCurrentCameraParameters[mCurrentCameraIndex] = params;
                mDefaultCameraParameters[mCurrentCameraIndex] = params;
            }

            mResolutionChangeListener.setSizes(availableSizes);
            mResolutionValue.assignSize(mCamera.getParameters().getPictureSize());            

            // Inits mode changer.
            if (mModeSelectionListener == null) {
                mModeSelectionListener = new ModeSelectionListener(getApplicationContext(), mCamera, mModeSelector, mModeSelectionButton);
                mModeSelectionButton.setOnClickListener(mModeSelectionListener);
                mModeSelectionListener.setCallbackTarget(this);
            }

            mModeSelectionListener.setCamera(mCamera);
            mFocusMode = mCurrentCameraParameters[mCurrentCameraIndex].getFocusMode();


        } catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.startCamera() failed during components initializing");
        }
    }

    
    /**
     * List the supported focus modes of the camera
     * 
     * @param mCamera2
     */
    private void listSupportedFocusModes(Camera cam) {
    	Log.d(TAG, "Current Focus mode : " + cam.getParameters().getFocusMode());
    	List<String> f_list = cam.getParameters().getSupportedFocusModes();
    	Log.d(TAG, "Supported focus modes");
    	for(String f : f_list){
    		Log.d(TAG, "" + f);
    	}
	}
	/**
     * Stops preview and releases camera.
     */
    private void stopCamera() {
    	if (mCamera == null)
    			return;
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
     * Switches to next available camera.
     * 
     * @param cameraIndex camera index.
     */
    private void switchCamera() {
        // KPI logging.
        updateTime();
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

        //If the focus menu is open, update its contents based on the new camera
        if(mFocusMenuView.getVisibility() == View.VISIBLE ){
        	removeFocusOptions();
        	initFocusOptions();
        }
        
		
        // KPI Logging.
        updateTime();
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

   

    /**
     * Sets optimal preview size and starts preview.
     *  
     * @param width preview view width.
     * @param height preview view height.
     */
    private void startPreviewWithOptimalParameters(int width, int height) {
        try {
        	
            // Disable capturing on time of mode change.
            setPanelEnabled(false);
            mShutterButton.setClickable(false);

            // Get last saved params.
            Camera.Parameters parameters = mCurrentCameraParameters[mCurrentCameraIndex];
            parameters.setPictureFormat(PixelFormat.JPEG);
            String activeMode = CameraInfo.getActiveMode(parameters);

            // Disable current mode.
            if (activeMode != null) {
                mBurstImages = 0;
                if (activeMode.equals(CameraInfo.CAMERA_MODE_BURST)) {
                    parameters.set(activeMode, 0);
                } else if (!CameraInfo.CAMERA_MODE_AUTO.equals(activeMode)) {
                    parameters.set(activeMode, CameraInfo.CAMERA_MODE_DISABLED_DEFAULT_VALUE);
                }
            }

            Size[] ss = getPreviewSizes();
            for(Size s : ss){
            	Log.w(TAG, " size : " + s.width + ", " + s.height);
            }
            // Reset preview frame.
            Camera.Size size = getPreviewSizes()[0];
            Log.i(TAG, "getPreviewSizes()[0] returning : " + size.width + ", " + size.height);//1280X720?
            preserveAspectRatio(dispDim.x, dispDim.y, size.width, size.height);
            parameters.setPreviewSize(size.width, size.height);
            
                   
          
            //OMAP4 Bug
       	 	List<Integer> frameRates = parameters.getSupportedPreviewFrameRates();
       	 	for(Integer i : frameRates){
       	 		Log.i(TAG, "supported frame rate : " + i);
       	 	}
       	    int min = 15;
       	 	int max = 30;

	        if (frameRates != null) {
	            max = Collections.max(frameRates);
	            min = Collections.min(frameRates);
	        } //OMAP4 : END
	        
            if (CameraInfo.CAMERA_MODE_PANORAMA.equals(mActiveMode)) {
                parameters.setPreviewFrameRate(min); //OMAP4 BUG
            } else {
                parameters.setPreviewFrameRate(max); //OMAP4 BUG
            }

            
	        
	        
            // Start camera with right preview size.
            mCamera.setParameters(mCurrentCameraParameters[mCurrentCameraIndex]);


 

      
            mCamera.startPreview();
 
            // Set zoom value.
            if ((parameters.isZoomSupported() || parameters.isSmoothZoomSupported())) { 
                int zoomValue = parameters.getZoom();
                Log.e(TAG, "zoomValue : " + zoomValue);
                if (CameraInfo.CAMERA_MODE_PANORAMA.equals(mActiveMode) || CameraInfo.CAMERA_MODE_HDR.equals(mActiveMode)) {
                	Log.e(TAG, "setting zoom values");
                    zoomValue = 0;
                    parameters.setZoom(zoomValue);
                }
                Log.e(TAG, "setting setZoomingCompletedCallbackListener");
                // Wait until zooming is completed.  
                mZoomControl.setSliderWidths();
                mZoomControl.setZoomingCompletedCallbackListener(this);
                mZoomControl.setZoom(zoomValue);
            } 

            enableSelectedMode();

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
    	RelativeLayout sv = (RelativeLayout) findViewById(R.id.main_camera_frame);
    	ViewGroup.LayoutParams sv_p = sv.getLayoutParams();
    	if(surface_ratio >= preview_ratio){
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

    	}else{
    		
    	}
    	sv.setLayoutParams(sv_p);
    }
    @Override
    public void onZoomingCompleted() {       
        mZoomControl.setZoomingCompletedCallbackListener(null);
        enableSelectedMode();
    }    

    /**
     * Turns on active camera mode.
     */
    private void enableSelectedMode() {
        Camera.Parameters parameters = mCurrentCameraParameters[mCurrentCameraIndex];

        try {
            // Enable selected mode.
            if (mActiveMode != null) {
                if (mActiveMode.equals(CameraInfo.CAMERA_MODE_BURST)) {
                	mCamera.stopPreview();
                	parameters.set(CameraInfo.CAMERA_MODE_BURST, CameraInfo.BURST_MODE_DEFAULT_VALUE);

                    mCamera.setParameters(parameters);



                    mCamera.startPreview();
                    
                } else if (mActiveMode.equals(CameraInfo.CAMERA_MODE_PANORAMA)) {
                	mCamera.stopPreview();
                	parameters.set(CameraInfo.CAMERA_MODE_PANORAMA, CameraInfo.CAMERA_MODE_ENABLED_DEFAULT_VALUE);

                    mCamera.setParameters(parameters);

 

                    mCamera.startPreview();
                    
                }  
                
                else if (mActiveMode.equals(CameraInfo.CAMERA_MODE_ZSL)) { 
                    mCamera.stopPreview();
                    parameters.set(mActiveMode, CameraInfo.CAMERA_MODE_ENABLED_DEFAULT_VALUE);
                    mCamera.setParameters(parameters);



                    mCamera.startPreview();
                } else if (!mActiveMode.equals(CameraInfo.CAMERA_MODE_AUTO)) {
                    parameters.set(mActiveMode, CameraInfo.CAMERA_MODE_ENABLED_DEFAULT_VALUE);   
                }
                mCamera.setParameters(parameters);
            }

            // Update UI.
            mCurrentCameraParameters[mCurrentCameraIndex] = parameters;
            mPreviewing = true;
            mModeSelectionListener.setCamera(mCamera);
            mResolutionChangeListener.setSizes(getAvailableSizes());
            mResolutionValue.assignSize(parameters.getPictureSize());

            // Enable capturing.
            setPanelEnabled(true);
            mShutterButton.setClickable(true);
        }
        catch (Exception ex) {
            Logger.logApplicationException(ex, "CameraActivity.enableSelectedMode() failed.");
              
            if(enableSelectModeRetryTimes-- > 0){
            	enableSelectedMode();
            }
        }
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                mGallery.update(getContentResolver(), ThumbnailControl.TYPE_IMAGE);

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
     * Shows Progress dialog.
     */
    private void showProgressDialog(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        mProgressDialog.setMessage("Processing ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }

    /**
     * Hides progress dialog.
     */
    private void hideProgressDialog(){
        // clean the view from the Context
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    

    /**
     * Takes picture.
     * 
     * @param camera Camera.
     */
    private void takePicture(Camera camera) {
    	
        // KPI logging
        updateTime();
        Logger.logMessage("Camera.takePicture() called on " + DateTimeUtils.formatDate(mCurrentDate) + ", time diff: " + getTimeDiff());

        mThumbnailsDataList.clear();
        mLastPhotoBuffer = null;
        if (CameraInfo.CAMERA_MODE_BURST.equals(mActiveMode)) {
            mBurstImages = CameraInfo.BURST_MODE_DEFAULT_VALUE - 1;
            mThumbnailNumber = CameraInfo.BURST_MODE_DEFAULT_VALUE;
            mShutterButton.setClickable(false);
            mBurstScroll.createSeed();
            setPanelEnabled(false);
        } else if (CameraInfo.CAMERA_MODE_HDR.equals(mActiveMode)) {
            mBurstImages = CameraInfo.HDR_IMAGE_COUNT_VALUE;
            mThumbnailNumber = CameraInfo.HDR_IMAGE_COUNT_VALUE;
            mShutterButton.setClickable(false);
            setPanelEnabled(false);
        } else if (CameraInfo.CAMERA_MODE_PANORAMA.equals(mActiveMode)) {
            setPanelEnabled(false);
            mShutterButton.setClickable(false);
        } else {
            mBurstImages = 0;
        }


        mPanoramaThumb.setOnClickListener(null);

        // Stop ROI focus on any mode except Auto.
        Parameters params = mCurrentCameraParameters[mCurrentCameraIndex];
        if ((mActiveMode != null) && CameraInfo.FOCUS_MODE_ROIFOCUS.equalsIgnoreCase(params.getFocusMode())) {
            params.setFocusMode(CameraInfo.FOCUS_MODE_AUTO);
            camera.setParameters(params);
            mRestoreFocusMode = true;
        }
        
        if(CameraInfo.CAMERA_MODE_AUTO.equals(mActiveMode)){
        	camera.takePicture(mShutterCallback, mRAWPictureCallback, mPostViewCallback, mJPEGPhotoCallback);
        }else{
        	camera.takePicture(mShutterCallback, null, mPostViewCallback, mJPEGPhotoCallback);
        }
        

        mPreviewing = false;
        mAutoFocusCallback = null;
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
            try {
            	dispDim.x = width;
            	dispDim.y = height;
            	
            	

                mPreviewHolder = holder;
                mSurfaceWasDestroyed = false;
                if (!mPreviewing) {
                    startCameraPreview(width, height);
                } else {
                    mCamera.setPreviewDisplay(mPreviewHolder);
                }
            } catch (Exception e) {
                Logger.logApplicationException(e, "CustomSurfaceHolderCallback.surfaceChanged: failed to start preview");
            }
            

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceWasDestroyed = true;
        }
    };
    public Camera.ShutterCallback getShutterCallback(){
    	return mShutterCallback;
    }
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
		
		@Override
		public void onShutter() {
		}
	};
	 private void resetCamera(){
		 Log.d(TAG, "entering resetCamera()");
		 mCamera.stopPreview();                
         Camera.Parameters params = mDefaultCameraParameters[mCurrentCameraIndex];
         mCurrentCameraParameters[mCurrentCameraIndex] = params;
         mCamera.setParameters(params);

         // Update UI
         mModeSelector.setVisibility(View.GONE);
         mResolutionValue.assignSize(params.getPictureSize()); 
         mModeSelectionListener.setCamera(mCamera);
         if (params.isZoomSupported()) {
             mZoomControl.onZoomChange(params.getZoom(), true, mCamera);
         }
         String activeMode = CameraInfo.getActiveMode(mCamera);
         onModeSelected(activeMode);
        
         startPreviewWithOptimalParameters(mPreview.getWidth(), mPreview.getHeight());
	 }
	 

	 public Camera.PictureCallback getJPEGPictureCallback(){
	    	return mJPEGPhotoCallback;
	    }
	 private boolean jpegDataNull = true;
	 public boolean isJPEGDataNull(){
		 return jpegDataNull;
	 }
	/**
     * Picture JPEG callback from camera.
     */
    private Camera.PictureCallback mJPEGPhotoCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	Log.i(TAG, "mJPEGPhotoCallback -> onPictureTaken");
        	Log.i(TAG, "is data null : " + (data == null));
            
            // KPI logging.
            updateTime();
            Logger.logMessage("Camera.PictureCallback.onPictureTaken() JPEG called at " + DateTimeUtils.formatDate(mCurrentDate) + ", time diff: " + getTimeDiff());
            jpegDataNull = (data == null);
            try {
                if ((mBurstImages == 0) && (CameraInfo.CAMERA_MODE_HDR.equals(mActiveMode))) {
                    mLastPhotoBuffer = data;
                } else {
                    new SavePhotoAction().execute(data, new byte[] {(byte) mBurstImages});
  
                }

                if (mBurstImages > 0) { 
                    mBurstImages--;
                } else if (mRestoreFocusMode) {
                    // Restore previous focus mode.
                    mCurrentCameraParameters[mCurrentCameraIndex].setFocusMode(mFocusMode);
                    mCamera.setParameters(mCurrentCameraParameters[mCurrentCameraIndex]);
                    mRestoreFocusMode = false;
                }

                if (!mPreviewing && !CameraInfo.CAMERA_MODE_BURST.equals(mActiveMode) && !CameraInfo.CAMERA_MODE_PANORAMA.equals(mActiveMode)) { 

                    //Changes for Roi based AE: Release AE locks
                    Parameters parameters = mCamera.getParameters();
                    parameters.setAutoExposureLock(false);
                    mCamera.setParameters(parameters);
                    //End
                	mCamera.startPreview();
                    mPreviewing = true;
                }
              
            } catch(final Exception e) {
                Logger.logApplicationException(e, "CameraActivity.mJPEGPhotoCallback.onPictureTaken() failed.");
            }
            if(runningTest && signal != null){
            	signal.countDown();
            }
        }
    };
    public Camera.PictureCallback getRawPictureCallback(){
    	return mRAWPictureCallback;
    }
    
    private boolean rawDataNull = true;
    public boolean isRawDataNull(){
    	return rawDataNull;
    }
    /**
     * Picture RAW callback from camera.
     */
    private Camera.PictureCallback mRAWPictureCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera cam) {
			Log.i(TAG, "mRAWPictureCallback -> onPictureTaken");
	       	
            // KPI logging.
            updateTime();
            Logger.logMessage("Camera.PictureCallback.onPictureTaken() RAW called at " + DateTimeUtils.formatDate(mCurrentDate) + ", time diff: " + getTimeDiff());
            
            rawDataNull = (data == null);
            Log.i(TAG, "RAW capture data length : " + (data == null ? "null" : data.length));
            try{
                
            	if(data != null){
            		new SavePhotoAction().execute(data, new byte[] {(byte) mBurstImages});
            	}
            	
            }catch(final Exception e) {
                Logger.logApplicationException(e, "CameraActivity.mRAWPictureCallback.onPictureTaken() failed.");
            }
            if(runningTest && signal != null){
            	signal.countDown();
            }
        }
    	
    };

    public Camera.PictureCallback getPostViewCallback(){
    	return mPostViewCallback;
    }
    private boolean postViewDataNull = true;
    public boolean isPostViewDataNull(){
    	return postViewDataNull;
    }
    /**
     * Postview callback to set thumbnails for HDR/Burst modes.
     */
    private Camera.PictureCallback mPostViewCallback = new Camera.PictureCallback() {
    	
    	Bitmap pano_bmp = null;
        
    	@Override
        public void onPictureTaken(byte[] data, Camera camera) {
    		Log.i(TAG, "mPostViewCallback -> onPictureTaken");
    		postViewDataNull = (data == null);
    		if(data == null){
    			Log.e(TAG, "PostView data is null");
    		}
        	if (CameraInfo.CAMERA_MODE_BURST.equals(mActiveMode) || CameraInfo.CAMERA_MODE_HDR.equals(mActiveMode)) {
//        		benchBytoToBitmapTime(data);
                new UpdateThumbnailAction(getThumbnailNumber()).execute(data);
            } else if (CameraInfo.CAMERA_MODE_PANORAMA.equals(mActiveMode)) {
            	
            	if (data != null) {
            		
            		YuvImage yuvi = new YuvImage(data, ImageFormat.NV21, PANORAMA_PREVIEW_WIDTH, PANORAMA_PREVIEW_HEIGHT, null);
     	            ByteArrayOutputStream out = new ByteArrayOutputStream();
     	            yuvi.compressToJpeg(new Rect(0, 0, PANORAMA_PREVIEW_WIDTH, PANORAMA_PREVIEW_HEIGHT), 100, out);
     	            try {
     	                
     	            	byte[] imageBytes = out.toByteArray();
     	            	pano_bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
     	              	                
     	            } catch (Exception e) {
     	                Logger.logApplicationException(e, "Exception in creating thumbnail");
     	            }	
                	 
                   
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        	mPanoramaThumb.setImageBitmap(pano_bmp);
                        }
                    });
                }
            }
        	if(runningTest && signal != null){
             	signal.countDown();
             }
        }

    };   
    
    private void benchBytoToBitmapTime(byte[]... data){
    	long start = System.currentTimeMillis();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        if (previewSize == null) {
            previewSize = mCurrentCameraParameters[mCurrentCameraIndex].getPreviewSize();
        }

        int arrayWidth = previewSize.width;
        int arrayHeight = previewSize.height;
        String log_size = "(" + arrayWidth + ", " + arrayHeight + ")";

        int scaledThumbWidth = BURST_PREVIEW_WIDTH;
        int scaledThumbHeight = BURST_PREVIEW_HEIGHT;
        
        Bitmap bmp = null;
        
    	YuvImage yuvi = new YuvImage(data[0], ImageFormat.NV21, arrayWidth, arrayHeight, null) ;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvi.compressToJpeg(new Rect(0, 0, scaledThumbWidth, scaledThumbHeight), 100, out);
        try {
            
        	byte[] imageBytes = out.toByteArray();
        	bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        	Log.e(TAG, "benchBytoToBitmapTime  at preview size " + log_size + " is :" + (System.currentTimeMillis() - start) + "ms");
            
        } catch (Exception e) {
            Logger.logApplicationException(e, "Exception in creating thumbnail");
            
        }
    }
    
    /**
     * Returns current thumbnail number.
     * 
     * @return Current thumbnail number.
     */
    private synchronized int getThumbnailNumber() {
        int index = 0;
        if (CameraInfo.CAMERA_MODE_BURST.equals(mActiveMode)) {
            index = CameraInfo.BURST_MODE_DEFAULT_VALUE - mThumbnailNumber;
        } else if (CameraInfo.CAMERA_MODE_HDR.equals(mActiveMode)) {
            index = CameraInfo.HDR_IMAGE_COUNT_VALUE - mThumbnailNumber;
        }
        mThumbnailNumber--;
        return index;
    }

 

    /**
     * Async task to update thumbnail.
     * 1) make the thumbnail view visible with blank image
     * 2) compress the captured image to thumbnail size
     * 3) set the thumbnail to the view
     */
    private class UpdateThumbnailAction extends AsyncTask<byte[], Bitmap, String> {
        /**
         * Index of the thumbnail being updated.
         */
        private int index;

        public UpdateThumbnailAction (int thumbnailnumber) {
            index = thumbnailnumber;
        }

        @Override
        protected void onPreExecute (){
            playSound(mThumbnailSoundId);

            mBurstScroll.createBlank(index);
        }

        @Override
        protected String doInBackground(byte[]... data) {
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            if (previewSize == null) {
                previewSize = mCurrentCameraParameters[mCurrentCameraIndex].getPreviewSize();
            }

            int arrayWidth = previewSize.width;
            int arrayHeight = previewSize.height;


            int scaledThumbWidth = BURST_PREVIEW_WIDTH;
            int scaledThumbHeight = BURST_PREVIEW_HEIGHT;
            
            Bitmap bmp = null;
            
        	YuvImage yuvi = new YuvImage(data[0], ImageFormat.NV21, arrayWidth, arrayHeight, null) ;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvi.compressToJpeg(new Rect(0, 0, scaledThumbWidth, scaledThumbHeight), 100, out);
            try {
                
            	byte[] imageBytes = out.toByteArray();
            	bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                
                
            } catch (Exception e) {
                Logger.logApplicationException(e, "Exception in creating thumbnail");
                
            }

            publishProgress(bmp);
            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... bmp) {
            final String currentMode = mActiveMode;
            
            if(CameraInfo.CAMERA_MODE_BURST.equals(currentMode) ||
            		CameraInfo.CAMERA_MODE_HDR.equals(currentMode)){
            	mBurstScroll.addThumbBitmap(index, bmp[0]);
            }

        }
  
        @Override
        protected void onPostExecute (String result) {
        	
        }
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
    private String savedFileName;
    public String getSavedFileName(){
    	return savedFileName;
    }
    /**
     * Async task to save photo on SD card.
     */
    private class SavePhotoAction extends AsyncTask<byte[], Object, Integer> {

        @Override
        protected Integer doInBackground(final byte[]... jpeg) {
            try {
                // Remember mode the task was called with.
                final String currentMode = mActiveMode;
                final int currentBurst = jpeg[1][0];

                String state = android.os.Environment.getExternalStorageState();
                if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
                    throw new IOException("Couldn't find SD card.");
                }

                if ((jpeg[0] == null) || (jpeg[0].length == 0)) {
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

                if ((CameraInfo.CAMERA_MODE_HDR.equals(currentMode) && currentBurst == 0) || CameraInfo.CAMERA_MODE_PANORAMA.equals(currentMode)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgressDialog();
                        }
                    });

                    if (CameraInfo.CAMERA_MODE_PANORAMA.equals(currentMode)) {
                        playSound(mThumbnailSoundId);
                    }
                }

                // Update thumbnail.
                if(currentBurst == 0) {
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
                }

                if (CameraInfo.CAMERA_MODE_BURST.equals(currentMode) || CameraInfo.CAMERA_MODE_HDR.equals(currentMode)) {
                    if (CameraInfo.CAMERA_MODE_BURST.equals(currentMode) && currentBurst == CameraInfo.BURST_MODE_DEFAULT_VALUE - 1) {
                        mBurstFileName = getPhotoName();
                    } else if(CameraInfo.CAMERA_MODE_HDR.equals(currentMode) && currentBurst == CameraInfo.HDR_IMAGE_COUNT_VALUE) {
                        mBurstFileName = getPhotoName();
                    }
                }

                savedFileName = getPhotoPath(currentBurst, currentMode, mBurstFileName);

                File directory = new File(savedFileName).getParentFile();
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IOException("Couldn't create savedFileName to file.");
                }

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(savedFileName);
                    fileOutputStream.write(jpeg[0]);
                    fileOutputStream.close();

                    // KPI Logging.
                    updateTime();
                    Logger.logMessage("Saved photo in " + savedFileName + " at " + DateTimeUtils.formatDate(mCurrentDate) + ", time diff: " + getTimeDiff());
                }
                catch (java.io.IOException e) {
                    Logger.logApplicationException(e, "CameraActivity.SavePhotoAction() failed. Couldn't save the file");
                }

                // Do not put information about panorama images to the gallery.
                if (currentMode != null && currentMode.equals(CameraInfo.CAMERA_MODE_PANORAMA)) {
                	
                    hideProgressDialog();
                	
                    String name = savedFileName.substring(savedFileName.lastIndexOf("/") + 1);
                    ContentValues values = new ContentValues();
                    values.put(Images.Media.TITLE, name);
                    values.put(Images.Media.DISPLAY_NAME, name);
                    values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(Images.Media.DATA, savedFileName);
                    values.put(Images.Media.SIZE, jpeg[0].length);
                    getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);

                    mPanoramaThumb.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setDataAndType(Uri.fromFile(new File(savedFileName)), "image/*");
                            intent.setAction(Intent.ACTION_VIEW);

                            startActivityForResult(Intent.createChooser(intent, "View panorama"), 0);
                        }
                    });                    

                    return 0;
                }

                String name = savedFileName.substring(savedFileName.lastIndexOf("/") + 1);

                ContentValues values = new ContentValues();
                values.put(Images.Media.TITLE, name);
                values.put(Images.Media.DISPLAY_NAME, name);
                values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(Images.Media.MIME_TYPE, "image/jpeg");
                values.put(Images.Media.DATA, savedFileName);
                values.put(Images.Media.SIZE, jpeg[0].length);

                final Uri uri = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);

                // KPI logging.
                updateTime();
                Logger.logMessage("Thumbnail loaded at " + DateTimeUtils.formatDate(mCurrentDate) + ", time diff: " + getTimeDiff());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(currentBurst == 0) {
                            mGallery.setUri(uri);                            

                            if (CameraInfo.CAMERA_MODE_HDR.equals(currentMode)) {
                                Bitmap lastThumbnail = null;
                                String stringUri = uri.toString(); 
                                int imgId = Integer.parseInt(stringUri.substring(stringUri.lastIndexOf("/")+1));
                                lastThumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), imgId, MediaStore.Images.Thumbnails.MINI_KIND, null);
                                mGallery.setThumbnail(lastThumbnail, uri);

                                hideProgressDialog();

                            }

                            mGallery.show();
                        }
                    }
                });

                System.gc();
                return currentBurst;
            }
            catch(final Exception e) {
                Logger.logApplicationException(e, "CameraActivity.SavePhotoAction.doInBackground() failed.");
            }

            return 0;            
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            Bitmap bmp = (Bitmap)values[0];
            Integer thumbNumber = (Integer)values[1];

            playSound(mThumbnailSoundId);
        }

        @Override
        protected void onPostExecute(Integer currentBurst) {
        	if(runningTest && signal != null){
            	signal.countDown();
            }
        	checkSdSize();

            if (CameraInfo.CAMERA_MODE_PANORAMA.equals(mActiveMode) || ((currentBurst == 0) && (!CameraInfo.CAMERA_MODE_HDR.equals(mActiveMode)))) {
            	if ((!mPreviewing) && (mCamera != null)) {
                    try {

                        mPreviewing = true;
                        mCamera.startPreview();
                    } catch (Exception ex) {
                        Logger.logApplicationException(ex, "SavePhotoAction.onPostExecute(): failed to start preview");
                    }
                }
                mShutterButton.setClickable(true);
                setPanelEnabled(true);
                
                //TODO REMOVE WHEN N-BURST works
                Log.e(TAG, "SavePhotoAction");
                if(mBurstScroll.continueFakeBurst()){
                	Log.e(TAG, "calling fake burst onclick");
                	mShutterButton.callOnClick();
                }
//                else if(CameraInfo.CAMERA_MODE_BURST.equals(mActiveMode)){
//                	Intent gridGalleryintent = new Intent(getApplicationContext(), GridGalleryActivity.class);
//					startActivity(gridGalleryintent);
//                }
            }

            if (CameraInfo.CAMERA_MODE_HDR.equals(mActiveMode) && (currentBurst == 1)) {
                mBurstImages = 0;
                new SavePhotoAction().execute(mLastPhotoBuffer, new byte[] {0});
            }

            if (currentBurst > 0) {
                mCamera.takePicture(null, null, null, mJPEGPhotoCallback);
            } else if ((!mPreviewing) && (mCamera != null)) {
                try {

                    mPreviewing = true;
                    mCamera.startPreview();
                } catch (Exception ex) {
                    Logger.logApplicationException(ex, "SavePhotoAction.onPostExecute(): failed to start preview");
                }
            }
            if(CameraInfo.CAMERA_MODE_AUTO.equals(mActiveMode)){
            	mShutterButton.setClickable(true);
            }
            
            if(runningTest && signal != null){
            	signal.countDown();
            }
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
        private String getPhotoPath(int index, String mode, String givenName) {
            try {
                // Special folder for panorama images.
                if (mode != null && mode.equals(CameraInfo.CAMERA_MODE_PANORAMA)) {
                    String fileName = givenName == null ? getPhotoName() : givenName;
                    return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Aptina/Panorama/" + fileName + ".jpg";
                }

                String fileName = givenName == null ? getPhotoName() : givenName;
                if (mode != null) {
                    if (mode.equals(CameraInfo.CAMERA_MODE_BURST)) {
                        int photoIndex = CameraInfo.BURST_MODE_DEFAULT_VALUE - index;
                        fileName = "BUR-" + photoIndex + "-" + fileName;
                    } else if (mode.equals(CameraInfo.CAMERA_MODE_HDR)) {
                        if (index == 0) {
                            fileName = "HDR-Final" + "-" + fileName;
                        } else {
                            int photoIndex = CameraInfo.HDR_IMAGE_COUNT_VALUE - index + 1;
                            fileName = "HDR-" + photoIndex + "-" + fileName;
                        }
                    }
                }

                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/" + fileName + ".jpg";

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
                Logger.logApplicationException(e, "CameraActivity.SavePhotoAction.getPhotoPath() failed.");
            }
            return null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                int curZoom = mCamera.getParameters().getZoom();
                if (curZoom > 0) {
                    if (mCamera.getParameters().isSmoothZoomSupported()) {
                        mCamera.startSmoothZoom(curZoom - 1);
                    } else if (mCamera.getParameters().isZoomSupported()) {
                        Parameters params = mCamera.getParameters();
                        params.setZoom(curZoom - 1);
                        mCamera.setParameters(params);
                        mZoomControl.onZoomChange(curZoom - 1, true, mCamera);
                    }
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                Parameters parameters = mCamera.getParameters();
                int curZoom = parameters.getZoom();
                int maxZoom = parameters.getMaxZoom();

                if (curZoom < maxZoom) {
                    if (mCamera.getParameters().isSmoothZoomSupported()) {
                        mCamera.startSmoothZoom(curZoom + 1);
                    } else if (mCamera.getParameters().isZoomSupported()) {
                        Parameters params = mCamera.getParameters();
                        params.setZoom(curZoom + 1);
                        mCamera.setParameters(params);
                        mZoomControl.onZoomChange(curZoom + 1, true, mCamera);
                    }
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mModeSelector.getVisibility() == View.VISIBLE) {
                    mModeSelector.setVisibility(View.GONE);
                    return true;
                }
            }
        }
        catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.onKeyDown() failed");
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
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
     * Auto focus callback listener. 
     */
    private class CustomAutoFocusCallBack implements Camera.AutoFocusCallback {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            try {                
                takePicture(camera);
            } catch(final Exception e) {
                Logger.logApplicationException(e, "CameraActivity.CustomAutoFocusCallBack.onAutoFocus() failed.");
            }
        }
    }

    @Override
    public void onModeSelected(String mode) {
    	long mStartuptime = System.currentTimeMillis();
    	if(mode == null){ //the mode becomes null if it is auto, since auto is inserted by default
    		mode = CameraInfo.CAMERA_MODE_AUTO;
    	}
        String currentMode = mActiveMode;
        mPreviewModeSelected = mActiveMode;
        
        Parameters params = mCamera.getParameters();

        mBurstFileName = null;
        
        mPanoramaThumb.setVisibility(View.GONE);

        try {
            // Enable selected mode.
            mActiveMode = mode;
            handleViews();
            
            //TODO:Reset the zoom to 0 until modes other than auto support zooming fully
            params.setZoom(0);
            mZoomControl.setZoom(0);
            
            //Set the capture resolution for auto and ZSL
            Size[] availableSizes = getAvailableSizes();
            mResolutionChangeListener.setSizes(availableSizes);
            Camera.Size p_s = setResolutionSizeValue(availableSizes);
            params.setPictureSize(p_s.width, p_s.height);

            if (mode != null) {
            	params = toggleFocusZSL(mode, currentMode, params);
                if (mode.equals(CameraInfo.CAMERA_MODE_BURST)) {
                	
                	handleViews();

                    params.set(CameraInfo.CAMERA_MODE_PANORAMA, "disable"); // panorama 
                    params.set(CameraInfo.CAMERA_MODE_ZSL, "disable"); // zsl
                } 
                if (mode.equals(CameraInfo.CAMERA_MODE_HDR)) {
                	handleViews();
                } 
                if (mode.equals(CameraInfo.CAMERA_MODE_PANORAMA)) {
                	Log.d(TAG, "onModeselected ::User selected Panorama mode");
                    mZoomControl.setVisibility(View.GONE);
                    mGallery.setVisibility(View.GONE);
                    mPanoramaThumb.setVisibility(View.VISIBLE);
                    mPanoramaThumb.setImageBitmap(null);
                    // disable all other modes
                    params.set(CameraInfo.CAMERA_MODE_ZSL, "disable"); // zsl
                    params.set(CameraInfo.CAMERA_MODE_BURST, 0); // burst
                    
                } 
                if (mode.equals(CameraInfo.CAMERA_MODE_ZSL)) {
                	Log.d(TAG, "onModeselected ::User selected ZSL mode");
                	mZoomControl.setVisibility(View.GONE); 
                	
                    params.set(CameraInfo.CAMERA_MODE_PANORAMA, "disable"); // panorma
                    params.set(CameraInfo.CAMERA_MODE_BURST, 0); // burst                    
                    
                } 
                if(mode.equals(CameraInfo.CAMERA_MODE_AUTO)){
                    //disable all other modes and all the related UI
                    params.set(CameraInfo.CAMERA_MODE_PANORAMA, "disable"); // panorma
                    params.set(CameraInfo.CAMERA_MODE_BURST, 0); // burst                    
                    params.set(CameraInfo.CAMERA_MODE_ZSL, "disable"); // zsl                		
                	
                }
            }


            if ((mode != null) && (mode.equals(currentMode))) {
                return;
            } else {

            	mCurrentCameraParameters[mCurrentCameraIndex] = quickModeSwitch(params);
            	
            }        

        } catch (RuntimeException ex) {
        	Log.e(TAG, "onModeSelected RuntimeException ex");
            Logger.logApplicationException(ex, "CameraActivity.onModeSelected(): failed to set mode " + mode);
            Toast.makeText(getApplicationContext(), "error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        long timeD = System.currentTimeMillis() - mStartuptime ;
        Log.i(TAG, "Mode Select TIME taken = " + timeD);
    }
    
    /**
     * Toggle the focus between infinity and whatever it was before
     * the user switched to ZSL. Infinity mode needs to be used in ZSL
     * to prevent auto-focus AFTER image capture
     * 
     * @param toMode focus mode switching to
     * @param fromMode focus mode currently on
     * @param Current camera parameters to modify 
     * @return The modified camera params with the properly set focus mode
     */
    private Camera.Parameters toggleFocusZSL(String toMode, String fromMode, Camera.Parameters params){
    	if(toMode.equalsIgnoreCase(CameraInfo.CAMERA_MODE_ZSL)){
    		/*
        	 * Camera refocuses after capture if not set to a static focus mode.
        	 */
    		if(params.getFocusMode() != null){
    			mNonZSLFocusMode = params.getFocusMode();
    		}
        	params.setFocusMode(CameraInfo.FOCUS_MODE_INFINITY);
    	}else{
    		params.setFocusMode(mNonZSLFocusMode != null ? mNonZSLFocusMode : CameraInfo.FOCUS_MODE_AUTO);
    	}
    	Log.d(TAG, "focus set to : " + params.getFocusMode());
    	return params;
    }
    
    private Camera.Parameters quickModeSwitch(Camera.Parameters params){
        // Disable capturing on time of mode change.
        setPanelEnabled(false);
        mShutterButton.setClickable(false);
    	boolean special_mode = false;    
      
        //OMAP4 Bug
   	 	List<Integer> frameRates = params.getSupportedPreviewFrameRates();
   	    int min = 15;
   	 	int max = 30;

        if (frameRates != null) {
            max = Collections.max(frameRates);
            min = Collections.min(frameRates);
        } //OMAP4 : END

        
    	if(mActiveMode.equals(CameraInfo.CAMERA_MODE_BURST)){
    		params.set(CameraInfo.CAMERA_MODE_BURST, CameraInfo.BURST_MODE_DEFAULT_VALUE);
    		params.setPreviewFrameRate(max); //OMAP4 BUG

         	special_mode = true;
    	}else if(mActiveMode.equals(CameraInfo.CAMERA_MODE_PANORAMA)){
    		params.set(CameraInfo.CAMERA_MODE_PANORAMA, CameraInfo.CAMERA_MODE_ENABLED_DEFAULT_VALUE);
    		params.setPreviewFrameRate(min); //OMAP4 BUG

         	special_mode = true;
    	}else if (mActiveMode.equals(CameraInfo.CAMERA_MODE_ZSL)) { 
    		params.set(mActiveMode, CameraInfo.CAMERA_MODE_ENABLED_DEFAULT_VALUE);
    		params.setPreviewFrameRate(max); //OMAP4 BUG
    		special_mode = true;
    	}else if (!mActiveMode.equals(CameraInfo.CAMERA_MODE_AUTO)) {
    		params.set(mActiveMode, CameraInfo.CAMERA_MODE_ENABLED_DEFAULT_VALUE);   
    		params.setPreviewFrameRate(max); //OMAP4 BUG
        }
    	Size[] previewSizes = getPreviewSizes();

    	params.setPreviewSize(previewSizes[0].width, previewSizes[0].height);
    	if(special_mode){

    		mCamera.stopPreview();

    		mCamera.setParameters(params);


    		mCamera.startPreview();
    	}
    	// check if we are moving from special mode to normal mode 
    	// then we would restart else we need not since we are in normal mode only	
    	else if (!mPreviewModeSelected.equalsIgnoreCase(CameraInfo.CAMERA_MODE_AUTO) && 
    			mActiveMode.equalsIgnoreCase(CameraInfo.CAMERA_MODE_AUTO) ){
    		//
    		mCamera.stopPreview();
    		mCamera.setParameters(params);    		
    		mCamera.startPreview();
    	}
    	else {
    		mCamera.setParameters(params);
    	}
        // Update UI.
        mPreviewing = true;
        mModeSelectionListener.setCamera(mCamera);
        mResolutionChangeListener.setSizes(getAvailableSizes());
        mResolutionValue.assignSize(params.getPictureSize());

        // Enable capturing.
        setPanelEnabled(true);
        mShutterButton.setClickable(true);
        
    	return params;

    }

    /**
     * We have to check that the old resolution size is available with the new 
     * mode, otherwise we end up in an infinite loop of exceptions being thrown
     *  
     * @param availableSizes the picture sizes available in this resolution
     */
    private Camera.Size setResolutionSizeValue(Size [] availableSizes){
         Size currentSize = mCamera.getParameters().getPictureSize();
         Size newSize = null;
         for(Size size : availableSizes){
         	if(size.equals(currentSize)){
         		newSize = size;
         	}
         }
         Camera.Parameters params = mCurrentCameraParameters[mCurrentCameraIndex];
         if(newSize != null){// if supported, keep the same picture size
         	 mResolutionValue.assignSize(newSize);  
         	params.setPictureSize(newSize.width, newSize.height);
         	 
         }else{// if not supported, set new size to largest available for the mode
         	mResolutionValue.assignSize(availableSizes[0]);
         	params.setPictureSize(availableSizes[0].width, availableSizes[0].height);
         }
         mCurrentCameraParameters[mCurrentCameraIndex] = params;
         
         Log.i(TAG, "setResolutionSizeValue set the picture size to : (w,h) : (" + params.getPictureSize().width + ", " + params.getPictureSize().height + ")");
         
         return mCurrentCameraParameters[mCurrentCameraIndex].getPictureSize();
    }




    
    /**
     * Alternate between the different UI widgets depending
     * on the current camera mode
     */
    private void handleViews(){
    	if(mActiveMode.equals(CameraInfo.CAMERA_MODE_BURST) || mActiveMode.equals(CameraInfo.CAMERA_MODE_HDR)){
    		showBurstThumbnail();
    	}else{
    		hideBurstThumbnail();
    	}
    }
    /**
     * Hides burst thumbnails control.
     */
    private void hideBurstThumbnail() {
    	Log.w(TAG, "hideBurstThumbnail visibility :  " + mBurstScroll.getVisibility());
    	mBurstScroll.setVisibility(View.GONE);
    	mBurstScroll.setClickable(false);
    	
        if (mCamera.getParameters().isSmoothZoomSupported() || mCamera.getParameters().isZoomSupported()) {
            mZoomControl.setVisibility(View.VISIBLE);
            mZoomControl.setClickable(true);
        }
        mGallery.setVisibility(View.VISIBLE);

    }
    
    /**
     * Shows burst thumbnails control.
     */
    private void showBurstThumbnail() {
    	Log.w(TAG, "showBurstThumbnail visibility :  " + mBurstScroll.getVisibility());
        mZoomControl.setVisibility(View.GONE);
        mZoomControl.setClickable(false);
        mGallery.setVisibility(View.GONE);
//        mBurstThumbnailsComponent.setVisibility(View.VISIBLE);
        mBurstScroll.setVisibility(View.VISIBLE);
        mBurstScroll.setClickable(true);
        
        
    }
   

    /**
     * Sets panel buttons clickable/disabled.
     * 
     * @param enabled <b>true</b> to enable panel buttons, <b>false</b> to disable.
     */
    private void setPanelEnabled(boolean enabled) {
        mModeSelectionButton.setClickable(enabled);
        mResolutionButton.setClickable(enabled);
        mResetButton.setClickable(enabled);
        mOptionsButton.setClickable(enabled);
        mSwitchButton.setClickable(enabled);
    }

    /**
     * Updates previos & current time properties.
     */
    private void updateTime() {
        mPreviousDate = mCurrentDate;
        mCurrentDate = new Date();
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
     * Plays sound specified by the given ID.
     * 
     * @param soundId ID of the sound in the sound pool.
     */
    private void playSound(int soundId) {
        if (mSoundPool != null) {
            mSoundPool.play(soundId, 100.0f, 100.0f, 1, 0, 1);
        }
    }

    @Override
    public void onResolutionSelected(Size resolution, int index) {
        Camera.Parameters parameters = mCamera.getParameters();

        // Stop preview to prevent crash on taking picture.   
        if(mActiveMode != CameraInfo.CAMERA_MODE_AUTO){
        	mCamera.stopPreview();
        }

        parameters.setPictureSize(resolution.width, resolution.height);
        mCurrentCameraParameters[mCurrentCameraIndex] = parameters;

        mCamera.setParameters( mCurrentCameraParameters[mCurrentCameraIndex]);

        if(mActiveMode != CameraInfo.CAMERA_MODE_AUTO){
        	startPreviewWithOptimalParameters(resolution.width, resolution.height);
        }


    }

    /**
     * Retrieves Array of available sizes.
     * 
     * @return Array of available sizes.
     */
    public Size[] getAvailableSizes() {
        Size[] pictureSizes = CameraInfo.sortPictureSizes(mDefaultCameraParameters[mCurrentCameraIndex], mCamera, mActiveMode);
        if (pictureSizes == null || pictureSizes.length == 0) {
            pictureSizes = CameraInfo.sortPictureSizes(mCurrentCameraParameters[mCurrentCameraIndex], mCamera, mActiveMode);
        }

        return pictureSizes;
    }
    
    /**
     * Retrieves Array of available preview sizes
     * 
     * @return Array of available preview sizes.
     */
    private Size[] getPreviewSizes(){
    	return CameraInfo.getZSLPreviewSizes(mDefaultCameraParameters[mCurrentCameraIndex], mCamera, mActiveMode,false);
    }

    @Override
    public Size getCurrentResolution() {
        Size size = null;
        try {
            size = mCamera.getParameters().getPictureSize();
            if (size == null) {
                size = mCurrentCameraParameters[mCurrentCameraIndex].getPictureSize();
            }
        } catch(final Exception e) {
            Logger.logApplicationException(e, "CameraActivity.getCurrentResolution(): Failed.");
        }
        return size;
    }
	
	
}
