package com.aptina.camera.components;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.aptina.R;
import com.aptina.miscellaneous.DimensionConverter;

/**
 * Custom component holding buttons for selecting various camera modes
 */
public class ModeSelector extends FrameLayout implements OnClickListener {
	/**
    * Tag for debuging
    */
	private static final String TAG = "ModeSelector";
	
    /**
     * Mode selection listener.
     */
    public interface OnModeSelectionListener {
        /**
         * Called when user presses one of buttons in the list.
         * @param view Button pressed.
         * @param modeIndex Selected mode index.
         */
        public void onModeSelected(View view, int modeIndex);
    }

    /**
     * Indicates that view is hiding itself.
     */
    private boolean mHiding = false;

    /**
     * Default button layout.
     */	
    private LayoutParams mButtonLayoutParams = null;

    /**
     * Default divider layout.
     */	
    private LayoutParams mDividerLayoutParams = null;

    /**
     * Max scroller size.
     */
    private RelativeLayout.LayoutParams mMaxScrollerParams = null;

    /**
     * Min scroller size.
     */
    private RelativeLayout.LayoutParams mMinScrollerParams = null;

    /**
     * Maximum width of buttons area.
     */
    private float mMaxButtonScrollerWidth;

    /**
     * Buttons scroller.
     */
    private HorizontalScrollView mButtonsScroller = null;

    /**
     * Buttons container.
     */
    private LinearLayout mButtonsContainer = null;

    /**
     * Buttons array.
     */
    private ArrayList<View> mButtons = new ArrayList<View>();

    /**
     * Event listener.
     */
    private OnModeSelectionListener mListener;

    /**
     * Fade in animation.
     */
    private Animation mFadeInAnim = null;

    /**
     * Fade out animation.
     */
    private Animation mFadeOutAnim = null;

    /**
     * Scroll offset.
     */
    private int scrollOffset = -1;
    
    /**
     * Constructor with context.
     * 
     * @param context Application context.
     */
    public ModeSelector(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor with context and attribute set.
     * 
     * @param context Application context.
     * @param attrs Attribute set.
     */
    public ModeSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor with context. attribute set and default style.
     * 
     * @param context Application context.
     * @param attrs Attribute set.
     * @param defStyle Style resource.
     */
    public ModeSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Internal initialization.
     */
    private void init() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.selector, this);
       
        
        // Get max button scroller width from the resources.
        mMaxButtonScrollerWidth = DimensionConverter.dipToPixels(getContext(), getContext().getResources().getDimension(R.dimen.mode_selector_scroller_width));        

        // Init layout variables.
        mButtonsContainer = (LinearLayout)findViewById(R.id.selector_buttons_container);
        mButtonsScroller = (HorizontalScrollView)findViewById(R.id.selector_buttons_scroller);
        mButtonsScroller.setSmoothScrollingEnabled(true);
        
        // Init layouts & layout params.
        mButtonLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mButtonLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        mDividerLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        mDividerLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        mMinScrollerParams = new RelativeLayout.LayoutParams(mButtonsScroller.getLayoutParams());
        mMinScrollerParams.width = LayoutParams.WRAP_CONTENT;
        mMinScrollerParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        mMaxScrollerParams = new RelativeLayout.LayoutParams(mButtonsScroller.getLayoutParams());		
        mMaxScrollerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mMaxScrollerParams.width = Math.round(mMaxButtonScrollerWidth);

