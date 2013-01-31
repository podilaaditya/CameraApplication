package com.aptina.camera.eventlisteners;


import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * @author stoyan
 *
 */
public class SlideGestureListener implements OnTouchListener{
	/**
	 * String to describe function to logcat
	 */
	private static final String TAG = "SlideGestureListener";
	/**
	 * Toggle to turn on/off logging
	 */
	private static final boolean LOG_ON = false;

	/**
	 * Slide directions, in clockwise succession
	 * Defines the type of slide gesture listener each object
	 * will be
	 */
	public static final int SLIDE_DEFAULT = -1;
	public static final int SLIDE_UP = 0;
	public static final int SLIDE_RIGHT = 1;
	public static final int SLIDE_DOWN = 2;
	public static final int SLIDE_LEFT = 3;
	
	/**
	 * Define the slide mode in one of the below:
	 * SLIDE_UP
	 * SLIDE_RIGHT
	 * SLIDE_DOWN
	 * SLIDE_LEFT
	 * Each listener should only trigger when it detects its' own 
	 * slide event
	 */
	private int SLIDE_MODE = SLIDE_DEFAULT;

	/**
     * SlideInterface selection callback target.
     */
    private SlideInterface mCallbackTarget = null;
	public interface SlideInterface {
		/**
		 * Send slide event to gesture composite listener
		 * in order to pass action to VideoActivity
		 */
		public void onSlideEvent(int slide_dir);
	}
    public void setCallback(SlideInterface callback){
    	mCallbackTarget= callback;
    }
	/**
	 * Constructor class
	 * @param slide_type = the SLIDE_MODE of this instance
	 * of SlideGestureListener
	 */
	public SlideGestureListener(int slide_type){
		SLIDE_MODE = slide_type;
	}

   
	/**
	 * The distance from the initial ACTION_DOWN that we will not
	 * trigger a slide event in order to prevent tiny gestures from 
	 * triggering the wrong slide
	 */
	private float mDeadRadius = 40f;
	
	/**
	 * The absolute distance from the ACTION_DOWN event the
	 * finger is
	 */
	private float mCurrentRadius = -1f;

	int initX = -1;
	int initY = -1;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
//		LOGI("slide : " + SLIDE_MODE);
//		dumpEvent(event);
		// Handle touch events here...
	    switch (event.getAction() & MotionEvent.ACTION_MASK) {
	    case MotionEvent.ACTION_DOWN:
	    	initX = (int)event.getX();
	    	initY = (int)event.getY();
	    	LOGI("down : " + initX + "," + initY);
	    	
	        break;
	    case MotionEvent.ACTION_UP:
	    	LOGI("down : " + initX + "," + initY);
	    	LOGI("up : " + event.getX() + "," + event.getY());
	    	mCurrentRadius = spacingFromDownEvent(event);
	    	if(mDeadRadius < mCurrentRadius){
	    		checkSlide(event);
	    	}
	    	break;
	    case MotionEvent.ACTION_MOVE:	    	
	        break;
	    }//End switch
	    //Indicate event was not handled
		return true;

	}
	
	private void checkSlide(MotionEvent event){
		if(correctDirection(event)){
			mCallbackTarget.onSlideEvent(SLIDE_MODE);
		}
	}
	/** Determine the space between two MotionEvents */
    private float spacingFromDownEvent(MotionEvent event) {
       // ...
       float x = event.getX() - initX;
       float y = event.getY() - initY;
       float ans = FloatMath.sqrt(x*x + y*y);
//       LOGI( " spacing = " + ans);
       return ans;
    }
    /**
     * The angle in radians of the motion event
     */
    private double mTheta = -1;
    public double getEventTheta(){
    	return mTheta;
    }
    /**
     * Determine if the movement of the finger is in the
     * correct direction for this instance of the SlideGestureListener
     */
    private boolean correctDirection(MotionEvent event){
    	float Xe = event.getX();
    	float Ye = event.getY();
    	float xDif = Xe - initX; 
    	float yDif = Ye - initY; 

    	mTheta = vectorAngle(xDif, yDif);
    	LOGI("theta  = " + mTheta);
    	return angleToDirection(mTheta) == SLIDE_MODE;
    }
    
    /**
     * Get the angle of the coordinates, in radians
     */
    private double vectorAngle(float dx, float dy){
    	double angle = Math.atan2(dy,dx);
    	//Offset angle if the coordinates are in the bottom two quadrants, [PI->2PI]
    	angle = angle < 0 ? 2*Math.PI + angle : angle;
    	return angle;

    }
    /**
     * Function that returns the direction of the angle.
     * Note that y-axes is inverted on the screen to point down
     * instead of up
     * @param angle The angle between the Action_Down and Action_up of the gesture
     * @return The direction of gesture
     */
    private int angleToDirection(double angle){
    	//To the right
    	if((angle >= (11*Math.PI/6) && angle <= 2*Math.PI) ||
    			(angle >= 0 && angle <= Math.PI/6)){
    		return SLIDE_RIGHT;
    	}else if(angle >= Math.PI/3 && angle <= 2*Math.PI/3){//Down
    		return SLIDE_DOWN;
    	}else if(angle >= 5*Math.PI/6 && angle <= 7*Math.PI/6){//Left
    		return SLIDE_LEFT;
    	}else if(angle >= 4*Math.PI/3 && angle <= 5*Math.PI/3){//Up
    		return SLIDE_UP;
    	}
    	
    	return SLIDE_DEFAULT;
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
    	Log.d(TAG, sb.toString());
    
    }
}
