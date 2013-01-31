package com.aptina.camera.components;

import com.aptina.R;
import com.aptina.logger.Logger;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Implements thumbnail control.
 */
public class ThumbnailControl extends FrameLayout {

    /**
     * Thumbnail type: images.
     */
    public static final int TYPE_IMAGE = 14;

    /**
     * Thumbnail type: videos.
     */
    public static final int TYPE_VIDEO = 15;

    /**
     * Last thumbnail.
     */
    private Bitmap mLastThumbnail;

    /**
     * Uri to the latest thumbnail.
     */
    private Uri mLastUri;

    /**
     * Image holder.
     */
    private ImageView mImageView;

    /**
     * Text holder.
     */
    private View mTextView;

    /**
     * On click listener.
     */
    private OnClickListener listener;

    /**
     * Creates new instance of the class.
     * 
     * @param context Application context.
     */
    public ThumbnailControl(Context context) {
        super(context);
    }

    /**
     * Creates new instance of the class.
     * 
     * @param context Application context.
     * @param attrs Attributes.
     */
    public ThumbnailControl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates new instance of the class.
     * 
     * @param context Application context.
     * @param attrs Attributes.
     * @param defStyle Default style.
     */
    public ThumbnailControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        mImageView = (ImageView)findViewById(R.id.gallery_image);
        mImageView.setOnClickListener(listener);
        mTextView = findViewById(R.id.gallery_info_text);
    }

    /**
     * Loads information about last thumbnail.
     * 
     * @return true id success and false otherwise.
     */
    public boolean show() {
        if (mImageView == null) {
            init();
        }

        if (mLastThumbnail == null) {
            mTextView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(null);
        } else {
            mTextView.setVisibility(View.INVISIBLE);
            if (mLastThumbnail != null) {
                mImageView.setImageBitmap(mLastThumbnail);
            }
        }

        return true;
    }

    /**
     * Updates last thumbnail.
     * 
     * @param contentResolver Content resolver.
     * @return 
     */
    public void update(ContentResolver contentResolver, int type) {        
        Cursor cursor = null;
        try {
            if (type == TYPE_VIDEO) {
                cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Video.Media._ID}, null, null, MediaStore.Video.Media._ID + " DESC");
                if (cursor.moveToFirst()) {
                    int videoId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    Bitmap newThumbnail = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, videoId, MediaStore.Video.Thumbnails.MINI_KIND, null);

                    int miniThumbWidth = getWidth() - getPaddingLeft() - getPaddingRight();
                    int miniThumbHeight = getHeight() - getPaddingTop() - getPaddingBottom();
                    
                    if (miniThumbWidth != 0 || miniThumbHeight != 0) {
                        mLastThumbnail = ThumbnailUtils.extractThumbnail(newThumbnail, miniThumbWidth, miniThumbHeight);
                    } else {
                        mLastThumbnail = null;
                    }
                    
                    mLastUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Integer.toString(videoId));
                } else {
                    mLastThumbnail = null;
                    mLastUri = null;
                }
            } else {
                cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Images.Media._ID}, null, null, MediaStore.Images.Media._ID + " DESC");
                if (cursor.moveToFirst()) {
                    int imgId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    Bitmap newThumbnail = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, imgId, MediaStore.Images.Thumbnails.MINI_KIND, null);

                    int miniThumbWidth = getWidth() - getPaddingLeft() - getPaddingRight();
                    int miniThumbHeight = getHeight() - getPaddingTop() - getPaddingBottom();
                    if (miniThumbWidth != 0 || miniThumbHeight != 0) {
                        mLastThumbnail = ThumbnailUtils.extractThumbnail(newThumbnail, miniThumbWidth, miniThumbHeight);
                    } else {
                        mLastThumbnail = null;
                    }
                    mLastUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(imgId));
                } else {
                    mLastThumbnail = null;
                    mLastUri = null;
                }
            }
        } catch(final Exception e) {
            Logger.logApplicationException(e, "ThumbnailControl.update(): failed to update thumbnail picture");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Sets new thumbnail.
     * 
     * @param newThumbnail New thumbnail.
     * @param uri Uri to the latest thumbnail.
     */
    public void setThumbnail(Bitmap newThumbnail, Uri uri) {
        try {
            mLastThumbnail = newThumbnail;
            mLastUri = uri;
        } catch (Exception ex) {
            Logger.logApplicationException(ex, "ThumbnailControl.setThumbnail(): failed to load thumbnail");
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {        
        if (mImageView != null) {
            mImageView.setOnClickListener(l);
        } else {
            listener = l;
        }
    }

    /**
     * Retrieves uri for the latest thumbnail.
     * 
     * @return uri for the latest thumbnail. 
     */
    public Uri getUri() {
        return mLastUri;
    }
    
    /**
     * Sets thumbnail URI.
     * 
     * @param uri Thumbnail URI.
     */
    public void setUri(Uri uri) {
        mLastUri = uri;
    }
}
