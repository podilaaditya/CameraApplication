package com.aptina.miscellaneous;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

import com.aptina.logger.Logger;

import android.os.Environment;
import android.os.StatFs;

public class FileUtils {
    /**
     * Checks that SD card is mounted.
     * @return
     */
    public static boolean isSdCardMounted() {
        try {
            return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        } catch (final Exception ex) {
        }
        return false;
    }

    /**
     * Checks that file exists.
     * 
     * @param path File path.
     * 
     * @return true if file exists, false otherwise.
     */
    public static boolean isFileExist(String path) {
        try {
            return new File(path).exists();
        } catch(final Exception ex) {
        }
        return false;
    }



    /**
     * Retrieves available size on SD card, in bytes.
     * 
     * @return available size on SD card in bytes.
     */
    public static long getAvailableBytes() {
        try {
            String storageDirectory = Environment.getExternalStorageDirectory().toString();
            StatFs statFileSystem = new StatFs(storageDirectory);
            return ((long) statFileSystem.getAvailableBlocks()) * ((long)statFileSystem.getBlockSize());
        } catch(final Exception e) {
        }
        return 0;
    }
}
