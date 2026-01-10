package fr.euphyllia.skyllia.api.utils;

import java.util.concurrent.TimeUnit;

public final class ExpiringValue<T> {
    private final T value;
    private final long expiresAtNanos;

    private ExpiringValue(T value, long ttlNanos) {
        this.value = value;
        this.expiresAtNanos = System.nanoTime() + ttlNanos;
    }

    public static <T> ExpiringValue<T> of(T value, long ttl, TimeUnit unit) {
        return new ExpiringValue<>(value, unit.toNanos(ttl));
    }

    public boolean isExpired() {
        return System.nanoTime() >= expiresAtNanos;
    }

    public T get() {
        return value;
    }
}
