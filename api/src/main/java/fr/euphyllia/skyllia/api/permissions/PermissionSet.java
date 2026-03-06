package fr.euphyllia.skyllia.api.permissions;

public final class PermissionSet {

    private final BitSet64 bits;

    public PermissionSet(int bitCount) {
        this.bits = new BitSet64(bitCount);
    }

    public boolean has(PermissionId id) {
        return bits.get(id.index());
    }

    public void set(PermissionId id, boolean value) {
        bits.set(id.index(), value);
    }

    public void ensureCapacity(int bitCount) {
        bits.ensureCapacity(bitCount);
    }

    public long[] snapshotWords() {
        return bits.snapshotWords();
    }

    public void loadWords(long[] newWords) {
        bits.loadWords(newWords);
    }
}