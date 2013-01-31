package com.aptina.logger.formatter;

import java.util.Date;

import com.aptina.miscellaneous.DateTimeUtils;

/**
 * Implements simple formatted to store exception information.
 */
public class SimpleExceptionFormatter implements IFormatter {

    /**
     * Max stack trace deep to log.s
     */
    private final static int MAX_STACK_TRACE_LENGTH = 5;

    /**
     * Line separator.
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public String format(Exception exception, String message) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(formatDate(new Date()));
        stringBuffer.append(LINE_SEPARATOR);

        if (message != null) {
            stringBuffer.append(formatMessage(message));
            stringBuffer.append(LINE_SEPARATOR);
        }

        if (exception != null) {
            String formattedException = formatException(exception);
            if (formattedException != null) {
                stringBuffer.append(formattedException);
            }
        }

        stringBuffer.append(LINE_SEPARATOR + LINE_SEPARATOR);
        return stringBuffer.toString();
    }

    /**
     * Formats date to log.
     * 
     * @param date
     *            date to log.
     * 
     * @return string with formatted date.
     */
    private String formatDate(Date date) {
        if (date == null) {
            date = new Date();
        }

        return DateTimeUtils.formatDate(date);
    }

    /**
     * Formats message.
     * 
     * @param message
     *            message to format.
     * 
     * @return string with formatted message.
     */
    private String formatMessage(String message) {
        return message;
    }

    /**
     * Formats exception.
     * 
     * @param exception
     *            exception to format.
     * 
     * @return string with formatted exception.
     */
    private String formatException(Exception exception) {
        if (exception == null) {
            return null;
        }

        StringBuffer stringBuffer = new StringBuffer();

        try {
            stringBuffer.append(exception.getMessage());
            stringBuffer.append(LINE_SEPARATOR);

            StackTraceElement[] stackTraceElements = exception.getStackTrace();
            if (stackTraceElements != null && stackTraceElements.length > 0) {
                int stackTraceLength = stackTraceElements.length;
                for (int i = 0; i < stackTraceLength; i++) {
                    stringBuffer.append("File name: " + stackTraceElements[i].getFileName() + LINE_SEPARATOR);
                    stringBuffer.append("Method: " + stackTraceElements[i].getMethodName() + LINE_SEPARATOR);
                    stringBuffer.append("Line number: " + stackTraceElements[i].getLineNumber() + LINE_SEPARATOR);
                    if (i >= MAX_STACK_TRACE_LENGTH) {
                        break;
                    }
                }
            }
        } catch (final Exception e) {
            // Do not log this exception.
        }

        return stringBuffer.toString();
    }
}
