package com.aptina.camera.eventlisteners;

import java.util.ArrayList;

import com.aptina.R;
import com.aptina.camera.components.ModeSelector;
import com.aptina.camera.components.ModeSelector.OnModeSelectionListener;
import com.aptina.camera.components.SizeHolder;
import com.aptina.logger.Logger;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Resolution button handler.
 */
public class ResolutionChangeListener implements OnClickListener, OnModeSelectionListener {
	private static final String TAG = "ResolutionChangeListener";
    /**
     *  Callback interface.
     */
    public interface Callback {
        /**
         * Invokes when resolution is selected.
         * 
         * @param resolution Selected resolution.
         * @param index Index of selected element.
         */
        public void onResolutionSelected(Size resolution, int index);

        /**
         * Retrieves current resolution.
         * 
         * @return current resolution.
         */
        public Size getCurrentResolution();
    }

    /**
     * Button's size holder.
     */
    private SizeHolder mResolutionTextHolder = null;

    /**
     * Application context object.
     */
    private Context mContext = null;

    /**
     * Mode selector control.
     */
    private ModeSelector mModeSelector = null;

    /**
     * Available picture sizes.
     */
    private Size[] mSizes;

    /**
     * A boolean to separate the snapshot and video resolutions
     */
    private boolean isVideoResolution;
    
    /**
     * Selected resolution view.
     */
    private int mSelectedHolderIndex = 0;

    /**
     * Mode selection callback target.
     */
    private Callback mCallbackTarget = null;

    /**
     * Creates new instance of the class.
     * 
     * @param context Application context.
     * @param sizes Array of supported resolution sizes.
     * @param selector Mode selector control.
     */
    public ResolutionChangeListener(Context context, Size[] sizes, ModeSelector selector) {
        if ((context == null) || (selector == null)) {
            throw new IllegalArgumentException();
        }

        this.mContext = context;
        this.mSizes = sizes;
        this.mModeSelector = selector;
    }
    public ResolutionChangeListener(Context context, Size[] sizes, ModeSelector selector, boolean videoResolution) {
        if ((context == null) || (selector == null)) {
            throw new IllegalArgumentException();
        }
        
        
        this.mContext = context;
        this.mSizes = sizes;
        this.isVideoResolution = videoResolution;
        this.mModeSelector = selector;
    }

    /**
     * Sets array of supported resolution sizes.
     * 
     * @param sizes Array of supported resolution sizes.
     */
    public void setSizes(Size[] sizes) {
        mSizes = sizes;
    }

    /**
     * Sets resolution selection callback target.
     * 
     * @param callback responder.
     */
    public void setCallbackTarget(Callback callback) {
        mCallbackTarget = callback;
    }

    @Override
    public void onClick(View resolutionButton) { 
        if (mResolutionTextHolder == null) {
        	if(isVideoResolution){
        		mResolutionTextHolder = (SizeHolder)resolutionButton.findViewById(R.id.video_resolution_value);
        	}else{
        		mResolutionTextHolder = (SizeHolder)resolutionButton.findViewById(R.id.snapshot_resolution_value);
        	}
            
        }

        if (mModeSelector.getVisibility() != View.VISIBLE) {
            ArrayList<View> buttonList = new ArrayList<View>();

            buttonList = getButtons();           

            // Initialize mode selector.
            mModeSelector.setButtons(buttonList);
            mModeSelector.scrollToButton(mSelectedHolderIndex);
            mModeSelector.setVisibility(View.VISIBLE);
            mModeSelector.setModeSelectionListener(this);            
        } else {
            mModeSelector.setVisibility(View.GONE);
        }
    }

    @Override
    public void onModeSelected(View view, int modeIndex) {
        Camera.Size selectedSize = mSizes[modeIndex];
        mSelectedHolderIndex = modeIndex;

        // Set new picture size.
        try {
            if (mCallbackTarget != null) {
                mCallbackTarget.onResolutionSelected(selectedSize, modeIndex);
            }

            if (selectedSize != null) {
                // Show information box with the selected picture size.
                Toast.makeText(mContext, selectedSize.width + "X" + selectedSize.height, Toast.LENGTH_SHORT).show();

                // Update UI.
                mResolutionTextHolder.assignSize(selectedSize);
            }            					
        } catch (RuntimeException ex) {
            Logger.logApplicationException(ex, "ResolutionChangeListener.onModeSelected(): failed to set new picture size.");
        }

        mModeSelector.setVisibility(View.GONE);		
    }

    /**
     * Retrieves list of available resolutions of photo as list of buttons to display it.
     * 
     * @return list of buttons (view) with supported resolutions for photo mode.
     */
    private ArrayList<View> getButtons() {
        ArrayList<View> buttonList = new ArrayList<View>();
        // Current size.
        Camera.Size currentSize = mCallbackTarget != null ? mCallbackTarget.getCurrentResolution() : null; 

        // Create selector buttons.
        int sizeCount = mSizes.length;
        for (int i = 0; i < sizeCount; i++) {
            Camera.Size size = mSizes[i];

            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View resButton = inflater.inflate(R.layout.resolution_selector, null);
            SizeHolder resHolder = (SizeHolder)resButton.findViewById(R.id.resolution_selector_holder);
            resHolder.assignSize(size);

            if (currentSize != null && size.width == currentSize.width && size.height == currentSize.height) {
                resHolder.setSelected(true);
                mSelectedHolderIndex = i;
            }

            buttonList.add(resButton);
        }

        return buttonList;
    }
}
