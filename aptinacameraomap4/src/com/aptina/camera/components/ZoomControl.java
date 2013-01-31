package com.aptina.camera.components;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.OnZoomChangeListener;
import android.hardware.Camera.Parameters;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aptina.R;
import com.aptina.logger.Logger;

/**
 * Implements zoom control.
 * Handles zoom changes and redraws zoom selector.
 * Handles press events on the control.
 */
public class ZoomControl extends RelativeLayout implements OnZoomChangeListener {
	private static final String TAG = "ZoomControl";
    /**
     * Zooming completion callback.
     */
    public interface ZoomingCompletedCallback {
        
        /**
         * Called when zooming is completed.
         */
        public void onZoomingCompleted();
    }
    
    /**
     * Number of zoom points on the control.
     */
    private final static float TOTAL_ZOOM_POINTS = 12.0f;

    /**
     * The width in pixels of the original image
     */
    private final static int ZOOM_SLIDER_IMAGE_WIDTH = 276;
    /**
     * The actual pixel width of the slider in the activity
     */
    private int mZoomSliderWidth = -1;
    /**
     * Width of point on the control, in pixels.
     */
    private final static float POINT_WIDTH_PIXEL = 19;
    /**
     * Relative width of point on the control, as a percentage of image width.
     */
    private final static double POINT_WIDTH_PERCENT = POINT_WIDTH_PIXEL/276;
    /**
     * The actual pixel width of each point in the activity
     */
    private float mPointWidth = -1;
    /**
     * The width of zoom control before first point, in pixels. 
     */
    private final static float WIDTH_BEFORE_FIRST_POINT_PIXELS = 28;
    /**
     * The relative width of zoom control before first point, as a percentage of image width.
     */
    private final static double WIDTH_BEFORE_FIRST_POINT_PERCENT = WIDTH_BEFORE_FIRST_POINT_PIXELS/276;
    /**
     * The actual pixel width before the first point in the slider, in the activity
     */
    private float mWidthBeforeFirstPoint = -1;

    /**
     * Slider image.
     */
    private ImageView mSlider;
    
    /**
     * Zoom selector.
     */
    private ImageView mZoomSelector;

    /**
     * Maximal supported zoom of camera.
     */
    private float mMaxZoom;

    /**
     * Camera instance.
     */
    private Camera mCamera;
    
    /**
     * Callback target for finishing zoom event.
     */
    private ZoomingCompletedCallback mCompletedCallbackListener = null;
    
    /**
     * Creates new instance of the class.
     * 
     * @param context application context.
     */
    public ZoomControl(Context context) {
        super(context);
    }
    public float getTotalZoomPoints(){
    	return TOTAL_ZOOM_POINTS;
    }
    public double getWidthBeforeFirstPointPercent(){
    	return WIDTH_BEFORE_FIRST_POINT_PERCENT;
    }
    public double getZoomSliderPointWidthPercent(){
    	return POINT_WIDTH_PERCENT;
    }
    /**
     * Creates new instance of the class.
     * 
     * @param context application context.
     * @param attrs attributes.
     */
    public ZoomControl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates new instance of the class.
     * 
     * @param context application context.
     * @param attrs attributes.
     * @param defStyle default style.
     */
    public ZoomControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets zooming completed callback listener.
     * 
     * @param listener Listener for zooming completed event.
     */
    public void setZoomingCompletedCallbackListener(ZoomingCompletedCallback listener) {
        mCompletedCallbackListener = listener;
    }
    
    /**
     * Initializes the control.
     * This method must be called before the control will be attached as zoom listener to camera.
     *  
     * @param camera Camera instance.
     * @param device_slider_width The width in pixels of the zoom slider in the application
     */
    public void init(Camera camera) {
        if (camera == null) {
            throw new IllegalArgumentException("ZoomControl.init(Camera camera): camera object is null");
        }
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.zoom_slider, this);
        
        this.mSlider = (ImageView) findViewById(R.id.img_zoom_slider);
        setSliderWidths();
        this.mSlider.setOnTouchListener(mSliderOnTouchListener);
        this.mZoomSelector = (ImageView) findViewById(R.id.img_zoom_selection);
        this.mCamera = camera;
        this.mMaxZoom = camera.getParameters().getMaxZoom();

        
        this.onZoomChange(camera.getParameters().getZoom(), true, camera);
        setSliderWidths();
    }

    @Override
    public void onZoomChange(int newZoom, boolean completed, Camera camera) {
    	
        MarginLayoutParams marginParams = new MarginLayoutParams(mZoomSelector.getLayoutParams());

        int pointToDisplay = Math.round((TOTAL_ZOOM_POINTS - 1) * ((float)newZoom)/mMaxZoom);

        marginParams.setMargins((int)(mWidthBeforeFirstPoint + mPointWidth * pointToDisplay), 0, 0, 0);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        mZoomSelector.setLayoutParams(layoutParams);
        
        if (completed && (mCompletedCallbackListener != null)) {            
            mCompletedCallbackListener.onZoomingCompleted();
        }
    }
    
    private OnTouchListener mSliderOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
        	Log.i(TAG,"zoomslider onTouch");
            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                float x = event.getX();
                x -= mWidthBeforeFirstPoint + mPointWidth/2;
                x /= mPointWidth;
                if (x < 0) x = 0;
                if (x >= TOTAL_ZOOM_POINTS) x = TOTAL_ZOOM_POINTS;
                int newZoom = Math.round(x * mMaxZoom / TOTAL_ZOOM_POINTS);
                return setZoom(newZoom);
            }

            return true;
        }
    };
    
    /**
     * Sets new zoom value for camera.
     * 
     * @param zoomValue Zoom value to apply.
     */
    public boolean setZoom(int zoomValue) {
       try {
           Parameters parameters = mCamera.getParameters();

           // If zoom is already set, invoke completed callback manually.
           if (zoomValue == parameters.getZoom()) {
               if (mCompletedCallbackListener != null) {
                   mCompletedCallbackListener.onZoomingCompleted();
               }
           }
           
           // Set camera zoom.
//           if (parameters.isSmoothZoomSupported()) { // smooth zoom makes pinch zoom look laggy and difficult to control
//               parameters.setZoom(zoomValue);
//               mCamera.startSmoothZoom(zoomValue);
//           } 
//           else {                         
               parameters.setZoom(zoomValue);
               mCamera.setParameters(parameters);
               onZoomChange(zoomValue, true, mCamera);
//           }
                 
           return true;
       } catch (Exception ex) {
           Logger.logApplicationException(ex, "mSliderOnTouchListener.onTouch: failed to set zoom value " + zoomValue);
           return false;
       }
    }

	public void setSliderWidths() {
//		setSliderWidths(zoom_slider.getWidth());
		Log.e(TAG, "mSlider.getWidth() : " + mSlider.getWidth());
        this.mZoomSliderWidth = mSlider.getWidth();
        this.mPointWidth = (float)POINT_WIDTH_PERCENT * (float)mZoomSliderWidth;
        this.mWidthBeforeFirstPoint = (float)WIDTH_BEFORE_FIRST_POINT_PERCENT * (float)mZoomSliderWidth;
	}
    
}
