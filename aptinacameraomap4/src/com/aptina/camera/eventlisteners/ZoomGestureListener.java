package com.aptina.camera.eventlisteners;

import com.aptina.camera.CameraInfo;
import com.aptina.logger.Logger;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
/**
 * @author stoyan
 *
 */
public class ZoomGestureListener implements OnTouchListener{
	/**
	 * String to describe function to logcat
	 */
	private static final String TAG = "ZoomGestureListener";
	

	
	/**
	 * Toggle to turn on/off logging
	 */
	private static final boolean LOG_ON = false;
	public interface ZoomInterface {
//		
		/**
         * Function to return the current camera 
         */
		public Camera getCamera();
//		/**
//         * Set camera's parameters
//         */
//        public void setCameraParams(Camera.Parameters params);
//        /**
//         * Get the current active mode, need to disable zooming in
//         * non auto modes since it is not yet supported
//         */
//        public String getActiveMode();
//        /**
//         * Set the camera parameters array
//         */
//        public void setParamsArray(Camera.Parameters params);
//        /**
//         * Set the autofocus callback of the camera to null
//         */
//        public void setAutoFocusNull();
//        /**
//         * Set the zoom level
//         */
//        public void changeZoom(int to_zoom);
        /**
         * Notify that the distance between the two points has changed
         * @zoom_scale The normalized difference between the last two points
         */
        public void OnZoomChange(float zoom_scale);
	}
	 /**
	  * Max time in milliseconds that the user has to tap the screen twice in order
	  * to trigger ROI focus in auto mode
	  */
	private static final int ROI_FOCUS_MAX_DOUBLE_TAP_TIME = 333;
	PointF start = new PointF();
	PointF mid = new PointF();
	/**
	 * The initial distance between the finger and first pointer down when the 
	 * first pointer touches the screen
	 */
	float initDist = 1f;
	
	/**
	 * The current distance between the finger and the first pointer
	 */
	float lastDist = 0f;
	
	/**
	 * The new distance between the finger and the first pointer
	 */
	float newDist = 0f;
	
	/**
	 * If the pinch gesture does not complete and return the zoom level to 0,
	 * then we need to store the original initDist in order to preserve the 
	 * pinch/zoom ratio
	 */
	float incompleteGestureDist = -1f;
	

	/**
	 * The ratio of the change in distance since the last zoom trigger and the
	 * initial distance between the two fingers
	 * zoom_scale = 1 +  (newDist - lastDist)/ initDist;
	 */
	float zoom_scale = 1f;
	
	/**
	 * The different touch gesture modes that we can be in.
	 * Can only be in one touch gesture mode at any point of time
	 */
	public static final int TOUCH_NONE = 0;
	public static final int TOUCH_ZOOM_ON = 1;
	public static final int TOUCH_ZOOM_OFF = 2;
	
	/**
	 * Variable to store the current gesture mode we are in
	 */
	private int touchMode = TOUCH_NONE;
	
	/**
	 * Variable to hold the value calculated to be the next zoom level that
	 * we need to change to
	 */
	private static int zoom_level = 0;
  

	/**
	 * The context of the camera application
	 */
	private Context mCamContext = null;
	/**
	 * Surface of the preview screen
	 */
	private SurfaceView mPreview = null;
	/**
     * ZoomInterface selection callback target.
     */
    private ZoomInterface mCallbackTarget = null;
    
	public ZoomGestureListener(){
    	
    }

	public void setCallback(ZoomGestureListener.ZoomInterface callback){
		mCallbackTarget = callback;
	}
	
	public ZoomGestureListener.ZoomInterface getCallBackTarget(){
		return mCallbackTarget;
	}
	
	public int getTouchMode(){
		return touchMode;
	}
	
//	public void setZoomRatios(){
//		MAX_ZOOM = mCallbackTarget.getCamera().getParameters().getMaxZoom();
//		ZOOM_RATIO = MAX_ZOOM/MAX_ZOOM_DIVIDOR;
//	}
//	
//	public int getMaxZoom(){
//		return MAX_ZOOM;
//	}
//	
//	public int getMaxZoomDividor(){
//		return MAX_ZOOM_DIVIDOR;
//	}
//	
//	public float getZoomRatio(){
//		return ZOOM_RATIO;
//	}
	
	public int getZoomLevel(){
		return zoom_level;
	}
	
	public float getInitDist(){
		return initDist;
	}
	
	public float getZoomScale(){
		return zoom_scale;
	}
	
