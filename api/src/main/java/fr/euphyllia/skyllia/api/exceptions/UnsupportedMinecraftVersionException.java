package fr.euphyllia.skyllia.api.exceptions;

/**
 * This exception is thrown when an operation is attempted on an unsupported version of Minecraft.
 */
public class UnsupportedMinecraftVersionException extends Exception {

    /**
     * Constructs a new UnsupportedMinecraftVersionException with the specified detail message.
     *
     * @param message The detail message.
     */
    public UnsupportedMinecraftVersionException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnsupportedMinecraftVersionException with the specified cause.
     *
     * @param cause The cause of the exception.
     */
    public UnsupportedMinecraftVersionException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new UnsupportedMinecraftVersionException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause of the exception.
     */
    public UnsupportedMinecraftVersionException(String message, Throwable cause) {
        super(message, cause);
    }
}
