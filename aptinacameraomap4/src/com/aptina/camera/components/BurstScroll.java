/**
 * 
 */
package com.aptina.camera.components;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aptina.R;
import com.aptina.camera.CameraInfo;
import com.aptina.miscellaneous.DimensionConverter;

/**
 * @author stoyan
 *
 */
public class BurstScroll extends LinearLayout{

	/**
	 * Tag for debuging
	 */
	private static final String TAG = "BurstScroll";
    /**
     * Indicates that view is hiding itself.
     */
    private boolean mHiding = false;
    /**
     * Fade in animation.
     */
    private Animation mFadeInAnim = null;

    /**
     * Fade out animation.
     */
    private Animation mFadeOutAnim = null;
    
    /**
     * Appear animation for the scroll view
     */
    private AnimatorSet mScrollAppearAnim = null;
    
    /**
     * Expand per burst thumbnail addition animation,
     * until we reach 3 thumbnails and @dimensions/burst_scroll_max_width,
     */
    private AnimatorSet mScrollExpandAnim = null;
    
    /**
     * Collapse the animation for the scroll view. Removes
     * all the thumbnail views and does a clear
     */
    private AnimatorSet mScrollCollapseAnim = null;
    
    /**
     * Boolean telling us if @mScrollAppearAnim has run
     */
    private boolean mBurstSeedCreated = false;
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
     * Maximum width of burst area.
     */
    private float mMaxBurstScrollerWidth;

    /**
     * Buttons scroller.
     */
    private ExpandingHorizontalScrollView mBurstImageScroller = null;

    /**
     * Thumbnail container.
     */
    private LinearLayout mBurstImageContainer = null;
    /**
     * List of all the burst thumbnails
     */
    private ArrayList<View> mBurstThumbnails = new ArrayList<View>();
    
    /**
     * The textview that displays the desired number of bursts to capture
     */
    private TextView mBurstsToCaptureView = null;
    /**
     * Minus button
     */
    private ImageView mMinusButton = null;
    /**
     * Minus button
     */
    private ImageView mPlusButton = null;
    
    /**
     * Number holding how many burst images we should take
     */
    private int mBurstsToCaptureIndex = 0;

    /**
     * Scroll offset.
     */
    private int scrollOffset = -1;
    
    /**
     * Context
     */
    private Context mContext = null;
    BurstScroll bScroll = null;
	/**
     * Constructor with context.
     * 
     * @param context Application context.
     */
    public BurstScroll(Context context) {
        super(context);
        mContext = context;
        init();
    }

    /**
     * Constructor with context and attribute set.
     * 
     * @param context Application context.
     * @param attrs Attribute set.
     */
    public BurstScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    /**
     * Constructor with context. attribute set and default style.
     * 
     * @param context Application context.
     * @param attrs Attribute set.
     * @param defStyle Style resource.
     */
    public BurstScroll(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }
    
    /**
     * Internal initialization.
     */
    private void init() {
    	LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.burst_scroll, this);

        
        this.requestFocus();
        mMinusButton = (ImageView) findViewById(R.id.minus_button);
        mMinusButton.requestFocus();
		mMinusButton.setOnClickListener(mMinusButtonListener);
        mPlusButton = (ImageView) findViewById(R.id.plus_button);
        mPlusButton.requestFocus();
        mPlusButton.setOnClickListener(mPlusButtonListener);
        
        mBurstsToCaptureView = (TextView) findViewById(R.id.bursts_to_capture_text);
        mBurstsToCaptureView.setText(getBurstNumber());
        // Get max burst scroller width from the resources.
        mMaxBurstScrollerWidth = DimensionConverter.dipToPixels(getContext(), getContext().getResources().getDimension(R.dimen.burst_scroll_max_width));        

        // Init layout variables.
        mBurstImageContainer = (LinearLayout)findViewById(R.id.burst_image_container);
        mBurstImageScroller = (ExpandingHorizontalScrollView)findViewById(R.id.burst_scroller);
        mBurstImageScroller.setSmoothScrollingEnabled(true);

