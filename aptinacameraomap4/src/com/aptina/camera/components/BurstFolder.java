/**
 * 
 */
package com.aptina.camera.components;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A class that stores the file paths of all the bursts in a folder
 * @author stoyan
 *
 */
public class BurstFolder implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<String> mFolderPictures = new ArrayList<String>();
	
	private File mImageFolder;

	private String mFolderName;
	public BurstFolder(File folder) {
		mImageFolder = folder;
		mFolderName = folder.getName();
	}
	
	public String getFolderName(){
		return mFolderName;
	}
	public File getBurstFolder(){
		return mImageFolder;
	}

	
	public ArrayList<String> getFolderPicturePaths(){
		return mFolderPictures;
	}
	
	public String getPicturePath(int index){
		return mFolderPictures.get(index);
	}
	
	public void addPicturePath(String path){
		mFolderPictures.add(path);
	}
	
	public void setPicturePaths(ArrayList<String> paths){
		mFolderPictures.addAll(paths);
	}

}
