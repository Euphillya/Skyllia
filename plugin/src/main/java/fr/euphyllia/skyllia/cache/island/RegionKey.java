package fr.euphyllia.skyllia.cache.island;

public final class RegionKey {
    public static long of(int regionX, int regionZ) {
        return (((long) regionX) << 32) ^ (regionZ & 0xffffffffL);
    }
}
