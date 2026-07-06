package fr.euphyllia.skyllia.api.skyblock.model;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum IslandWeatherType {

    CLEAR,

    RAIN,

    THUNDER;

    /**
     * Parses an {@link IslandWeatherType} from a string, ignoring case.
     *
     * @param value the raw value (e.g. "rain", "THUNDER", "clear")
     * @return the corresponding {@link IslandWeatherType}, or {@link IslandWeatherType#CLEAR} if the value is null or invalid
     */
    public static IslandWeatherType fromString(@Nullable String value) {
        if (value == null) return IslandWeatherType.CLEAR;
        try {
            return IslandWeatherType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return IslandWeatherType.CLEAR;
        }
    }
}
