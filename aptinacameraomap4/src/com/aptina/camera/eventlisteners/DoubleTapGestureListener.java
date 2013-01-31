package com.aptina.camera.eventlisteners;

import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
/**
 * @author stoyan
 *
 */
public class DoubleTapGestureListener implements OnTouchListener{
	/**
	 * String to describe function to logcat
	 */
	private static final String TAG = "DoubleTapGestureListener";
	/**
	 * Toggle to turn on/off logging
	 */
	private static final boolean LOG_ON = false;
	/**
	 * Static values for speed in ms of tapping to trigger double tap
	 */
	public static final int DOUBLE_TAP_SLOW = 600;
	public static final int DOUBLE_TAP_DEFAUT = 400;
	public static final int DOUBLE_TAP_FAST = 300;
	
	/**
	 * Time double tap gesture needs to complete in to trigger
	 * event
	 */
	private int mDoubleTapTimeLimit = DOUBLE_TAP_DEFAUT;
	/**
     * SlideInterface selection callback target.
     */
    private DoubleTapInterface mCallbackTarget = null;
	/**
	 * Interface for the listener
	 */
	public interface DoubleTapInterface{
		/**
		 * Pass the double tap event
		 */
		public void onDoubleTap(MotionEvent event);
		
	}
    public void setCallback(DoubleTapInterface callback){
    	mCallbackTarget= callback;
    }
	/**
	 * Constructor of listener, sets the speed the user has to 
	 * double tap at in order to trigger event
	 * @param tap_speed Speed in ms of double tap event
	 */
	public DoubleTapGestureListener(int tap_speed) {
		mDoubleTapTimeLimit = tap_speed;
	}
	public DoubleTapGestureListener() {
	}
	/**
	 * Time of first tap
	 */
	private long mFirstTap = 0;
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
	    case MotionEvent.ACTION_DOWN:
	    	if((SystemClock.uptimeMillis() - mFirstTap) < mDoubleTapTimeLimit){
	    		mCallbackTarget.onDoubleTap(event);
	    	}else{
	    		mFirstTap = SystemClock.uptimeMillis();
	    	}
	        break;
	    case MotionEvent.ACTION_UP:
	    	break;
	    case MotionEvent.ACTION_MOVE:	    	
	        break;
	    }//End switch
	    //Indicate event was not handled
		return true;
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
