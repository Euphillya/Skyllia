package fr.euphyllia.skyllia.api.permissions;

public final class IslandFlags {

    private final BitSet64 bits;
    private int version;

    public IslandFlags(IslandFlagRegistry registry) {
        this.bits = new BitSet64(registry.size());
        this.version = registry.version();
    }

    /**
     * Ensures the internal bitset is large enough to hold all registered flags.
     * Called automatically by {@link #has} and {@link #set}.
     */
    public void ensureUpToDate(IslandFlagRegistry registry) {
        int regVersion = registry.version();
        if (this.version == regVersion) return;
        synchronized (this) {
            if (this.version == regVersion) return;
            bits.ensureCapacity(registry.size());
            this.version = regVersion;
        }
    }

    /**
     * Returns {@code true} if the flag is enabled on this island.
     */
    public boolean has(IslandFlagRegistry registry, FlagId id) {
        ensureUpToDate(registry);
        return bits.get(id.index());
    }

    /**
     * Enables or disables a flag on this island.
     */
    public void set(IslandFlagRegistry registry, FlagId id, boolean value) {
        ensureUpToDate(registry);
        bits.set(id.index(), value);
    }

    /**
     * Raw snapshot for persistence.
     */
    public long[] snapshotWords() {
        return bits.snapshotWords();
    }

    /**
     * Restore from persistence.
     */
    public void loadWords(long[] words) {
        bits.loadWords(words);
    }
}
