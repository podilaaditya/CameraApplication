package com.aptina.camera.eventlisteners;

import java.util.ArrayList;

import com.aptina.R;
import com.aptina.camera.CameraInfo;
import com.aptina.camera.components.ModeButton;
import com.aptina.camera.components.ModeSelector;
import com.aptina.camera.components.ModeSelector.OnModeSelectionListener;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Mode Selection listener. 
 */
public class ModeSelectionListener implements OnClickListener, OnModeSelectionListener  {
    /**
     * Tag for debuging
     */
	private static final String TAG = "ModeSelectionListener";
	
    /**
     *  Callback interface.
     */
    public interface Callback {
        /**
         * Invokes when mode is selected.
         * 
         * @param mode Selected mode.
         */
        public void onModeSelected(String mode);
    }
    
    /**
     * Application context object.
     */
    private Context mContext = null;

    /**
     * Mode selector control.
     */
    private ModeSelector mModeSelector = null;

    /**
     * Assigned camera.
     */
    private Camera mCamera = null;

    /**
     * Mode button image holder.
     */
    private ImageView mModeHolder = null;
    
    /**
     * Mode text.
     */
    private TextView mModeText;

    /**
     * Last selected mode button.
     */
    private ModeButton mSelectedButton = null;

    /**
     * Buttons list.
     */
    private ArrayList<View> mButtons = null;

    /**
     * Array of modes supported by camera.
     */
    private ArrayList<String> mSupportedModes = null;
    
    /**
     * Currently selected mode.
     */
    private String mActiveMode = null;
    
    /**
     * Mode selection callback target.
     */
    private Callback mCallbackTarget = null;
    
    /**
     * Sets mode selection callback target.
     * 
     * @param callback IModeSelectionCallback responder.
     */
    public void setCallbackTarget(Callback callback) {
        mCallbackTarget = callback;
    }
    
    /**
     * Returns current assigned callback target.
     * 
     * @return Currently assigned callback target.
     */
    public Callback getCallbackTarget() {
        return mCallbackTarget;
    }
    
    /**
     * Updates camera property.
     * 
     * @param camera New {@link Camera} object.
     */
    public void setCamera(Camera camera) {
        this.mCamera = camera;
        
        mSupportedModes = CameraInfo.getAvailableModes(mCamera.getParameters());
        mButtons = makeButtonsList();
        
        // Update panel button.
        mModeHolder.setImageResource(mSelectedButton.getSelectedImageResource());
        mModeText.setText(mSelectedButton.getCaption());
    }

    /**
     * Returns assigned camera object.
     * 
     * @return Assigned {@link Camera} object.
     */
    public Camera getCamera() {
        return this.mCamera;
    }

    /**
     * Creates new instance of the class.
     * 
     * @param context Application context.
     * @param camera Camera object.
     * @param selector Mode selector control.
     */
    public ModeSelectionListener(Context context, Camera camera, ModeSelector selector, View modeSelectionButton) {
        this.mContext = context;
        this.mCamera = camera;		
        this.mModeSelector = selector;        
        this.mModeHolder = (ImageView)modeSelectionButton.findViewById(R.id.mode_image_holder);
        this.mModeText = (TextView) modeSelectionButton.findViewById(R.id.mode_label);
    }

    @Override
    public void onModeSelected(View mode, int modeIndex) {
    	Log.i(TAG,"onModeSelected");
        // Update selector.
        mSelectedButton.setSelected(false);

        mSelectedButton = (ModeButton)(mButtons.get(modeIndex));
        mSelectedButton.setSelected(true);

        // Update panel button.
        mModeHolder.setImageResource(mSelectedButton.getSelectedImageResource());
        mModeText.setText(mSelectedButton.getCaption());
        mModeSelector.setVisibility(View.GONE);
        
        // Update active mode property and call the callback target.
        if (mSupportedModes.size() > 0 && modeIndex > 0) {
            // Takes modeIndex - 1, because auto mode was added by default.
            mActiveMode = mSupportedModes.get(modeIndex - 1);
        } else {
            mActiveMode = null;
        }
        if (mCallbackTarget != null) {
            mCallbackTarget.onModeSelected(mActiveMode);
        }
    }

    @Override
    public void onClick(View modeButton) {     
    	Log.i(TAG,"onClick");
        if (mCamera == null) {
            return;
        }

        if (mModeSelector.getVisibility() != View.VISIBLE) {

            if (mButtons == null) {
                mButtons = makeButtonsList();
            }

            // Show selector.
            mModeSelector.setButtons(mButtons);
            mModeSelector.scrollToButton(mSelectedButton);
            mModeSelector.setVisibility(View.VISIBLE);
            mModeSelector.setModeSelectionListener(this);            
        } else {
            mModeSelector.setVisibility(View.GONE);
        }
    }

    /**
     * Creates list of buttons corresponding to available modes.
     * 
     * @return List of buttons corresponding to available modes.
     */
    private ArrayList<View> makeButtonsList() {
        Parameters parameters = mCamera.getParameters();
        mSupportedModes = CameraInfo.getAvailableModes(parameters);
        mActiveMode = CameraInfo.getActiveMode(parameters);
        
        ArrayList<View> buttons = new ArrayList<View>();

        // Add auto button by default.
        ModeButton autoButton = new ModeButton(mContext);       
        autoButton.setCaption(R.string.mode_auto);
        autoButton.setSelectedImage(R.drawable.icon_active_auto);
        autoButton.setDeselectedImage(R.drawable.icon_inactive_auto);
        buttons.add(autoButton);
        
        if (mActiveMode == null) {
            mSelectedButton = autoButton;
            autoButton.setSelected(true);
        } else {
            autoButton.setSelected(false);
        }
        
        for (String mode : mSupportedModes) {
            ModeButton button = new ModeButton(mContext);

            if (mode.equals(CameraInfo.CAMERA_MODE_BURST)) {
                button.setCaption(R.string.mode_burst);
                button.setSelectedImage(R.drawable.icon_active_burst);
                button.setDeselectedImage(R.drawable.icon_inactive_burst);                
            } else if (mode.equals(CameraInfo.CAMERA_MODE_HDR)) {
                button.setCaption(R.string.mode_hdr);
                button.setSelectedImage(R.drawable.icon_active_hdr);
                button.setDeselectedImage(R.drawable.icon_inactive_hdr);
            } else if (mode.equals(CameraInfo.CAMERA_MODE_PANORAMA)) {
                button.setCaption(R.string.mode_panorama);
                button.setSelectedImage(R.drawable.icon_active_panorama);
                button.setDeselectedImage(R.drawable.icon_inactive_panorama);
            } else if (mode.equals(CameraInfo.CAMERA_MODE_ZSL)) {
                button.setCaption(R.string.mode_zsl);
                button.setSelectedImage(R.drawable.icon_active_zsl);
                button.setDeselectedImage(R.drawable.icon_inactive_zsl);
            } else if (mode.equals(CameraInfo.VIDEO_MODE_DVS)) {
                button.setCaption(R.string.mode_dvs);
                button.setSelectedImage(R.drawable.icon_active_dvs);
                button.setDeselectedImage(R.drawable.icon_inactive_dvs);
            }
            
            buttons.add(button);
            
            if (mode == mActiveMode) {
                button.setSelected(true);
                mSelectedButton = button;
            } else {
                button.setSelected(false);
            }
        }
        
        return buttons;
    }
}
