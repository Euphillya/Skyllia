package fr.euphyllia.skyllia.api.exceptions;

public class UnsupportedMinecraftVersionException extends Exception {

    public UnsupportedMinecraftVersionException(String message) {
        super(message);
    }

    public UnsupportedMinecraftVersionException(Throwable cause) {
        super(cause);
    }

    public UnsupportedMinecraftVersionException(String message, Throwable cause) {
        super(message, cause);
    }
}