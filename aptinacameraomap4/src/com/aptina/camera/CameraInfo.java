package com.aptina.camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.aptina.logger.Logger;

import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;

/**
 * Class that queries camera for its features.
 */
public class CameraInfo {
	private static final String TAG = "CameraInfo";
    /**
     * Auto camera mode.
     */
    public static final String CAMERA_MODE_AUTO = "auto";

    /**
     * HDR camera mode.
     */
    public static final String CAMERA_MODE_HDR = "hdr";

    /**
     * Burst camera mode.
     */
    public static final String CAMERA_MODE_BURST = "burst-capture";

    /**
     * Panorama camera mode.
     */
    public static final String CAMERA_MODE_PANORAMA = "panorama-capture";
    /**
     * Key for post-view preview sizes
     */
    public static final String SUPPORTED_ZSL_PREVIEW_SIZES_LABEL = "supported-zsl-preview-sizes";

    /**
     * Key for zsl picture sizes
     */
    public static final String SUPPORTED_ZSL_PICTURE_SIZES_LABEL = "supported-zsl-picture-sizes";

    /**
     * Zero Shutter Lag camera mode.
     */
    public static final String CAMERA_MODE_ZSL = "zsl";

    /**
     * DVS video mode.
     */
    public static final String VIDEO_MODE_DVS = "dvs";
    public static final String VIDEO_MODE_DVS_OFF = "off";
    public static final String VIDEO_MODE_DVS_ON = "on";
    public static final String VIDEO_MODE_DVS_LOW = "low";
    public static final String VIDEO_MODE_DVS_HIGH = "high";
    /**
     * Default value for "enabled" mode state.
     */
    public static final String CAMERA_MODE_ENABLED_DEFAULT_VALUE = "enable";

    /**
     * Default value for "disabled" mode state.
     */
    public static final String CAMERA_MODE_DISABLED_DEFAULT_VALUE = "disable";

    /**
     * F/S Shutter parameters
     */
    public static final String SMILE_SHUTTER = "smile-shutter";
    public static final String SMILE_SHUTTER_ON = "on";
    public static final String SMILE_SHUTTER_OFF ="off";

    public static final String FACE_SHUTTER  = "face-shutter";
    public static final String FACE_SHUTTER_ON = "on";
    public static final String FACE_SHUTTER_OFF ="off";


    /**
     * A toggle to switch between full and minimal resolution support for the TI
     * Blaze development devices
     */
    public static final boolean MIN_RESOLUTIONS = true;
    public static List<Point> MIN_PICTURE_RESOLUTIONS = new ArrayList<Point>();
    public static List<Point> MIN_POSTVIEW_RESOLUTIONS = new ArrayList<Point>();
    public static List<Point> MIN_PREVIEW_RESOLUTIONS = new ArrayList<Point>();
    public static List<Point> MIN_VIDEO_RESOLUTIONS = new ArrayList<Point>();
    public static List<Point> VIDEO_RESOLUTIONS_NO_SNAP  = new ArrayList<Point>();

    /**
     * Default number of photos for burst mode.
     */
    public static final int BURST_MODE_DEFAULT_VALUE = 3; // Changed from 5 to 3

    /**
     * Number of photos for HDR.
     */
    public static final int HDR_IMAGE_COUNT_VALUE = 3;

    /**
     * Thumbnail image height.
     */
    public static final int THUMBNAIL_IMAGE_HEIGHT = 120;
	/**
	 * The amount of time we wait for the auto exposure to compensate
	 * for a new ROI focus
	 */
    public static final int EXPOSURE_WAIT_TIME = 1000;
    /**
     * Auto focus mode.
     */
    public static final String FOCUS_MODE_AUTO = "auto";

    /**
     * Infinity focus mode.
     */
    public static final String FOCUS_MODE_INFINITY = "infinity";

    /**
     * ROI focus mode.
     */
    public static final String FOCUS_MODE_ROIFOCUS = "roifocus";

