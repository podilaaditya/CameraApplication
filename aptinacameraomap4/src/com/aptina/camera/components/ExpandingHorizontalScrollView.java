/**
 * 
 */
package com.aptina.camera.components;

import com.aptina.R;
import com.aptina.miscellaneous.DimensionConverter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

/**
 * @author stoyan
 *
 */
public class ExpandingHorizontalScrollView extends HorizontalScrollView{
	private static final String TAG = ExpandingHorizontalScrollView.class.getName();
	private ViewGroup.LayoutParams mLayoutParams = null;
	
	/**
	 * The burst number that we are on. Continue to expand the scroll view
	 * for a few bursts and then stop expanding
	 */
	private int mBurstFrame = 0;
	/**
     * Initial width of burst area.
     */
    private float mInitBurstScrollerWidth;
    /**
     * Initial height of burst area.
     */
    private float mInitBurstScrollerHeight;
	/**
     * Constructor with context.
     * 
     * @param context Application context.
     */
    public ExpandingHorizontalScrollView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor with context and attribute set.
     * 
     * @param context Application context.
     * @param attrs Attribute set.
     */
    public ExpandingHorizontalScrollView(Context context, AttributeSet attrs) {
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
    public ExpandingHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        
    }
    
    private void init(){
    	mInitBurstScrollerWidth = DimensionConverter.dipToPixels(getContext(), getContext().getResources().getDimension(R.dimen.burst_scroll_appear_width));
    	mInitBurstScrollerHeight = DimensionConverter.dipToPixels(getContext(), getContext().getResources().getDimension(R.dimen.burst_scroll_appear_height));
    }
    
    public void setExpandToFrame(int frame_number){
    	mBurstFrame = frame_number;
    }
    
    public void setWidth(float width_percent){
    	mLayoutParams = this.getLayoutParams();
    	mLayoutParams.width = getWidthIncrement(width_percent);
    	this.setLayoutParams(mLayoutParams);
    }
    
    public void setHeight(float height_percent){
    	mLayoutParams = this.getLayoutParams();
    	mLayoutParams.height = (int) (mInitBurstScrollerHeight * height_percent);
    	this.setLayoutParams(mLayoutParams);
    }
    
    private int getWidthIncrement(float width_percent){
    	return (int) (mInitBurstScrollerWidth * width_percent + mInitBurstScrollerWidth * mBurstFrame);
    }

}
