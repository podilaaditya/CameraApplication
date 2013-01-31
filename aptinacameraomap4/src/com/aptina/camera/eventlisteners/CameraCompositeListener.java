/**
 * 
 */
package com.aptina.camera.eventlisteners;


/**
 * @author stoyan
 *
 */
public class CameraCompositeListener extends GestureCompositeListener {
	
	public interface CameraGestureInterface extends GestureInterface {
//		public void 
	}

	/**
	 * 
	 */
	public CameraCompositeListener() {
		//Add double tap listener for the ROI focus
		addDoubleTapListener();
		
		//Add the zoom listener for camera zoom
		addZoomListener();
		
		//Add the smile detection listener for smile capture
		addSmileGestureListener();
	}
	


}
