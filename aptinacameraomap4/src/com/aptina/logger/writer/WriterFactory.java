package com.aptina.logger.writer;

/**
 * Implements Factory pattern to return specified writer for the Logger.
 */
public class WriterFactory {

    /**
     * Private constructor to prevent creating an instance of the class.
     */
    private WriterFactory() {
    }

    /**
     * Retrieves more appropriated writer.
     * 
     * @return writer.
     */
    public static IWriter getWriter() {
        if (FileWriter.hasAccessToSdCard()) {
            return FileWriter.getInstance();
        }

        return new LogCatWritter();
    }
}