        // Animations.
        mFadeInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.generic_fade_in);        
        mFadeInAnim.setAnimationListener(new AnimationListener() {            
            @Override
            public void onAnimationStart(Animation animation) { 
                // Scroll to selected button.
                if (scrollOffset >= 0) {
                    mButtonsScroller.smoothScrollTo(scrollOffset, 0);
                    scrollOffset = -1;
                }
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) { }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                
            }
        });     
        mFadeOutAnim = AnimationUtils.loadAnimation(getContext(), R.anim.generic_fade_out);
        mFadeOutAnim.setAnimationListener(new AnimationListener() {			
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                setVisibility(GONE);
            }
        });
    }

    /**
     * Returns new divider view.
     * 
     * @return divider {@link ImageView}.
     */
    private ImageView createDivider() {
        ImageView divider = new ImageView(getContext());
        divider.setImageResource(R.drawable.mode_selection_div);
        divider.setScaleType(ScaleType.FIT_XY);

        return divider;
    }

    /**
     * Updates layout for button scrollers.
     */
    private void updateUI() {
        if (mButtons.size() < 4) {
            mButtonsScroller.setLayoutParams(mMinScrollerParams);
        } else {
            mButtonsScroller.setLayoutParams(mMaxScrollerParams);
        }
    }

    /**
     * Sets the buttons for the container.
     * 
     * @param buttons {@link ArrayList} of the buttons.
     */
    public void setButtons(ArrayList<View> buttons) {
        if (buttons == null) {
            throw new IllegalArgumentException();
        }

        this.mButtons = buttons;

        mButtonsContainer.removeAllViews();		
        for (int i = 0; i < buttons.size(); i++) {
            addButton(buttons.get(i));
        }
    }

    /**
     * Adds one button to the end of the buttons list.
     * 
     * @param button Button {@link View}.
     */
    public void addButton(View button) {
        if (button == null) {
            throw new IllegalArgumentException();
        }

        if (mButtonsContainer.getChildCount() > 0) {
            mButtonsContainer.addView(createDivider(), mDividerLayoutParams);
        }		
        mButtonsContainer.addView(button, mButtonLayoutParams);		
        button.setOnClickListener(this);

        updateUI();
    }

    /**
     * Removes button from list by its index.
     * 
     * @param idx Button number.
     */
    public void removeButton(int idx) {
        int childCount = mButtonsContainer.getChildCount();
        int buttonIdx = idx * 2;

        if ((buttonIdx < 0) && (buttonIdx > childCount)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        View button = mButtonsContainer.getChildAt(buttonIdx);

        if (childCount > buttonIdx) {
            View rightDivider = mButtonsContainer.getChildAt(idx+1);
            mButtonsContainer.removeView(rightDivider);
        }

        if (buttonIdx > 1) {
            View leftDivider = mButtonsContainer.getChildAt(idx-1);
            mButtonsContainer.removeView(leftDivider);
        }

        mButtonsContainer.removeView(button);
    }

    /**
     * Removes button from container by reference.
     * 
     * @param button Button to remove.
     */
    public void removeButton(View button) {
        if (button == null) {
            throw new IllegalArgumentException();
        }

        int idx = mButtonsContainer.indexOfChild(button);
        if (idx > -1) {
            removeButton(idx / 2);
        }
    }

    /**
     * Removes all buttons from the container.
     */
    public void removeAllButtons() {
        mButtonsContainer.removeAllViews();
    }

    /**
     * Sets mode button click listener.
     * 
     * @param modeSelectionListener {@linkplain OnModeSelectionListener} to respond click events.
     */
    public void setModeSelectionListener(ModeSelector.OnModeSelectionListener modeSelectionListener) {
        this.mListener = modeSelectionListener; 
    }

    /**
     * Scrolls view to display given button.
     * 
     * @param index Index of button to scroll to.
     */
    public void scrollToButton(int index) {
        int offset = 0;
        for (int i = 0; i < index; i++) {            
            offset += getResources().getDimensionPixelSize(R.dimen.resolution_selector_holder_size) + getResources().getDimensionPixelSize(R.dimen.selector_buttons_margin);
        }
        
        if (getVisibility() == View.VISIBLE) {
            mButtonsScroller.smoothScrollTo(offset, 0);
        } else {
            // Delay scrolling until view really appears.
            // This is needed because Android use lazy layouts.
            scrollOffset = offset;
        }
    }
    
    /**
     * Scrolls view to display given button.
     * 
     * @param button Button view to scroll to.
     */
    public void scrollToButton(View button) {
        if (button != null) {
            int index = mButtonsContainer.indexOfChild(button);
            if (index >= 0) {
                scrollToButton(index / 2);
            }
        }
    }
    
    @Override
    public void onClick(View view) {
    	Log.i(TAG,"onClick");
        int modeIdx = 0;

        if (view.getClass().equals(RelativeLayout.class)) {
            modeIdx = mButtonsContainer.indexOfChild(view) / 2;
        } else {
            modeIdx = mButtonsContainer.indexOfChild((View)view.getParent().getParent()) / 2;
        }

        if (mListener != null) {
            mListener.onModeSelected(view, modeIdx);
        }
    }

    @Override
    public void setVisibility(int visibility) {
    	Log.i(TAG,"setVisibility");
        if ((visibility == View.GONE) && (this.getVisibility() == View.VISIBLE)) {
            if (!mHiding) {
                mHiding = true;
                startAnimation(mFadeOutAnim);
            } else {
                mHiding = false;
                super.setVisibility(visibility);
            }
        } else if ((visibility == View.VISIBLE) && (this.getVisibility() != View.VISIBLE)) {
            super.setVisibility(visibility);
            startAnimation(mFadeInAnim);
        }
    }
    
}
