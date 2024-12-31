package fr.euphyllia.skyllia.api.utils;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for formatting TPS (Ticks Per Second) values into colored strings and displaying them.
 */
public class TPSFormatter {

    /**
     * Formats the given TPS value into a colored string based on predefined thresholds.
     *
     * @param tps The TPS value to format.
     * @return A string with color codes (MiniMessage syntax) and the formatted TPS value.
     */
    private static @NotNull String coloredTPS(double tps) {
        // Determine the color prefix based on the TPS thresholds
        String colorCode = getColorCode(tps);
        // Format the TPS with two decimals and prepend the color code
        return colorCode + String.format("%.2f", tps);
    }

    /**
     * Returns a color code in MiniMessage format based on TPS thresholds.
     *
     * @param tps The TPS value.
     * @return A MiniMessage color tag, e.g., {@code <green>}.
     */
    private static String getColorCode(double tps) {
        if (tps >= 19.5) {
            return "<aqua>";
        } else if (tps >= 18.0) {
            return "<green>";
        } else if (tps >= 16.0) {
            return "<yellow>";
        } else if (tps >= 13.0) {
            return "<gold>";
        } else if (tps >= 10.0) {
            return "<red>";
        } else {
            return "<dark_red>";
        }
    }

    /**
     * Creates a {@link Component} showing the TPS times and values in colored format.
     * <p>
     * If Folia is detected (via {@link SkylliaAPI#isFolia()}), it displays:
     * <ul>
     *   <li>5s, 15s, 1m, 5m, 15m (5 values)</li>
     * </ul>
     * Otherwise:
     * <ul>
     *   <li>1m, 5m, 15m (3 values)</li>
     * </ul>
     *
     * @param tps An array of TPS values. For Folia, it should contain 5 values; otherwise 3.
     * @return A formatted {@link Component} with line breaks.
     */
    public static Component displayTPS(double[] tps) {
        String timeLabel;
        String tpsValues;

        // Adapt labels and number of TPS values based on Folia or not
        if (SkylliaAPI.isFolia()) {
            // Expecting 5 TPS values: tps[0..4]
            timeLabel = "<white>TPS Times: <gray>[5s, 15s, 1m, 5m, 15m]";
            tpsValues = String.format(
                    "TPS Values: %s, %s, %s, %s, %s",
                    coloredTPS(tps[0]),
                    coloredTPS(tps[1]),
                    coloredTPS(tps[2]),
                    coloredTPS(tps[3]),
                    coloredTPS(tps[4])
            );
        } else {
            // Expecting 3 TPS values: tps[0..2]
            timeLabel = "<white>TPS Times: <gray>[1m, 5m, 15m]";
            tpsValues = String.format(
                    "TPS Values: %s, %s, %s",
                    coloredTPS(tps[0]),
                    coloredTPS(tps[1]),
                    coloredTPS(tps[2])
            );
        }

        // Combine both strings with a newline
        String message = timeLabel + "\n" + tpsValues;

        // Deserialize the MiniMessage string into a Component
        return MiniMessage.miniMessage().deserialize(message);
    }
}
