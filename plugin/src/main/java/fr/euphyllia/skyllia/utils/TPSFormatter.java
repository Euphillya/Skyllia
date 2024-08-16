package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class TPSFormatter {

    private static @NotNull String formatTPS(double tps) {
        String color = getGradientColor(tps);
        return color + String.format("%.2f", tps);
    }

    private static String getGradientColor(double tps) {
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

    public static Component displayTPS(double[] tps) {
        String timeLabels;
        String tpsValues;
        if (SkylliaAPI.isFolia()) {
            timeLabels = "<white>TPS Times: <gray>[5s, 15s, 1m, 5m, 15m]";
            tpsValues = String.format("TPS Values: %s, %s, %s, %s, %s",
                    formatTPS(tps[0]), formatTPS(tps[1]), formatTPS(tps[2]), formatTPS(tps[3]), formatTPS(tps[4]));
        } else {
            timeLabels = "<white>TPS Times: <gray>[1m, 5m, 15m]";
            tpsValues = String.format("TPS Values: %s, %s, %s",
                    formatTPS(tps[0]), formatTPS(tps[1]), formatTPS(tps[2]));
        }
        return Main.getPlugin(Main.class).getInterneAPI().getMiniMessage().deserialize(timeLabels + "\n" + tpsValues);
    }
}
