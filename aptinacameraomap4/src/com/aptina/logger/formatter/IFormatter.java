package com.aptina.logger.formatter;

/**
 * Common interface for all formatters. Declared common functions.
 */
public interface IFormatter {

    /**
     * Formats exception with specified message to log it.
     * 
     * @param exception
     *            exception.
     * 
     * @param message
     *            friendly message.
     * 
     * @return formatted string.
     */
    public String format(Exception exception, String message);
}