    public static final ArrayList<Integer> BURST_NUMBER_MAP = new ArrayList<Integer>();

    static {

    	BURST_NUMBER_MAP.add(6);//5
    	BURST_NUMBER_MAP.add(12);//10
    	BURST_NUMBER_MAP.add(30);//30
    	BURST_NUMBER_MAP.add(54);//50
    	BURST_NUMBER_MAP.add(96);//100

	}
    /**
     * List of focus modes
     */
    public static final String FOCUS_AUTO = "auto";
    public static final String FOCUS_INFINITY = "infinity";
    public static final String FOCUS_MACRO = "macro";
    public static final String FOCUS_CONTINUOUS_PICTURE = "continuous-picture";
    public static final String FOCUS_CONTINUOUS_VIDEO = "continuous-video";
    /**
     * See if the devices is an omap development
     */
    public static final boolean sIsDeviceOMAP4Blaze = Build.DEVICE.equals("blaze")
    		|| Build.DEVICE.equals("blaze_tablet")
    		|| Build.DEVICE.equals("zoom2");

    /**
     * Returns all supported modes for the camera object.
     *
     * @param camera Camera parameters to check.
     *
     * @return Array of supported modes.
     */
    public static ArrayList<String> getAvailableCameraModes(Camera.Parameters parameters) {
        ArrayList<String> modes = new ArrayList<String>();

        try {
            int burst = parameters.getInt(CAMERA_MODE_BURST);
            if (burst >= 0) {
                modes.add(CAMERA_MODE_BURST);
            }
        } catch (Exception ex) {
            Logger.logMessage("CameraInfo.getAvailableModes(): Burst feature is not supported");
        }

        try {
            String hdr = parameters.get(CAMERA_MODE_HDR);
            if (hdr != null) {
                modes.add(CAMERA_MODE_HDR);
            }
        } catch (Exception ex) {
            Logger.logMessage("CameraInfo.getAvailableModes(): HDR feature is not supported");
        }

        try {
            String panorama = parameters.get(CAMERA_MODE_PANORAMA);
            if (panorama != null) {
                modes.add(CAMERA_MODE_PANORAMA);
            }
        } catch (Exception ex) {
            Logger.logMessage("CameraInfo.getAvailableModes(): Panorama feature is not supported");
        }

        try {
            String zsl = parameters.get(CAMERA_MODE_ZSL);
            if (zsl != null) {
                modes.add(CAMERA_MODE_ZSL);
            }
        } catch (Exception ex) {
            Logger.logMessage("CameraInfo.isZslSupprted() : ZSL feature is not supported");
        }

        return modes;
    }

    /**
     * Retrieves all available parameters for video camera.
     *
     * @param parameters camera parameters.
     *
     * @return array of supported video modes.
     */
    public static ArrayList<String> getAvailableVideoModes(Parameters parameters) {
        ArrayList<String> modes = new ArrayList<String>();

        // Checks for DVS mode.
        try {
            String dvs = parameters.get(VIDEO_MODE_DVS);
            if (dvs != null) {
                modes.add(VIDEO_MODE_DVS);
            }
        } catch (Exception ex) {
        }

        return modes;
    }

