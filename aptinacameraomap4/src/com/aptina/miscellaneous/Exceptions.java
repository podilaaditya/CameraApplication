package com.aptina.miscellaneous;

public class Exceptions {
	public static class NotParabolaException extends Exception {
	    private static final long serialVersionUID = 1L;
	    public Exception innerException;

	    public NotParabolaException() {
	    }

	    public NotParabolaException(Exception innerException) {
	        this.innerException = innerException;
	    }
	}

}
