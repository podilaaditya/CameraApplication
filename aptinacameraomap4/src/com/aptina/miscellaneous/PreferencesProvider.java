package com.aptina.miscellaneous;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wrapper class for accessing preferences.
 */
public class PreferencesProvider {
    /**
     * Application preferences file name.
     */
    public final static String PREFERENCES_NAME = "AptinaCameraPrefs";
    
    /**
     * Logging switch preferences key.
     */
    public final static String PREFERENCES_LOGGING_KEY = "loggingOn";  
    

    
    /**
     * Saves logging indicator into the preferences.
     * 
     * @param context Application context. 
     * @param enabled Logging switch value.
     */
    public static void setLogging(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);        
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREFERENCES_LOGGING_KEY, enabled);
        editor.commit();
    }

    
    /**
     * Reads and returns logging indicator from the preferences.
     * 
     * @param context Application context.
     * 
     * @return <b>true</b> if logging is on, <b>false</b> otherwise.
     */
    public static boolean isLoggingOn(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES_NAME, 0);
        boolean isLoggingEnabled = settings.getBoolean(PREFERENCES_LOGGING_KEY, true);
        return isLoggingEnabled;
    }

}
