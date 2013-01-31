package com.aptina.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aptina.R;

/**
 * This view is for the scroll selection in the gallery activity. It is composed of
 * the ImageView, thumbs background, and a TextView inside of a frame layout.
 * 
 * ImageView -- displays the first burst image in a folder
 * Thumbs Background -- displays the thumbnail background behind and enveloping the ImageView
 * TextView -- displays the number of bursts in the set, possibly other info about the burst sequence
 * 
 * @author stoyan
 * 
 */
public class BurstBucketView extends RelativeLayout{
	/**
	 * Tag for debuging
	 */
	private static final String TAG = "BurstBucketView";
    /**
     * The textview that displays the number of bursts in the bucket
     */
    private TextView mBurstBucketNumberView = null;
    /**
     * The first image in the burst sequence to display to the user as a preview 
     */
    private ImageView mBurstBucketImageView = null;
    /**
     * Context
     */
    private Context mContext = null;
	/**
     * Constructor with context.
     * 
     * @param context Application context.
     */
    public BurstBucketView(Context context) {
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
    public BurstBucketView(Context context, AttributeSet attrs) {
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
    public BurstBucketView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }
    
    /**
     * Internal initialization.
     */
    private void init() {
    	LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.burst_bucket, this);
        
        mBurstBucketNumberView = (TextView) findViewById(R.id.burst_bucket_count);
        mBurstBucketImageView = (ImageView) findViewById(R.id.burst_bucket_image);
    }
    
    public ImageView getImageView(){
    	return mBurstBucketImageView;
    }
    
    public void setText(String txt){
    	mBurstBucketNumberView.setText(txt);
    }
    
    public String getText(){
    	return mBurstBucketNumberView.getText().toString();
    }

}
