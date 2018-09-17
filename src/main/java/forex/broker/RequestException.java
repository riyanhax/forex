package forex.broker;

public class RequestException extends Exception {

    public RequestException(String message) {
        this(message, null);
    }

    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
