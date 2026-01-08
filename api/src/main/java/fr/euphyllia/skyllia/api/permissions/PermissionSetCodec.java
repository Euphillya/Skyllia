package fr.euphyllia.skyllia.api.permissions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PermissionSetCodec {
    private PermissionSetCodec() {}

    public static byte[] encodeLongs(long[] words) {
        if (words == null) return new byte[0];
        ByteBuffer buf = ByteBuffer.allocate(words.length * Long.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (long w : words) buf.putLong(w);
        return buf.array();
    }

    public static long[] decodeLongs(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return new long[0];
        if ((bytes.length % Long.BYTES) != 0) {
            return new long[0];
        }
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        long[] out = new long[bytes.length / Long.BYTES];
        for (int i = 0; i < out.length; i++) out[i] = buf.getLong();
        return out;
    }
}
