package com.aptina.camera.components;

import com.aptina.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view that draws Size object.
 */
public class SizeHolder extends View {
    
    /**
     * Additional spacing between lines that we should remove.
     */
    private static final int SECOND_LINE_VERTICAL_GAP = 2;

    /**
     * Assigned size object.
     */
    private Size mSize = null;

    /**
     * First line font size.
     */
    private float mFontSizeBig;

    /**
     * Second line font size;
     */
    private float mFontSizeSmall;

    /**
     * Text color for deselected state.
     */
    private int mDeselectedColor;

    /**
     * Text color for selected state.
     */
    private int mSelectedColor;

    /**
     * Selection state.
     */
    private boolean mSelected = false;

    /**
     * Paint object for text.
     */
    private Paint mTextPaint = null;

    /**
     * Constructor for creating from XML with assigned style. 
     * 
     * @param context Application context.
     * @param attrs Attributes set.
     * @param defStyle Style resource ID.
     */
    public SizeHolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        readAttributes(attrs);
        init();
    }

    /**
     * Constructor for creating from XML.
     * 
     * @param context Application context.
     * @param attrs Attributes set.
     */
    public SizeHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttributes(attrs);
        init();
    }

    /**
     * Constructor for creating from code.
     * 
     * @param context Application context.
     */
    public SizeHolder(Context context) {
        super(context);
        init();

        // Init default values
        mSelectedColor = getContext().getResources().getColor(R.color.default_resolution_text_color);
        mDeselectedColor = getContext().getResources().getColor(R.color.default_resolution_text_color);
        mFontSizeBig = getContext().getResources().getDimension(R.dimen.resolution_button_big_text_size);
        mFontSizeSmall = getContext().getResources().getDimension(R.dimen.resolution_button_small_text_size);
        mSelected = false;
    }

    /**
     * Reads attributes from the AttributeSet.
     * 
     * @param attrs {@link AttributeSet} with view parameters.
     */
    private void readAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.CustomModeView);

        // Get colors.
        mSelectedColor = a.getColor(R.styleable.CustomModeView_selectedColor, getContext().getResources().getColor(R.color.default_resolution_text_color));
        mDeselectedColor = a.getColor(R.styleable.CustomModeView_deselectedColor, getContext().getResources().getColor(R.color.default_resolution_text_color));

        // Get font size.
        mFontSizeBig = a.getFloat(R.styleable.CustomModeView_fontSizeBig, getContext().getResources().getDimension(R.dimen.resolution_button_big_text_size));
        mFontSizeSmall = a.getFloat(R.styleable.CustomModeView_fontSizeSmall, getContext().getResources().getDimension(R.dimen.resolution_button_small_text_size));

        // Get state.
        mSelected = a.getBoolean(R.styleable.CustomModeView_selected, false); 
    }

    /**
     * Initializes internal variables.
     */
    private void init() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        if (mSelected) {
            mTextPaint.setColor(mSelectedColor);
        } else {
            mTextPaint.setColor(mDeselectedColor);
        }
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    protected void onDraw (Canvas canvas) {
        if (mSize != null) {            
            Rect clipRect = canvas.getClipBounds();
            Rect firstStringBounds = new Rect();
            Rect secondStringBounds = new Rect();

            // First line
            mTextPaint.setTextSize(mFontSizeBig);

            String widthString = "" + mSize.width;			
            mTextPaint.getTextBounds(widthString, 0, widthString.length(), firstStringBounds);			

            canvas.drawText(widthString, 
                    (clipRect.width() / 2) - Math.round((firstStringBounds.width() / 2)) + (clipRect.left / 2), 
                    firstStringBounds.height(), 
                    mTextPaint);

            // Second line
            mTextPaint.setTextSize(mFontSizeSmall);

            String heightString = "X " + mSize.height;
            mTextPaint.getTextBounds(heightString, 0, heightString.length(), secondStringBounds);

            canvas.drawText(heightString, 
                    (clipRect.width() / 2) - Math.round(firstStringBounds.width() / 2) + (firstStringBounds.width() - secondStringBounds.width()) + (clipRect.left / 2), 
                    firstStringBounds.height() + secondStringBounds.height() + SECOND_LINE_VERTICAL_GAP, 
                    mTextPaint);
        }
    }

    /* Attribute accessors */

    /**
     * Sets resolution to display.
     * 
     * @param res {@link Size} value to display.
     */
    public void assignSize(Camera.Size res) {
        if (res != null) {
            mSize = res;
        }
        this.invalidate();
    }

    /**
     * Returns assigned resolution.
     * 
     * @return Assigned {@link Size} object.
     */
    public Camera.Size getAssignedSize() {
        return mSize;
    }

    /**
     * Returns selection state.
     * 
     * @return <b>true</b> if view is in selected state. <b>false</b> otherwise.
     */
    public boolean getSelected() {
        return mSelected;
    }

    /**
     * Sets selection state.
     * 
     * @param Selection state.
     */
    public void setSelected(boolean selected) {
        this.mSelected = selected;
        if (selected) {
            mTextPaint.setColor(mSelectedColor);
        } else {
            mTextPaint.setColor(mDeselectedColor);
        }
    }
}
