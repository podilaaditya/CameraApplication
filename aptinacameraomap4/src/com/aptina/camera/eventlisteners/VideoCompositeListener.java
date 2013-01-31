/**
 * 
 */
package com.aptina.camera.eventlisteners;

/**
 * @author stoyan
 *
 */
public class VideoCompositeListener extends GestureCompositeListener {
	public interface VideoGestureInterface extends GestureInterface {
		
	}
	/**
	 * 
	 */
	public VideoCompositeListener() {
		addSlideListener(SlideGestureListener.SLIDE_RIGHT);
		addSlideListener(SlideGestureListener.SLIDE_DOWN);
		addSlideListener(SlideGestureListener.SLIDE_LEFT);
		
		//Add double tap listener for the video snapshot
		addDoubleTapListener();
	}

}
