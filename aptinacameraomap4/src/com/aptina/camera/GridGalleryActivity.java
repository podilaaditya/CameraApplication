package com.aptina.camera;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.aptina.R;
import com.aptina.camera.components.BurstFolder;
import com.aptina.camera.fragments.ImageGridFragment;
import com.aptina.camera.fragments.ImageGridFragment.ImageGridFragmentInterface;
import com.aptina.camera.fragments.ImageGroupListFragment;
import com.aptina.camera.fragments.ImageGroupListFragment.ImageGroupListFragmentInterface;


public class GridGalleryActivity extends Activity{
    private static final String TAG = "GridGalleryActivity";
    ImageGroupListFragment mFolderListFrag;
    ImageGridFragment mGridFrag;
    
    private static final int ANIM_LEFT_TO_RIGHT = 0;
    private static final int ANIM_TOP_TO_BOTTOM = 1;
    private static final int ANIM_BOTTOM_TO_TOP = 2;
    /**
     * Remember which folder we are currently showing, so that we do not 
     * reload the same folder if the user clicks on it, 1 = camera folder
     * Indices start at 1, since we have an invisible row
     */
    private static int mGridFolderBeingDisplayed = 1;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_gallery_layout);
        
        FragmentManager fragmentManager = this.getFragmentManager();
        
        mFolderListFrag = (ImageGroupListFragment) fragmentManager.findFragmentById(R.id.image_group_fragment);

        if(mFolderListFrag.getCameraFolder() != null){//can be null if there are no images in camera folder, ie we just flashed a new build to device
        	replaceGrid(getNewGridFragment(mFolderListFrag.getCameraFolder()), ANIM_LEFT_TO_RIGHT);
        }else{
        	Toast.makeText(this, "No images to display", Toast.LENGTH_LONG).show();
        	try {
				Thread.sleep(5000);
				finish();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
        
        mFolderListFrag.setCallbackListener(new ImageGroupListFragmentInterface(){

			@Override
			public void OnBurstFolderSelected(int position, BurstFolder folder) {
				Log.i(TAG, "clicked on burst folder : " + position);
				if(position != mGridFolderBeingDisplayed){
					replaceGrid(getNewGridFragment(folder), ANIM_LEFT_TO_RIGHT);
					mGridFolderBeingDisplayed = position;
				}else{
					Log.i(TAG, "Already on folder: index = " + position);
				}
			}
        });
        
        getDisplaySize();
        getStatusBarHeight();
    }
    private void replaceGrid(Fragment newFragment, int anim){
    	FragmentTransaction transaction = getFragmentManager().beginTransaction();
    	switch(anim){
    	case ANIM_LEFT_TO_RIGHT:
    		transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
    		break;
    	case ANIM_TOP_TO_BOTTOM:
    		transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom);
    		break;
    	case ANIM_BOTTOM_TO_TOP:
    		transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top);
    		break;
    		default:
    			transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
    			break;
    	}
		
		transaction.replace(R.id.grid_fragment_container,newFragment);
		transaction.commit();
    }
    
    private ImageGridFragment getNewGridFragment(final BurstFolder folder){
    	ImageGridFragment frag = new ImageGridFragment(folder);
    	frag.setCallbackListener(new ImageGridFragmentInterface(){
			@Override
			public void OnImageSelected(ImageView imageView) {
//				Log.i(TAG, "Image in gridview clicked");
//				mExpandedImageFrag = new GalleryExpandedImageFragment();
//				mExpandedImageFrag.setImage(imageView);
//				mExpandedImageFrag.setCallbackListener(new ExpandedImageFragmentInterface(){
//
//					@Override
//					public void OnImageTapSelected() {
//						replaceGrid(getNewGridFragment(folder), ANIM_BOTTOM_TO_TOP);
//						
//					}
//					
//				});
//				replaceGrid(mExpandedImageFrag, ANIM_TOP_TO_BOTTOM);  
			}
    	});
    	
    	return frag;
    }
    
	 public Point getDisplaySize(){
		 Point disp = new Point();
	     getWindowManager().getDefaultDisplay().getSize(disp);
	   
	     Log.e(TAG, "display size : (" + disp.x + ", " + disp.y + ")");
	     DisplayMetrics outMetrics = new DisplayMetrics();
	     getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
	     Log.e(TAG, "display metrics : (" + outMetrics.widthPixels + ", " + outMetrics.heightPixels + ")");

	     return disp;
	}

	 public int getStatusBarHeight() {
		  int result = 0;
		  int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		  if (resourceId > 0) {
		      result = getResources().getDimensionPixelSize(resourceId);
		  }
		  Log.e(TAG, "Status bar height : " + result);
		  return result;
		}

}
