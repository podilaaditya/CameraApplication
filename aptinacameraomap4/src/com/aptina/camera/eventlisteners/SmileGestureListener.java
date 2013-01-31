package com.aptina.camera.eventlisteners;

import org.apache.commons.math3.analysis.UnivariateFunction;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.aptina.miscellaneous.GestureCurve;
/**
 * @author stoyan
 *
 */
public class SmileGestureListener  implements OnTouchListener{
	/**
	 * String to describe function to logcat
	 */
	private static final String TAG = "SmileGestureListener";
	/**
	 * Toggle to turn on/off logging
	 */
	private static final boolean LOG_ON = false;
	

	/**
	 * The curve that the persons finger traces on the screen. Used to
	 * calculate the polynomial function that best fits the gesture
	 */
	private GestureCurve mSmileGesture;
	
	/**
     * SlideInterface selection callback target.
     */
    private SmileInterface mCallbackTarget = null;
	/**
	 * Interface for the listener
	 */
	public interface SmileInterface{
		/**
		 * Pass the smile detected event
		 */
		public void OnSmileDetected();
		
		/**
		 * Pass the frown detected event
		 */
		public void OnFrownDetected();
		
	}
    public void setCallback(SmileInterface callback){
    	mCallbackTarget= callback;
    }
	public SmileGestureListener() {
	}
	
	/**
	 * The minimum number of observation points that we require in order
	 * to check the gesture. 
	 */
	private static final int MIN_OBSERVATION_POINTS = 10;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
	    case MotionEvent.ACTION_DOWN:
	    	mSmileGesture = new GestureCurve();
	        break;
	    case MotionEvent.ACTION_UP:
	    	//Make sure we have enough points
	    	if(mSmileGesture != null && mSmileGesture.getRawCurve().getObservations().length > MIN_OBSERVATION_POINTS){
	    		checkGesture(mSmileGesture);
	    	}
	    	mSmileGesture = null;
	    	break;
	    case MotionEvent.ACTION_MOVE:	
	    	if(mSmileGesture != null)
	    		mSmileGesture.addCurvePoint(event.getX(), event.getY());
	        break;
	    case MotionEvent.ACTION_POINTER_DOWN://clear gesture if more than one finger
	    	mSmileGesture = null;
	    	break;
	    }//End switch
	    //Indicate event was not handled
		return true;
	}
	UnivariateFunction mParabFunct = null;
	private void checkGesture(GestureCurve curve){
		
		
		String gesture = curve.analyze();
		if(gesture.equalsIgnoreCase(GestureCurve.SMILE_GESTURE)){
			mCallbackTarget.OnSmileDetected();
		}else if(gesture.equalsIgnoreCase(GestureCurve.FROWN_GESTURE)){
			mCallbackTarget.OnFrownDetected();
		}
		
	}
	
}