    /**
	 * A function that creates the minimal resolution settings for the application on the
	 * TI Blaze development devices
	 */
	public static void initMinResolutionSizes(){
		MIN_PICTURE_RESOLUTIONS.add(new Point(3264, 2448));
		MIN_PICTURE_RESOLUTIONS.add(new Point(2048, 1536));
		MIN_PICTURE_RESOLUTIONS.add(new Point(1280, 960));

		MIN_POSTVIEW_RESOLUTIONS.add(new Point(3264, 2448));
		MIN_POSTVIEW_RESOLUTIONS.add(new Point(2048, 1536));

		VIDEO_RESOLUTIONS_NO_SNAP.add(new Point(1280, 720));
		VIDEO_RESOLUTIONS_NO_SNAP.add(new Point(800, 480));
		VIDEO_RESOLUTIONS_NO_SNAP.add(new Point(720, 480));

		MIN_VIDEO_RESOLUTIONS.add(new Point(1280, 720));
		MIN_VIDEO_RESOLUTIONS.add(new Point(800, 480));
//		MIN_VIDEO_RESOLUTIONS.add(new Point(720, 480));
		MIN_VIDEO_RESOLUTIONS.add(new Point(640, 480));
//		MIN_VIDEO_RESOLUTIONS.add(new Point(320, 240));


//		MIN_PREVIEW_RESOLUTIONS.add(new Point(1280, 720));
//		MIN_PREVIEW_RESOLUTIONS.add(new Point(800, 480));
//		MIN_PREVIEW_RESOLUTIONS.add(new Point(720, 480));
		MIN_PREVIEW_RESOLUTIONS.add(new Point(640, 480));
//		MIN_PREVIEW_RESOLUTIONS.add(new Point(320, 240));

	}
    /**
     * Returns all supported modes for the camera object.
     *
     * @param parameters Camera parameters.
     *
     * @return Array of supported modes.
     */
    public static ArrayList<String> getAvailableModes(Parameters parameters) {
        String captureMode = parameters.get("capture");

        if ("video".equalsIgnoreCase(captureMode)) {
            return getAvailableVideoModes(parameters);
        }

        return getAvailableCameraModes(parameters);
    }

    /**
     * Returns currently active camera mode.
     *
     * @param camera Camera to check mode for.
     *
     * @return Name of the mode.
     */
    public static String getActiveMode(Camera camera) {
        return getActiveMode(camera.getParameters());
    }

    /**
     * Returns currently active camera mode.
     *
     * @param camera Camera parameters to check mode for.
     *
     * @return Name of the mode.
     */
    public static String getActiveMode(Camera.Parameters parameters) {
        String captureMode = parameters.get("capture");

        if ("video".equalsIgnoreCase(captureMode)) {
            try {
                ArrayList<String> modes = getAvailableVideoModes(parameters);

                for (String mode : modes) {
                    if (parameters.get(mode).equals(CAMERA_MODE_ENABLED_DEFAULT_VALUE)) {
                        return mode;
                    }
                }
            } catch(final Exception e) {
                Logger.logApplicationException(e, "CameraInfo.getActiveMode(): Failed during defining active mode for video.");
            }
        } else {
            try {
                ArrayList<String> modes = getAvailableCameraModes(parameters);
                for (String mode : modes) {
                    if (mode.equals(CAMERA_MODE_BURST)) {
                        if (parameters.getInt(mode) > 0) {
                            return CAMERA_MODE_BURST;
                        }
                    } else if (parameters.get(mode).equals(CAMERA_MODE_ENABLED_DEFAULT_VALUE)) {
                        return mode;
                    }
                }
            } catch(final Exception e) {
                Logger.logApplicationException(e, "CameraInfo.getActiveMode(): Failed during defining active mode for photo.");
            }
        }

        // If all modes are inactive, return null.
        return null;
    }

    /**
     * Function to check if the video resolution supports snapshot
     * If snapshot is not supported. ie high resolutions, then snapshot
     * mode needs to be disabled in parameters of camera or recording
     * at high resolutions will come out with green static
     *
     * @param videoResolution to check if snapshot is supported for
     * @return boolean if snapshot is supported
     */
    public static boolean snapshotSupportedRes(Size videoResolution){
    	Point p = new Point(videoResolution.width, videoResolution.height);

    	if(VIDEO_RESOLUTIONS_NO_SNAP.contains(p)){
    		return false;
    	}
    	return true;
    }
    /**
     * Filters out unsupported picture sizes.
     *
     * @param sizes All available picture sizes.
     *
     * @return Filtered list of picture sizes.
     */
    private static List<Size> filterPictureSizes(List<Size> sizes) {
       Log.i(TAG, "filterPictureSizes(List<Size> sizes)");

    	if(sizes == null){
    		Log.e(TAG, "No supported sizes");
    		return null;
    	}

    	if(MIN_RESOLUTIONS && sIsDeviceOMAP4Blaze){
    		List<Size> supportedPictureSizes = new ArrayList<Size>();
            for (Camera.Size size : sizes) {
            	for(Point p : MIN_PICTURE_RESOLUTIONS){
            		if (p.x == size.width && p.y == size.height) {
            			supportedPictureSizes.add(size);
            			break;
                    }
        		}
            }
            return supportedPictureSizes;
         }else{
        	 return sizes;
         }
    }