	public float getIncompleteGestureDist(){
		return incompleteGestureDist;
	}
	
//	public Camera.Parameters getCameraParams(){
//		return mCallbackTarget.getCamera().getParameters();
//	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		dumpEvent(event);
		// Handle touch events here...
	    switch (event.getAction() & MotionEvent.ACTION_MASK) {
	    case MotionEvent.ACTION_DOWN:
	    	LOGI( "down");
	    	
	        start.set(event.getX(), event.getY());
	        break;
	    case MotionEvent.ACTION_POINTER_DOWN:
	    	LOGI( "pointer down");
	    	initDist = incompleteGestureDist > 0 ? incompleteGestureDist : spacing(event);

	    	lastDist = spacing(event);
	    	LOGI("initDist=" + initDist);
	        if (initDist > 10f) {
	        	touchMode = TOUCH_ZOOM_ON;
	            midPoint(mid, event);
	        }
	        break;
	    case MotionEvent.ACTION_UP:
	    	LOGI( "up ");
	    	
	    	last_touch_down = SystemClock.uptimeMillis();

	    	touchMode = TOUCH_NONE;
	    	break;
	    case MotionEvent.ACTION_POINTER_UP:
//	    	LOGI( "up ");
	    	touchMode = TOUCH_ZOOM_OFF;
	    	/**
	    	 * If the zoom is not back to 0, then the pinch gesture is not complete
	    	 * and we need to store the value of the original fingers down length so that
	    	 * when the user finishes pinching out, the pinch distance to zoom matches the 
	    	 * pinch-in ratio
	    	 */
	    	incompleteGestureDist = mCallbackTarget.getCamera().getParameters().getZoom() > 0 ? 
	    			initDist : -1f;

	        break;
	    case MotionEvent.ACTION_MOVE:
//	    	LOGI( "move");
	    	if(touchMode == TOUCH_ZOOM_ON){
	    		newDist = spacing(event);
//		        Log.d(TAG, "newDist=" + newDist);

		        float dif = newDist - lastDist;
		        
		        if ( Math.abs(dif) > 10f) {
		        	LOGI( "dif = : " + dif);
		        	zoom_scale = 1 +  (newDist - lastDist)/ initDist;
		        	lastDist = newDist; 
		        	LOGI( "scale : " + zoom_scale);
//		        	if(mCallbackTarget.getActiveMode() == CameraInfo.CAMERA_MODE_AUTO){
//		        		setZoomDistance(zoom_scale);
		        		mCallbackTarget.OnZoomChange(zoom_scale);
//		        	}
		        	
		        }
	    	}
	    	
	        break;
	    }//End switch
	    //Indicate event was handled
		return true;
	}
	
	/** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
       // ...
       float x = event.getX(0) - event.getX(1);
       float y = event.getY(0) - event.getY(1);
       LOGI( " spacing = " + FloatMath.sqrt(x * x + y * y));
       return FloatMath.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
       // ...
       float x = event.getX(0) + event.getX(1);
       float y = event.getY(0) + event.getY(1);
       point.set(x / 2, y / 2);
    }
    
    /**
     * Function that calculates how much to zoom based on input scale.
     * The scale, which is the ratio of the current distance between the
     * two fingers with the last distance between the two fingers, is multiplied
     * with the ZOOM_RATIO to give us the zoom that we need to add/subtract from the 
     * current zoom level. The ZOOM_RATIO is defined as the MAX_ZOOM/(A constant > 1)
     * @param scale
     * @return
     */
//    private void setZoomDistance(float scale){
//    	LOGI( "Raw Pinch Scale : " + scale);
//    	Camera.Parameters params = mCallbackTarget.getCamera().getParameters();
//    	//Replace camera_zoom_level with to_zoom when ACSS-1058 is fixed
//    	int current_zoom = params.getZoom();
//    	zoom_level = current_zoom;
//    	LOGI( "current zoom = "+ zoom_level);
//    	
//		
//		//Modify scale for negative and positive zooming
//    	scale -= 1;
//
//		
//		//Modified scale for zooming
//		LOGI( "Modified Pinch Scale : " + scale);
//		zoom_level += scale * ZOOM_RATIO;
//		
//		if(zoom_level > MAX_ZOOM){
//			zoom_level = MAX_ZOOM;
//		}else if(zoom_level < 0){
//			zoom_level = 0;
//		}
//		LOGI( "setting zoom to = " + zoom_level + ", out of a max of = " + MAX_ZOOM);
//		mCallbackTarget.changeZoom(zoom_level);
//
//    }
    //Remember when the user has touched the screen
    static long last_touch_down = 0;
   
    
    /**
     * Log to logcat with a conditional toggle
     * @param msg string to be printed to logcat
     */
    private void LOGI(String msg){
    	if(LOG_ON){
    		Log.i(TAG, msg);
    	}
    }
    
    
    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
    	LOGI("dump event action : " + event.getAction());
    	String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
    	"POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
    	StringBuilder sb = new StringBuilder();
    	int action = event.getAction();
    	int actionCode = action & MotionEvent.ACTION_MASK;
    	sb.append("event ACTION_" ).append(names[actionCode]);
    	if(actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
    		sb.append("(pid " ).append(
    		action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
    		sb.append(")" );
    	}
    	sb.append("[" );
    	for(int i = 0; i < event.getPointerCount(); i++) {
    		sb.append("#" ).append(i);
    		sb.append("(pid " ).append(event.getPointerId(i));
    		sb.append(")=" ).append((int) event.getX(i));
    		sb.append("," ).append((int) event.getY(i));
    		if(i + 1 < event.getPointerCount())
    			 sb.append(";" );
    	}
    	sb.append("]" );
    	LOGI(sb.toString());
    
    }
}

