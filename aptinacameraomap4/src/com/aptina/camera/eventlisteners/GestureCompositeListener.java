package com.aptina.camera.eventlisteners;

import java.util.ArrayList;
import java.util.List;

import com.aptina.camera.eventlisteners.SlideGestureListener.SlideInterface;
import com.aptina.camera.eventlisteners.ZoomGestureListener.ZoomInterface;

import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
/**
 * @author stoyan
 *
 */
public abstract class GestureCompositeListener implements OnTouchListener{
	/**
	 * String to describe function to logcat
	 */
	private static final String TAG = "GestureCompositeListener";
	
	/**
	 * List of listeners we will combine to have our gesture interface for the VideoActivity
	 */
	private List<OnTouchListener> registeredListeners = new ArrayList<OnTouchListener>();
	
	/**
	 * Toggle to turn on/off logging
	 */
	private static final boolean LOG_ON = true;
	/**
     * VideoGestureInterface selection callback target.
     */
    protected GestureInterface mCallbackTarget = null;
    public interface GestureInterface {
		/**
         * Function to return the current camera 
         */
		public Camera getCamera();

        /**
         * Set the DVS mode of the camera and animate switching
         */
        public void OnSlideGesture(int slide_dir);
        
        /**
         * Pass the onDoubleTap event
         */
        public void OnDoubleTapGesture(MotionEvent event);
        
        /**
         * Pass the OnSmileDetected callback to the camera
         */
        public void OnSmileGesture();
        /**
		 * Pass the OnFrownDetected callback to the camera
		 */
		public void OnFrownDetected();
        /**
         * Pass the zoom change from the zoom listener
         */
        public void OnZoomGesture(float zoom_scale);
    }
    public void setCallback(GestureInterface callback){
    	mCallbackTarget = callback;
    }
	/**
	 * Constructor of class which should set all of the listeners for the gesture interface
	 */
	public GestureCompositeListener(){

	}
	
	protected void addSlideListener(int slide_direction){
		SlideGestureListener.SlideInterface slideInterface = new SlideGestureListener.SlideInterface(){

			@Override
			public void onSlideEvent(int slide_dir) {
				Log.i(TAG, "Logging from slideInterface SLIDE : " + slide_dir);
				mCallbackTarget.OnSlideGesture(slide_dir);

			}
			
		};
		SlideGestureListener slideGesure = new SlideGestureListener(slide_direction);
		slideGesure.setCallback(slideInterface);
		this.registerListener(slideGesure);
	}
	
	protected void addDoubleTapListener(){
		DoubleTapGestureListener tapListener = new DoubleTapGestureListener();
		DoubleTapGestureListener.DoubleTapInterface tapInterface = new DoubleTapGestureListener.DoubleTapInterface(){

			@Override
			public void onDoubleTap(MotionEvent event) {
				Log.i(TAG, "onDoubleTap");
				mCallbackTarget.OnDoubleTapGesture(event);
				
			}
			
		};
		tapListener.setCallback(tapInterface);
		this.registerListener(tapListener);
	}
	
	protected void addSmileGestureListener(){
		SmileGestureListener smileListener = new SmileGestureListener();
		SmileGestureListener.SmileInterface smileInterface = new SmileGestureListener.SmileInterface(){

			@Override
			public void OnSmileDetected() {
				Log.i(TAG, "OnSmileDetected");
				mCallbackTarget.OnSmileGesture();
			}

			@Override
			public void OnFrownDetected() {
				Log.i(TAG, "OnFrownDetected");
				mCallbackTarget.OnFrownDetected();
				
			}
		};
		smileListener.setCallback(smileInterface);
		this.registerListener(smileListener);
	}
	
	protected void addZoomListener(){
		ZoomGestureListener mZoomGestureListener = new ZoomGestureListener();
		ZoomGestureListener.ZoomInterface mZoomInterface = new ZoomGestureListener.ZoomInterface(){

			@Override
			public Camera getCamera() {
				return mCallbackTarget.getCamera();
			}


			@Override
			public void OnZoomChange(float zoom_scale) {
				mCallbackTarget.OnZoomGesture(zoom_scale);
				
			}
			
		};

		mZoomGestureListener.setCallback(mZoomInterface);
		this.registerListener(mZoomGestureListener);
	}
	
	/**
	 * 
	 */
	/**
	 * Register listeners to be passed touch events from the view this 
	 * is set to
	 * @param listener The listener to add to our registered listeners list
	 */
	public void registerListener (OnTouchListener listener) {
	      registeredListeners.add(listener);
	}
	
	/**
	 * Testing method for getting the SlideGestureListeners
	 */
	public SlideGestureListener getSlideListener(int idx){
		return (SlideGestureListener)registeredListeners.get(idx);
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		passEvent(v,event);
		//Indicate event was consumed so that the view does not consume this event itself and block ACTION_MOVE, etc..
		return true;
	}
	
	private void passEvent(View v, MotionEvent event){
		for(OnTouchListener listener : registeredListeners) {
	         listener.onTouch(v,event);
	         
	    }
	}
	
	 /**
     * Log to logcat with a conditional toggle
     * @param msg string to be printed to logcat
     */
    private void LOGI(String msg){
    	if(LOG_ON){
    		Log.i(TAG, msg);
    	}
    }

}