    private static List<Size> filterPreviewSizes(List<Size> sizes, boolean forVideo){
    	Log.i(TAG, "filterPreviewSizes(List<Size> sizes)");

    	if(sizes == null){
    		Log.e(TAG, "No supported sizes");
    		return null;
    	}
    	if(MIN_RESOLUTIONS && sIsDeviceOMAP4Blaze){
    		List<Size> supportedPreviewSizes = new ArrayList<Size>();
            for (Camera.Size size : sizes) {
            	if(forVideo){
            		for(Point p : MIN_VIDEO_RESOLUTIONS){
                		if (p.x == size.width && p.y == size.height) {
                			supportedPreviewSizes.add(size);
                			break;
                        }
            		}
            	}else{
            		for(Point p : MIN_PREVIEW_RESOLUTIONS){
                		if (p.x == size.width && p.y == size.height) {
                			supportedPreviewSizes.add(size);
                			break;
                        }
            		}
//            		supportedPreviewSizes.add(size);
            	}

            }
            return supportedPreviewSizes;
         }else{
        	 return sizes;
         }
    }

    public static Camera.Size[] getZSLPreviewSizes(Camera.Parameters params, Camera cam, String activeMode, boolean forVideo){
    	Log.e(TAG, "getZSLPreviewSizes activeMode : " + activeMode);
    	 Camera.Size[] previewSizes = null;
    	 try {
             List<Size> supportedPreviewSizes;
             if(activeMode == CameraInfo.CAMERA_MODE_AUTO){
            	 Log.w(TAG, "activeMode == CameraInfo.CAMERA_MODE_AUTO");
            	 supportedPreviewSizes = filterPreviewSizes(params.getSupportedPreviewSizes(), forVideo);
             }else{//TODO: Assumption is all other modes are post-view
            	 supportedPreviewSizes = getZSLSizes(params.get(CameraInfo.SUPPORTED_ZSL_PREVIEW_SIZES_LABEL), cam);
             }

             if(supportedPreviewSizes == null){
            	 return null;
             }

             previewSizes = new Camera.Size[supportedPreviewSizes.size()];
             supportedPreviewSizes.toArray(previewSizes);
             Arrays.sort(previewSizes, new Comparator<Camera.Size>() {

                 @Override
                 public int compare(Size arg0, Size arg1) {
                     if ((arg0.width*arg0.height) < (arg1.width*arg1.height)) {
                         return 1;
                     } else if ((arg0.width*arg0.height) > (arg1.width*arg1.height)) {
                         return -1;
                     } else {
                         return 0;
                     }
                 }

             });
         } catch(final Exception e) {
             Logger.logApplicationException(e, "CameraInfo.sortPictureSizes(): Failed.");
         }
    	 return previewSizes;
    }

