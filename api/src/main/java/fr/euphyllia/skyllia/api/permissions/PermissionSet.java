package fr.euphyllia.skyllia.api.permissions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PermissionSet {
    private static final Logger log = LoggerFactory.getLogger(PermissionSet.class);
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
        if (value) {
            words[w] |= m;
        } else {
            words[w] &= ~m;
        }
    }

    public synchronized void ensureCapacity(int bitCount) {
        int needed = (bitCount + 63) >>> 6;
        if (needed <= words.length) return;
        long[] newArr = new long[needed];
        System.arraycopy(words, 0, newArr, 0, words.length);
        words = newArr;
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