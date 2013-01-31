package com.aptina.logger.writer; 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import android.os.Environment;

/**
 * Implements logger to store log in the file. Stores logs in internal storage.
 */
final class FileWriter implements IWriter {

    /**
     * File name to store log information.
     */
    private final static String LOG_FILE_NAME = "AptinaLogInfo.txt";

    /**
     * Instance of the class.
     */
    private static FileWriter sInstance;

    /**
     * Private constructor to prevent creating an instance of the class.
     */
    private FileWriter() {
    }

    /**
     * Retrieves an instance of the class.
     * 
     * @return instance of the class.
     */
    public static FileWriter getInstance() {
        return sInstance == null ? sInstance = new FileWriter() : sInstance;
    }

    @Override
    public synchronized void write(String stringToWrite) {
        File logFile = getLogFile();
        if (logFile == null || stringToWrite == null) {
            return;
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(logFile, true);
            fileOutputStream.write(stringToWrite.getBytes());
            fileOutputStream.close();
        } catch (final Exception e) {
        }
    }

    @Override
    public synchronized String getContent() {
        StringBuffer output = new StringBuffer();
        try {
            File logFile = getLogFile();
            if (logFile == null) {
                return output.toString();
            }

            FileInputStream fileInputStream = new FileInputStream(logFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            String separator = System.getProperty("line.separator");
            while ((line = bufferedReader.readLine()) != null) {
                output.append(line);
                output.append(separator);
            }
        } catch (final Exception e) {
        }

        return output.toString();
    }

    /**
     * Checks log file access.
     * 
     * @return true if wile is available, false otherwise.
     */
    public static boolean hasAccessToSdCard() {
        if (getLogFile() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieves log file or null if something goes wrong.
     * 
     * @return log file from SD card.
     */
    private static File getLogFile() {
        File file = null;
        try {
            String externalStorageState = Environment.getExternalStorageState();
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            if (externalStorageState.equals(Environment.MEDIA_MOUNTED) && externalStorageDirectory != null) {
                file = new File(externalStorageDirectory, LOG_FILE_NAME);
            }
        } catch (final Exception e) {
        }

        return file;
    }
}