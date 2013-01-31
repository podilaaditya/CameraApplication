package com.aptina.camera.components;


import com.aptina.R;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * @author stoyan
 *
 */
public class FocusMenu {
	private static final String TAG = "FocusMenu";

	/**
	 * Variable to toggle logging across the whole class
	 */
	private static final boolean LOGGING_ON = false;
	
	public interface MenuLayoutInterface{
		/**
		 * Add each focus option view to the focus menu section
		 * of camera.xml
		 * @param view The focus menu option
		 */
		public void addViewToLayout(View view);
		
		/**
		 * Toggle the focus menu section of camera.xml visible
		 * once all of the options items have been created and added
		 * to the main view. Menu panel button should be used to toggle
		 * 
		 * @param visible Toggle boolean
		 */
		public void setVisible(boolean visible);
		
		/**
		 * Call to change the focus mode of the camera and exit the
		 * focus men view group
		 * 
		 * @index the index of the focus view item
		 */
		public void OnFocusClick(int index);
	}
	
	/**
	 * List of supported focus modes.
	 */
	 private String[] mFocusModes; 
	 
	 /**
	  * Hold the context of the CameraActivity for view inflation
	  */
	 private Context camContext;
	 /**
	  * Hold the current camera focus when this dialog is shown
	  */
	 private String mCurrentFocus;
	 /**
	  * Array of radio buttons to choose focus mode.
	  */
	 private RadioButton[] mFocusRadioButtons;
	 
	 /**
	  * Index of the focus radio button touched on MotionEven.ActionDown
	  */
	 private int radioDown = -1;
	 /**
	  * Interface to handle layout changes
	  */
	 MenuLayoutInterface mCallbackTarget = null;
	 
	 
	 public FocusMenu(Context context, FocusMenu.MenuLayoutInterface callback) {
		 camContext = context;
		 mCallbackTarget = callback;
	 }

	 public void setCallbackTarget(FocusMenu.MenuLayoutInterface callback){
			mCallbackTarget = callback;
	 }
	 

	 public void setFocusModesArray(String[] focusModes, String cam_focus){
		 LOGI("setFocusModesArray");
	
		 mFocusModes = focusModes;
		 mCurrentFocus = cam_focus;
		 if (mFocusModes == null || mFocusModes.length == 0) {
			 LOGI("mFocusModes null || mFocusModes.length == 0");
		 } else {
			 if (mCurrentFocus != null && mCurrentFocus.length() > 0) {
				 LOGI("currentFocus : " + mCurrentFocus);
			 } else {
				 LOGI("no current focus : setting focus to auto");
				 mCurrentFocus = "continuous-picture";
			 }
            
			 mFocusRadioButtons = new RadioButton[mFocusModes.length];
			 for (int i = 0; i < mFocusModes.length; i++) {
				 View view = View.inflate(camContext, R.layout.options_item, null);
				 TextView text = (TextView) view.findViewById(R.id.text);
				 text.setText(mFocusModes[i]);
				 mFocusRadioButtons[i] = (RadioButton) view.findViewById(R.id.radioButton);
				 if (mCurrentFocus != null && mCurrentFocus.equalsIgnoreCase(mFocusModes[i])) {
					 mFocusRadioButtons[i].setChecked(true);
				 }
	             final int index = i;
	             RadioButton rb = (RadioButton) view.findViewById(R.id.radioButton);
	             rb.setOnTouchListener(new View.OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch(event.getAction()){
						case MotionEvent.ACTION_DOWN:
							LOGI("ACTION_DOWN index : " + index);
							
							radioDown = index;
							break;
						case MotionEvent.ACTION_UP:
							LOGI("ACTION_UP index : " + index);
							if(radioDown == index && radioDown != -1){
								for(int j = 0; j < mFocusRadioButtons.length; j++){
									((RadioButton) mFocusRadioButtons[j]).setChecked(j == index);
								}
								
								mCallbackTarget.OnFocusClick(index);
							}
							break;
						case MotionEvent.ACTION_MOVE:
							LOGI("ACTION_MOVE index : " + index);

							break;
						}
						return true;
					}
				});

				 mCallbackTarget.addViewToLayout(view);
            }
			 
			 mCallbackTarget.setVisible(true);
        }
    }
	 
	 /**
	  * Private class for logging that we can toggle for quick
	  * logging
	  * @param msg The string to log
	  */
	 private void LOGI(String msg){
		 if(LOGGING_ON){
			 Log.i(TAG, msg);
		 }
	 }
	 
	 




}
