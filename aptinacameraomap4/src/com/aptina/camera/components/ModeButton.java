package com.aptina.camera.components;

import com.aptina.R;
import com.aptina.logger.Logger;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Custom component for displaying a mode selection button.
 */
public class ModeButton extends RelativeLayout {

    /**
     * Selection state.
     */
    private boolean mSelected = false;

    /**
     * Text color for deselected state.
     */
    private int mDeselectedColor;

    /**
     * Text color for selected state.
     */
    private int mSelectedColor;

    /**
     * Selected image resource.
     */
    private int mSelectedImage = -1;

    /**
     * Deselected image resource.
     */
    private int mDeselectedImage = -1;

    /**
     * Image holder.
     */
    private ImageView mImageHolder = null;

    /**
     * Mode caption holder.
     */
    private TextView mCaptionHolder = null;

    /**
     * Constructor for creating from XML with assigned style.
     *  
     * @param context Application context.
     * @param attrs Attributes set.
     * @param defStyle Default style.
     */
    public ModeButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);		
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.mode_selector, this, true);		

        init();
        readAttributes(attrs);
    }

    /**
     * Constructor for creating from XML.
     * 
     * @param context Application context.
     * @param attrs Attributes set.
     */
    public ModeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.mode_selector, this, true);

        init();
        readAttributes(attrs);
    }

    /**
     * Constructor for creating from code.
     * 
     * @param context Application context.
     */
    public ModeButton(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.mode_selector, this, true);

        init();

        // Init default values.
        mSelectedColor = getContext().getResources().getColor(R.color.resolution_button_text_color);
        mDeselectedColor = getContext().getResources().getColor(R.color.default_resolution_text_color);

        setSelected(false);
    }

    /**
     * Reads attributes array.
     * 
     * @param attrs Attributes set.
     */
    private void readAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.CustomModeView);

        // Get colors.
        mSelectedColor = a.getColor(R.styleable.CustomModeView_selectedColor, getContext().getResources().getColor(R.color.default_resolution_text_color));
        mDeselectedColor = a.getColor(R.styleable.CustomModeView_deselectedColor, getContext().getResources().getColor(R.color.default_resolution_text_color));

        // Get image.
        mSelectedImage = a.getColor(R.styleable.CustomModeView_selectedImage, -1);
        mDeselectedImage = a.getColor(R.styleable.CustomModeView_deselectedImage, -1);

        // Get state.
        setSelected(a.getBoolean(R.styleable.CustomModeView_selected, false)); 
    }

    /**
     * Initializes local variables.
     */
    private void init() {
        mImageHolder = (ImageView)findViewById(R.id.mode_image_holder);
        mCaptionHolder = (TextView)findViewById(R.id.mode_caption_holder);
    }

    /**
     * Sets button mode.
     * 
     * @param selected Selection mode value.
     */
    public void setSelected(boolean selected) {
        mSelected = selected;
        if (mSelected) {
            if (mSelectedImage > 0) {
                mImageHolder.setImageResource(mSelectedImage);
            }
            mCaptionHolder.setTextColor(mSelectedColor);
        } else {
            if (mDeselectedImage > 0) {
                mImageHolder.setImageResource(mDeselectedImage);
            }
            mCaptionHolder.setTextColor(mDeselectedColor);
        }
    }

    /**
     * Returns resource id for selected state image.
     * 
     * @return Resource id for selected state image.
     */
    public int getSelectedImageResource() {
        return mSelectedImage;
    }

    /**
     * Sets resource id for selected state image.
     * 
     * @param resourceId Resource id for selected state image.
     */
    public void setSelectedImage(int resourceId) {
        try {
            mSelectedImage = resourceId;
        } catch (Resources.NotFoundException ex) {
            Logger.logApplicationException(ex, "ModeButton.setSelectedImage() failed to find resource.");
        }
    }

    /**
     * Returns resource id for selected state image.
     * 
     * @param Resource id for selected state image.
     */
    public void setDeselectedImage(int resourceId) {
        try {
            mDeselectedImage = resourceId;
        } catch (Resources.NotFoundException ex) {
            Logger.logApplicationException(ex, "ModeButton.setDeselectedImage() failed to find resource.");
        }
    }

    /**
     * Returns resource id for deselected state image.
     * 
     * @return Resource id for deselected state image.
     */

    public int getDeselectedImageResource() {
        return mDeselectedImage;
    }

    /**
     * Sets caption color for selected state.
     * 
     * @param color Color value.
     */
    public void setSelectedColor(int color) {
        mSelectedColor = color;
    }

    /**
     * Sets caption color for deselected state.
     * 
     * @param color Color value.
     */
    public void setDeselectedColor(int color) {
        mDeselectedColor = color;
    }

    /**
     * Returns caption text.
     * 
     * @return Caption text string.
     */
    public String getCaption() {
        return (String)mCaptionHolder.getText();
    }

    /**
     * Sets caption text.
     * 
     * @param caption Text string to display.
     */
    public void setCaption(String caption) {
        mCaptionHolder.setText(caption);
    }

    /**
     * Sets caption text.
     * 
     * @param resourceId String resource ID.
     */
    public void setCaption(int resourceId) {
        try {
            mCaptionHolder.setText(resourceId);
        } catch (Resources.NotFoundException ex) {
            Logger.logApplicationException(ex, "ModeButton.setCaption() failed to find resource.");
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        super.setOnClickListener(listener);
        mImageHolder.setOnClickListener(listener);
        mCaptionHolder.setOnClickListener(listener);
    }

}
