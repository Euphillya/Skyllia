package fr.euphyllia.skyllia.sgbd.stream;

/**
 * The {@code BinaryStream} record encapsulates a binary {@link java.io.InputStream}
 * along with its length.
 * <p>
 * It is typically used for handling SQL operations involving binary data,
 * such as inserting or retrieving BLOB columns.
 *
 * @param x      the binary {@link java.io.InputStream} to be processed.
 * @param length the length (in bytes) of the binary stream.
 */
public record BinaryStream(java.io.InputStream x, int length) {
}
