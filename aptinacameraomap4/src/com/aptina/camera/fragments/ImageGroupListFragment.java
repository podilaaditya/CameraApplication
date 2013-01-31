/**
 * 
 */
package com.aptina.camera.fragments;



import java.io.File;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.aptina.BuildConfig;
import com.aptina.R;
import com.aptina.camera.components.BurstBucketView;
import com.aptina.camera.components.BurstFolder;
import com.aptina.data.ImageCache.ImageCacheParams;
import com.aptina.data.ImageFetcher;


/**
 * @author stoyan
 *
 */
public class ImageGroupListFragment  extends Fragment  implements AdapterView.OnItemClickListener {
	private static final String TAG = "ImageGroupListFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    private static final String BURST_FOLDER_PREFIX = "BURST";
    private ArrayList<BurstFolder> mImageFolders = new ArrayList<BurstFolder>();
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    
    private ImageGroupListFragmentInterface callbackListener;
    
    public interface ImageGroupListFragmentInterface{
    	public void OnBurstFolderSelected(int position, BurstFolder folder);
    }
    
    public void setCallbackListener(ImageGroupListFragmentInterface listener){
    	callbackListener = listener;
    }
    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageGroupListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.grid_image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.grid_image_thumbnail_spacing);

        mAdapter = new ImageAdapter(getActivity());

        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        // Set memory cache to 25% of mem class
        cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getFragmentManager(), cacheParams);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.image_group_list_fragment, container, false);
        final GridView mGridView = (GridView) v.findViewById(R.id.group_list_view);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageFetcher.setPauseWork(true);
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            }
        });

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (mAdapter.getNumColumns() == 0) {
                            final int numColumns = (int) Math.floor(
                                    mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                            if (numColumns > 0) {
                                final int columnWidth =
                                        (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                                mAdapter.setNumColumns(numColumns);
                                mAdapter.setItemHeight(columnWidth);
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                                }
                            }
                        }
                    }
                });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    @TargetApi(16)
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	callbackListener.OnBurstFolderSelected(position,mImageFolders.get(position - mNumColumns));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.gallery_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                mImageFetcher.clearCache();
                Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public BurstFolder getCameraFolder(){
    	return (mImageFolders != null && mImageFolders.size() > 0) ? mImageFolders.get(0) : null;
    }
    /**
     * The main adapter that backs the GridView. This is fairly standard except the number of
     * columns in the GridView is used to create a fake top row of empty views as we use a
     * transparent ActionBar and don't want the real top row of images to start off covered by it.
     */
    private int mNumColumns = 0;
    private class ImageAdapter extends BaseAdapter {

        private final Context mContext;
        private int mItemHeight = 0;
        
        private int mActionBarHeight = 0;
        private GridView.LayoutParams mImageViewLayoutParams;

        public ImageAdapter(Context context) {
            super();
            mContext = context;
            loadImages();
            logFolders();
            mImageViewLayoutParams = new GridView.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(
                    android.R.attr.actionBarSize, tv, true)) {
                mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, context.getResources().getDisplayMetrics());
            }
            Log.e(TAG, "Action Bar Height: " + mActionBarHeight);
        }

        @Override
        public int getCount() {
            // Size + number of columns for top empty row
            return mImageFolders.size() + mNumColumns;
        }

        @Override
        public Object getItem(int position) {
            return position < mNumColumns ?
                    null : mImageFolders.get(position - mNumColumns).getFolderPicturePaths().get(0);
        }

        @Override
        public long getItemId(int position) {
            return position < mNumColumns ? 0 : position - mNumColumns;
        }

        @Override
        public int getViewTypeCount() {
            // Two types of views, the normal ImageView and the top row of empty views
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return (position < mNumColumns) ? 1 : 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            // First check if this is the top row
            if (position < mNumColumns) {
                if (convertView == null) {
                    convertView = new View(mContext);
                }
                // Set empty view with height of ActionBar
                convertView.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
                return convertView;
            }

            // Now handle the main ImageView thumbnails
            BurstBucketView burstView;
            if (convertView == null) { // if it's not recycled, instantiate and initialize
            	burstView = new BurstBucketView(mContext);
            	burstView.getImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
            	burstView.setLayoutParams(mImageViewLayoutParams);
            } else { // Otherwise re-use the converted view
                burstView = (BurstBucketView) convertView;
            }

            // Check the height matches our calculated column width
            if (burstView.getLayoutParams().height != mItemHeight) {
            	burstView.setLayoutParams(mImageViewLayoutParams);
            }

            // Finally load the image asynchronously into the ImageView, this also takes care of
            // setting a placeholder image while the background thread runs
            if(mImageFolders.get(position - mNumColumns).getFolderPicturePaths().size() != 0){
            	mImageFetcher.loadImage(mImageFolders.get(position - mNumColumns).getFolderPicturePaths().get(0),
                		mImageFolders.get(position - mNumColumns).getFolderName(),burstView);
            }
            
            
            return burstView;
        }

        /**
         * Sets the item height. Useful for when we know the column width so the height can be set
         * to match.
         *
         * @param height
         */
        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams =
                    new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
            mImageFetcher.setImageSize(height);
            notifyDataSetChanged();
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }
        
        /**
         * Method to get all the images in the Camera folder
         */
        private void loadImages(){
        	String camera_dir = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera";
        	File img_dir = new File(camera_dir);
        	if(img_dir.isDirectory()){
        		BurstFolder cameraFolder = new BurstFolder(img_dir);
        		mImageFolders.add(cameraFolder);
        		File[] files = img_dir.listFiles();
        		for(File f : files){
        			/**
        			 * Go through all the Burst directories and scan for jpegs,
        			 * create a ImageFolder class for each and add all jpeg paths to it
        			 */
        			if(f.isDirectory() && f.getName().startsWith(BURST_FOLDER_PREFIX)){
        				BurstFolder folder = new BurstFolder(f);
        				boolean containsJPG = false;
        				File[] folder_list = f.listFiles();
        				for(File f_file : folder_list){
        					if(f_file.getName().endsWith("jpg") || f_file.getName().endsWith("jpeg")){
        						containsJPG = true;
        						folder.addPicturePath(f_file.getAbsolutePath());
        					}
        				}
        				if(containsJPG){
        					mImageFolders.add(folder);
        				}
        					
        			}
        			
        			/**
        			 * Once done with the Burst directories, then scan the /DCIM/Camera/ 
        			 * for all jpeg images and create a ImageFolder class for each and 
        			 * add all jpeg paths to it
        			 */
        			if(f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")){
        				cameraFolder.addPicturePath(f.getAbsolutePath());
        			}
        		}
//        		mImageFolders.get(0).setPicturePaths(cameraFolder.getFolderPicturePaths());
        	}
        }
       
        private void logFolders(){
        	for(BurstFolder bf : mImageFolders){
        		Log.i(TAG, "folder : " + bf.getFolderName());
        	}
        }
    }
}
	
