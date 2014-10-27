package test;

/*
 * Exception class to enclose other and prevent exposing details to the client
 */

public class ClientException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
