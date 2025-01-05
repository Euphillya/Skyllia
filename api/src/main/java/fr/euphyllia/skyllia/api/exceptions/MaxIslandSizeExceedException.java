package fr.euphyllia.skyllia.api.exceptions;

/**
 * This exception is thrown when an operation exceeds the maximum allowed size of an island.
 */
public class MaxIslandSizeExceedException extends Exception {

    /**
     * Constructs a new MaxIslandSizeExceedException with the specified detail message.
     *
     * @param message The detail message.
     */
    public MaxIslandSizeExceedException(String message) {
        super(message);
    }

    /**
     * Constructs a new MaxIslandSizeExceedException with the specified cause.
     *
     * @param cause The cause of the exception.
     */
    public MaxIslandSizeExceedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new MaxIslandSizeExceedException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause of the exception.
     */
    public MaxIslandSizeExceedException(String message, Throwable cause) {
        super(message, cause);
    }
}