        // Init layouts & layout params.
        mButtonLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mButtonLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        mDividerLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        mDividerLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        mMinScrollerParams = new RelativeLayout.LayoutParams(mBurstImageScroller.getLayoutParams());
        mMinScrollerParams.width = LayoutParams.WRAP_CONTENT;
        mMinScrollerParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        mMaxScrollerParams = new RelativeLayout.LayoutParams(mBurstImageScroller.getLayoutParams());		
        mMaxScrollerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mMaxScrollerParams.width = Math.round(mMaxBurstScrollerWidth);
       
        // Animations.
        initAnimations();
    }
    
    /**
     * Instantiate all of the global animation references
     */
    private void initAnimations(){
        mFadeInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.generic_fade_in);        
        mFadeInAnim.setAnimationListener(new AnimationListener() {            
            @Override
            public void onAnimationStart(Animation animation) { 
                // Scroll to selected button.
                if (scrollOffset >= 0) {
                	mBurstImageScroller.smoothScrollTo(scrollOffset, 0);
                    scrollOffset = -1;
                }
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) { }
            
            @Override
            public void onAnimationEnd(Animation animation) {
            	mHiding = false;
            	
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
            	mHiding = true;
                setVisibility(GONE);
            }
        });
        
        mScrollAppearAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.anim.burst_scroll_appear);
        mScrollAppearAnim.setTarget(mBurstImageScroller);
        mScrollAppearAnim.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mBurstSeedCreated = true;
				
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationStart(Animator animation) {
			}

        	
        });
        mScrollExpandAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.anim.burst_scroll_expand);
        mScrollExpandAnim.setTarget(mBurstImageScroller);
        mScrollExpandAnim.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator animation) {	
			}

			@Override
			public void onAnimationEnd(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationStart(Animator animation) {
			}

        	
        });
        
        mScrollCollapseAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.anim.burst_scroll_collapse);
        mScrollCollapseAnim.setTarget(mBurstImageScroller);
        mScrollCollapseAnim.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				cleanScroll();
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationStart(Animator animation) {

			}

        	
        });
    }
    
    /**
     * Expand the scroll view from (w,h) = (0,0) at the beginning
     * of the burst capture so that the first thumbnail does not get
     * sized wrong due to taking the dimensions of a changing view parent
     */
    public void createSeed(){
    	if(!mBurstSeedCreated){
        	mBurstImageScroller.setExpandToFrame(0);
    		mScrollAppearAnim.start();
    		
    		//Figure out how many fake bursts we need
    		int val = Integer.valueOf(mBurstsToCaptureView.getText().toString());
    		mFakeBurstToTake = val/3 - 1;
    	}
    	//TODO include when real n-burst is usable, also fix bug in collapse -> animation and thumbnail threading problem
//    	else{
//    		mScrollCollapseAnim.start();
//    		mBurstImageScroller.setExpandToFrame(0);
//    		mScrollAppearAnim.start();
//    	}
    }

    /**
     * Method that creates a blank view with a gray image, to 
     * serve as a placeholder for when the thumbnail is ready
     * @param idx The index of the thumbnail to create a blank for
     */
    public void createBlank(int idx){
    	int index = mFakeBlankIndexHolder++;//TODO remove
    	Log.e(TAG, "createBlank FAKE index : " + index);
    	switch(index){
    	case 1:
    	case 2:
    	case 3:
    	case 4:
    		mBurstImageScroller.setExpandToFrame(index);
    		mScrollExpandAnim.start();
    		break;
    		default:
    			break;
    	}
    	View view = View.inflate(mContext, R.layout.burst_thumb_blank, null);
//		TextView text = (TextView) view.findViewById(R.id.text);
//    	mBurstThumbnails.add(view);
    	mBurstThumbnails.add(view);
    	mBurstImageContainer.addView(view);
    	
    }
    
    /**
     * Method that sets the thumbnail image to the blank
     * placeholder view
     * @param index The index of the blank view to add bitmap to
     */
    public void addThumbBitmap(int idx, Bitmap bitmap){
//        if (index < 0 || index >= Integer.getInteger(getBurstNumber())) {
//            throw new IndexOutOfBoundsException("BurstThumbnailsComponent couldn't add image at index: " + index);
//        }
//    	if(mBurstImageScroller.getVisibility() == GONE){
//    		mBurstImageScroller.setVisibility(VISIBLE);
//    	}
//    	
    	
    	int index = mFakeSaveIndexHolder++;//TODO remove
    	Log.e(TAG, "addThumbBitmap FAKE index : " + index);
        int miniThumbWidth = mBurstThumbnails.get(index).getWidth() -  mBurstThumbnails.get(index).getPaddingLeft() -  mBurstThumbnails.get(index).getPaddingRight();
        int miniThumbHeight =  mBurstThumbnails.get(index).getHeight() -  mBurstThumbnails.get(index).getPaddingTop() -  mBurstThumbnails.get(index).getPaddingBottom();
        Log.i(TAG,"(miniThumbWidth,miniThumbHeight) : " + "(" + miniThumbWidth + "," + miniThumbHeight + ")");
        if(bitmap == null){
        	Log.e(TAG, "addThumbBitmap bitmap == null");
        }
        Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap, miniThumbWidth, miniThumbHeight);
        
        View burstBlank = mBurstThumbnails.get(index);
        ImageView iView = (ImageView)burstBlank.findViewById(R.id.burst_thumb_image);
        burstBlank.setVisibility(View.VISIBLE);
        iView.setImageBitmap(thumbBitmap);
        mBurstImageContainer.removeViewAt(index);
        mBurstImageContainer.addView(burstBlank, index);
        
        //Scroll to the farthest right
        mBurstImageScroller.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        
    }
    /**
     * Method that removes all the thumbnails from the viewgroup if they exists.
     * Closes the HorizontalScrollView as well
     */
    public void cleanScroll(){
    	mBurstImageScroller.setVisibility(GONE);
    	mBurstImageContainer.removeAllViews();
    	mBurstThumbnails.clear();
    	mBurstImageScroller.setVisibility(VISIBLE);
    }
    
    @Override
    public void setVisibility(int visibility) {
    	Log.i(TAG,"setVisibility : " + visibility);
    	super.setVisibility(visibility);
        if ((visibility == View.GONE) && (this.getVisibility() == View.VISIBLE)) {
            startAnimation(mFadeOutAnim);
        } else if ((visibility == View.VISIBLE) && (this.getVisibility() != View.VISIBLE)) {
            startAnimation(mFadeInAnim);
        }
        
    }

    
    OnClickListener mMinusButtonListener = new OnClickListener(){

		@Override
		public void onClick(View view) {
//			Log.i(TAG, "minus");
			mBurstsToCaptureIndex = mBurstsToCaptureIndex > 0 ? 
					--mBurstsToCaptureIndex : mBurstsToCaptureIndex;
			mBurstsToCaptureView.setText(getBurstNumber());
		}
    	
    };
    OnClickListener mPlusButtonListener = new OnClickListener(){

		@Override
		public void onClick(View view) {
//			Log.i(TAG, "plus");
			mBurstsToCaptureIndex = mBurstsToCaptureIndex < burstNumberMapLength - 1  ? 
					++mBurstsToCaptureIndex : mBurstsToCaptureIndex;
			mBurstsToCaptureView.setText(getBurstNumber());
			
			
		}
    	
    };
    /**
     * Function that returns the burst number text
     */
    private int burstNumberMapLength = CameraInfo.BURST_NUMBER_MAP.size();
    public String getBurstNumber(){
    	return "" + CameraInfo.BURST_NUMBER_MAP.get(mBurstsToCaptureIndex);
    }
    
    
    
    
    
    // TODO Get rid of this function when N-Burst is in ACCESS
    /**
     * Fake burst index numbers so that 
     * I can test N-burst before it is done
     */
    private int mFakeBlankIndexHolder = 0;
    private int mFakeSaveIndexHolder = 0;
    
    //keeps the bursts comming artificially
    private int mFakeBurstToTake = 0;
    public boolean continueFakeBurst(){
    	return mFakeBurstToTake-- > 0 ? true : false;
    }


 
}
