package fr.euphyllia.skyllia.api.permissions;

public final class PermissionSet {
    private long[] words;

    public PermissionSet(int bitCount) {
        this.words = new long[(bitCount + 63) >>> 6];
    }

    public synchronized boolean has(PermissionId id) {
        int bit = id.index();
        int w = bit >>> 6;
        if (w >= words.length) return false;
        return (words[w] & (1L << (bit & 63))) != 0;
    }

    public synchronized void set(PermissionId id, boolean value) {
        int bit = id.index();
        ensureCapacity(bit + 1);
        int w = bit >>> 6;
        long m = 1L << (bit & 63);
        if (value) words[w] |= m;
        else words[w] &= ~m;
    }

    public synchronized void ensureCapacity(int bitCount) {
        int needed = (bitCount + 63) >>> 6;
        if (needed <= words.length) return;
        long[] newArr = new long[needed];
        System.arraycopy(words, 0, newArr, 0, words.length);
        words = newArr;
    }
}