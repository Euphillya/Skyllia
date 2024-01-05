package fr.euphyllia.skyfolia.api.exceptions;

public class MaxIslandSizeExceedException extends Exception {

    public MaxIslandSizeExceedException(String message) {
        super(message);
    }

    public MaxIslandSizeExceedException(Throwable cause) {
        super(cause);
    }

    public MaxIslandSizeExceedException(String message, Throwable cause) {
        super(message, cause);
    }
}