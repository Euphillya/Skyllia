package fr.euphyllia.skyllia.sgbd.utils.stream;

/**
 * The {@code AsciiStream} record represents an ASCII-based input stream along with its length.
 * <p>
 * It is primarily used to handle SQL operations that require ASCII streams,
 * such as inserting or updating TEXT columns.
 *
 * @param x      the ASCII {@link java.io.InputStream} to be processed.
 * @param length the length (in bytes) of the ASCII stream.
 */
public record AsciiStream(java.io.InputStream x, int length) {
}
