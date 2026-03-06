package fr.euphyllia.skyllia.api.permissions;

public final class BitSet64 {

    private long[] words;

    public BitSet64(int bitCount) {
        this.words = new long[Math.max(1, (bitCount + 63) >>> 6)];
    }

    public synchronized boolean get(int bit) {
        int w = bit >>> 6;
        if (w >= words.length) return false;
        return (words[w] & (1L << (bit & 63))) != 0;
    }

    public synchronized void set(int bit, boolean value) {
        ensureCapacity(bit + 1);
        int w = bit >>> 6;
        long m = 1L << (bit & 63);
        if (value) words[w] |= m;
        else words[w] &= ~m;
    }

    public synchronized void ensureCapacity(int bitCount) {
        int needed = (bitCount + 63) >>> 6;
        if (needed <= words.length) return;
        long[] next = new long[needed];
        System.arraycopy(words, 0, next, 0, words.length);
        words = next;
    }

    public synchronized long[] snapshotWords() {
        long[] copy = new long[words.length];
        System.arraycopy(words, 0, copy, 0, words.length);
        return copy;
    }

    public synchronized void loadWords(long[] newWords) {
        this.words = newWords != null ? newWords : new long[0];
    }
}
