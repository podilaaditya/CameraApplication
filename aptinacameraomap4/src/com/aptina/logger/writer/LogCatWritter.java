package com.aptina.logger.writer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

/**
 * Implements writer for logger to store logged information in the default cat
 * logger.
 */
class LogCatWritter implements IWriter {

    /**
     * TAG to recognize application.
     */
    private final static String TAG = "AptinaCamera";

    @Override
    public void write(String stringToWrite) {
        if (stringToWrite == null) {
            return;
        }

        Log.d(TAG, stringToWrite);
    }

    @Override
    public String getContent() {
        String commandOutput = null;
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "logcat", "-d", "AndroidRuntime:E " + TAG + ":V *:S" });


            if (process == null) {
                return null;
            }

            BufferedReader reader = null;
            final StringBuilder commandResults = new StringBuilder();

            try {
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                String separator = System.getProperty("line.separator");
                while ((line = reader.readLine()) != null) {
                    commandResults.append(separator);
                    commandResults.append(line);
                }

                // Deletes first unused 'next line' symbol.
                if (commandResults.length() > 0) {
                    commandResults.deleteCharAt(0);
                }
            } catch (Exception e) {
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }

            commandOutput = commandResults.toString();
        } catch (Exception e) {
            // Ignores this exception.
        }

        return commandOutput == null ? new String() : commandOutput;
    }
}
