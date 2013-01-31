package com.aptina.logger.writer;

/**
 * Common interface for all log writers.
 */
public interface IWriter {

    /**
     * Writes string to the log.
     * 
     * @param stringToWrite
     *            string to log.
     */
    public void write(String stringToWrite);

    /**
     * Retrieves all logged entities united to the string.
     * 
     * @return string with logged content.
     */
    public String getContent();
}