    /**
     * Sorts available picture sizes in descending order.
     *
     * @param camera Camera to get picture sizes from.
     *
     * @return Sorted array of available picture {@link Size}s.
     */
    public static Camera.Size[] sortPictureSizes(Camera.Parameters params, Camera cam, String activeMode) {
    	 Camera.Size[] pictureSizes = null;

         try {
             List<Size> supportedPictureSizes;
             if(activeMode == CameraInfo.CAMERA_MODE_AUTO){
             	supportedPictureSizes = filterPictureSizes(params.getSupportedPictureSizes());
             }else{//TODO: Assumption is all other modes are post-view
             	supportedPictureSizes = filterPictureSizes(getZSLSizes(params.get(CameraInfo.SUPPORTED_ZSL_PICTURE_SIZES_LABEL), cam));
             }

             if(supportedPictureSizes == null){
            	 return null;
             }

             pictureSizes = new Camera.Size[supportedPictureSizes.size()];
             supportedPictureSizes.toArray(pictureSizes);
             Arrays.sort(pictureSizes, new Comparator<Camera.Size>() {

                 @Override
                 public int compare(Size arg0, Size arg1) {
                     if ((arg0.width*arg0.height) < (arg1.width*arg1.height)) {
                         return 1;
                     } else if ((arg0.width*arg0.height) > (arg1.width*arg1.height)) {
                         return -1;
                     } else {
                         return 0;
                     }
                 }

             });
         } catch(final Exception e) {
             Logger.logApplicationException(e, "CameraInfo.sortPictureSizes(): Failed.");
         }


        return pictureSizes;
    }

    public static List<Size> getZSLSizes(String str, Camera cam){
    	Log.i(TAG, "getZSLSizes(String str, Camera cam)");
    	Log.i(TAG, str);
    	String [] pic_ar = str.split(",");
    	List<Camera.Size> pic_size_list = new ArrayList<Camera.Size>();
    	if(pic_ar.length == 0){
    		Log.e(TAG, "pic_ar size is 0");
    		return null;
    	}
    	for(String pic : pic_ar){
    		int width = Integer.parseInt(pic.split("x")[0]);
    		int height = Integer.parseInt(pic.split("x")[1]);
    		Camera.Size size = cam.new Size(width, height);
    		pic_size_list.add(size);
    	}
    	if(pic_size_list.isEmpty()){
    		Log.e(TAG, "pic_size_list == null");
    		return null;
    	}
    	return pic_size_list;
    }

	public static Size[] getVideoSizes(Camera.Parameters params) {
		Camera.Size[] videoSizes = null;
		List<Size> supportedSizes = params.getSupportedVideoSizes();
		 if(videoSizes == null){
	        	Log.w(TAG, "getSupportedVideoSizes() returned null, using preview sizes to manage video recording");
	        	/**void getSupportedVideoSizes(Vector<Size> &sizes) const;*/
	            // Retrieve a Vector of supported dimensions (width and height)
	            // in pixels for video frames. If sizes returned from the method
	            // is empty, the camera does not support calls to setVideoSize()
	            // or getVideoSize(). In adddition, it also indicates that
	            // the camera only has a single output, and does not have
	            // separate output for video frames and preview frame.

	        	supportedSizes = params.getSupportedPreviewSizes();
	        }
		for(Size s : supportedSizes){
			Log.i(TAG, "Supported Video Size : " + s.width + "x" + s.height);
		}
		List<Size> filleteredSizes = filterPreviewSizes(supportedSizes, true);
		videoSizes = new Camera.Size[filleteredSizes.size()];
		filleteredSizes.toArray(videoSizes);
        Arrays.sort(videoSizes, new Comparator<Camera.Size>() {

            @Override
            public int compare(Size arg0, Size arg1) {
                if ((arg0.width*arg0.height) < (arg1.width*arg1.height)) {
                    return 1;
                } else if ((arg0.width*arg0.height) > (arg1.width*arg1.height)) {
                    return -1;
                } else {
                    return 0;
                }
            }

        });
		return videoSizes;
	}

	/**
	 * Calculates the dip from pixels
	 */
	public static float DISPLAY_METRICS_DENSITY = -1;
	public static int getDip(int pixel){
		if(DISPLAY_METRICS_DENSITY > 0){
			return (int) (pixel / DISPLAY_METRICS_DENSITY);
		}
	    return -1;
	}

	/**
	 * Calculates the pixels from dip
	 */
	public static int getPix(int dip){
		if(DISPLAY_METRICS_DENSITY > 0){
			return (int) (dip * DISPLAY_METRICS_DENSITY);
		}
	    return -1;
	}




}