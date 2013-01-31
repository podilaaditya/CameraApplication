package com.aptina.miscellaneous;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class to format date to string.
 */
public class DateTimeUtils {

    /**
     * Default time date format.
     */
    private static final String DEFAULT_TIME_DATE_FORMAT = "yyyy'-'MM'-'dd' 'HH':'mm':'ss':'SSS";

    /**
     * Time date format
     */
    private static final String PHOTO_TIME_DATE_FORMAT = "yyyyMMdd_HHmmss";

    /**
     * Private constructor to prevent creating an instance of the class.
     */
    private DateTimeUtils() {
    }

    /**
     * Formats date to the string with default time date format.
     * 
     * @param time
     *            Date to format.
     * 
     * @return result string.
     */
    public static String formatDate(Date time) {
        if (time == null) {
            return null;
        }
        return formatDateWithFormat(time, DEFAULT_TIME_DATE_FORMAT);
    }

    /**
     * Formats date to string with special photo time date format.
     * 
     * @param time
     *            time to format.
     * 
     * @return formatted date.
     */
    public static String getPhotoFormatDate(Date time) {
        if (time == null) {
            return null;
        }
        return formatDateWithFormat(time, PHOTO_TIME_DATE_FORMAT);
    }

    /**
     * Formats date to string with specific format.
     * 
     * @param time
     *            time to format.
     * @param format
     *            format.
     * 
     * @return formatted time.
     */
    private static String formatDateWithFormat(Date time, String format) {
        if (time == null || format == null) {
            return null;
        }
        SimpleDateFormat dateformat = new SimpleDateFormat(format);
        return dateformat.format(time);
    }
}
