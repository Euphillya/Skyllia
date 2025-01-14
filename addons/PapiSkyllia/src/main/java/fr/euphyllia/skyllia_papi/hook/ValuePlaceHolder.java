package fr.euphyllia.skyllia_papi.hook;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.excaliamc.skyllia_value.API.API;
import fr.excaliamc.skyllia_value.Main;
import fr.excaliamc.skyllia_value.database.cache.CacheValue;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

/**
 * Handles placeholders related to the SkylliaValue addon.
 */
public class ValuePlaceHolder {

    /**
     * Formatter for decimal numbers.
     */
    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

    /**
     * Processes value-related placeholders.
     *
     * @param island      the player's island
     * @param playerId    the player's UUID
     * @param placeholder the placeholder to process
     * @return the placeholder value as a string
     */
    public static String processPlaceholder(Island island, UUID playerId, String placeholder) {
        try {
            API svAPI = Main.getPlugin(Main.class).getAPI();
            CacheValue cacheValue = svAPI.getCacheValue();

            UUID islandId = island.getId();
            double value = cacheValue.getValueIslandBySkyblockId(islandId);

            Method getConfigMethod = svAPI.getClass().getMethod("getConfig");

            Object config = getConfigMethod.invoke(svAPI);

            Method getEquationSimulationLevelMethod = config.getClass().getMethod("getEquationSimulationLevel");

            String equation = (String) getEquationSimulationLevelMethod.invoke(config);

            String placeholderLower = placeholder.toLowerCase(Locale.ROOT);
            return switch (placeholderLower) {
                case "value_experience" -> df.format(value);
                case "value_level" -> {
                    double expression = new ExpressionBuilder(equation).build().evaluate();
                    yield String.valueOf((int) value / expression);
                }
                default -> "-1";
            };
        } catch (Exception e) {
            return "-1";
        }
    }
}
