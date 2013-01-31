package com.aptina.logger;

import android.util.Log;

import com.aptina.logger.formatter.IFormatter;
import com.aptina.logger.formatter.SimpleExceptionFormatter;
import com.aptina.logger.writer.IWriter;
import com.aptina.logger.writer.WriterFactory;

/**
 * Class to log messages.
 */
public final class Logger {
    /**
     * whether logging is enabled.
     */
    private static boolean sLoggingEnabled = true;
    
    /**
     * Whether trace messages must be written to log.
     */
    private static boolean sTracing = true;

    /**
     * The writer.
     */
    private static IWriter sWriter;

    /**
     * The formatter.
     */
    private static IFormatter sFormatter;

	private static String TAG = "AptinaCameraAppOMAP4";

    /**
     * Static constructor.
     */
    static {
        sWriter = WriterFactory.getWriter();
        sFormatter = new SimpleExceptionFormatter();
    }
    
    /**
     * Initializes logger. Parameters won't be assigned if one of the parameters
     * have null value.
     * 
     * @param initWriter
     *            writer
     * @param initFormatter
     *            formatter
     */
    public static void init(IWriter initWriter, IFormatter initFormatter) {
        if (initWriter == null || initFormatter == null) {
            return;
        }

        sWriter = initWriter;
        sFormatter = initFormatter;
    }

    /**
     * Switches logging on/off.
     * 
     * @param enabled <b>true</b> to enable logging, <b>false</b> to disable logging.
     */
    public static void setLogging(boolean enabled) {
        if (!enabled) {
            logMessage("Logging is off");
            sLoggingEnabled = enabled;
        } else {
            sLoggingEnabled = enabled;
            logMessage("Logging is on");
        }
    }
    
    /**
     * Indicates if logging is enabled or disabled.
     * 
     * @return <b>true</b> if logging is enabled, and <b>false</b> otherwise.
     */
    public static boolean isLoggingOn() {
        return sLoggingEnabled;
    }
    
    /**
     * Retrieves all logged entities united to the string.
     * 
     * @return logged content.
     */
    public static synchronized String getContent() {
        return sWriter.getContent();
    }

    /**
     * Logs exception with specified message.
     * 
     * @param exception
     *            the exception.
     * @param friendlyMessage
     *            the friendly message.
     */
    public static void logApplicationException(Exception exception, String friendlyMessage) {
        //logEntry(exception, friendlyMessage);
        Log.d("CameraActivity" , exception.toString());
        exception.printStackTrace();
    }

    /**
     * Logs message.
     * 
     * @param friendlyMessage
     *            the friendly message.
     */
    public static void logMessage(String friendlyMessage) {
        //logEntry(null, friendlyMessage);
        Log.d("CameraActivity" , friendlyMessage);
    }

    /**
     * Logs trace message.
     * 
     * @param friendlyMessage
     *            the friendly message.
     */
    public static void logTraceMessage(String friendlyMessage) {
        if(!sTracing) {
            return;
        }
        
        logEntry(null, friendlyMessage);
    }
    
    /**
     * Logs entry.
     * 
     * @param exception
     *            the exception.
     * @param friendlyMessage
     *            the friendly message.
     */
    private static synchronized void logEntry(Exception exception, String friendlyMessage) {
        if (sLoggingEnabled) {
            sWriter.write(sFormatter.format(exception, friendlyMessage));
        }
    }
}
