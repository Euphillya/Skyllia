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

    public static @NotNull String coloredMSPT(double mspt) {
        // Determine the color prefix based on the MSPT thresholds
        String colorCode = getMSPTColorCode(mspt);
        // Format the MSPT with two decimals and prepend the color code
        return colorCode + String.format("%.2f", mspt);
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

    private static String getMSPTColorCode(double mspt) {
        if (mspt <= 20) {
            return "<aqua>";
        } else if (mspt <= 40) {
            return "<green>";
        } else if (mspt <= 50) {
            return "<yellow>";
        } else if (mspt <= 70) {
            return "<gold>";
        } else if (mspt <= 90) {
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
    public static Component displayTPS(double[] tps, double[] mspt) {
        String timeLabel;
        String tpsValues;
        String msptLabel;
        String msptValues;

        // Adapt labels and number of TPS values based on Folia or not
        if (SkylliaAPI.isFolia()) {
            // Expecting 5 TPS values: tps[0..4]
            timeLabel = "<gray>TPS from last 5s, 15s, 1m, 5m, 15m:";
            tpsValues = String.format(
                    " %s<gray>, %s<gray>, %s<gray>, %s<gray>, %s",
                    coloredTPS(tps[0]),
                    coloredTPS(tps[1]),
                    coloredTPS(tps[2]),
                    coloredTPS(tps[3]),
                    coloredTPS(tps[4])
            );
            msptLabel = "<gray>Tick durations from last 5s, 15s, 1m, 5m, 15m:";
            msptValues = String.format(
                    " %s<gray>, %s<gray>, %s<gray>, %s<gray>, %s",
                    coloredMSPT(mspt[0]),
                    coloredMSPT(mspt[1]),
                    coloredMSPT(mspt[2]),
                    coloredMSPT(mspt[3]),
                    coloredMSPT(mspt[4])
            );

        } else {
            // Expecting 3 TPS values: tps[0..2]
            timeLabel = "<gray> TPS from last 1m, 5m, 15m:";
            tpsValues = String.format(
                    " %s<gray>, %s<gray>, %s<gray>",
                    coloredTPS(tps[0]),
                    coloredTPS(tps[1]),
                    coloredTPS(tps[2])
            );
            msptLabel = "<gray> Tick durations from last 1m, 5m, 15m:";
            msptValues = String.format(
                    " %s<gray>, %s<gray>, %s<gray>",
                    coloredMSPT(mspt[0]),
                    coloredMSPT(mspt[1]),
                    coloredMSPT(mspt[2])
            );
        }

        // Combine both strings with a newline
        String message = timeLabel + "\n" + tpsValues + "\n\n" + msptLabel + "\n" + msptValues;

        // Deserialize the MiniMessage string into a Component
        return MiniMessage.miniMessage().deserialize(message);
    }
}
