package fr.euphyllia.skyllia_papi.hook;

import fr.euphyllia.skyllia.api.skyblock.Island;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
            Class<?> mainClass = Class.forName("fr.excaliamc.skyllia_value.Main");

            Method getPluginMethod = mainClass.getMethod("getPlugin", Class.class);

            Object pluginInstance = getPluginMethod.invoke(null, mainClass);

            Method getAPIMethod = mainClass.getMethod("getAPI");

            Object svAPI = getAPIMethod.invoke(pluginInstance);

            Method getCacheValueMethod = svAPI.getClass().getMethod("getCacheValue");

            Object cacheValue = getCacheValueMethod.invoke(svAPI);

            Method getValueIslandBySkyblockIdMethod = cacheValue.getClass().getMethod("getValueIslandBySkyblockId", String.class);

            Method getIdMethod = island.getClass().getMethod("getId");
            String islandId = (String) getIdMethod.invoke(island);
            double value = (Double) getValueIslandBySkyblockIdMethod.invoke(cacheValue, islandId);

            Method getConfigMethod = svAPI.getClass().getMethod("getConfig");

            Object config = getConfigMethod.invoke(svAPI);

            Method getEquationSimulationLevelMethod = config.getClass().getMethod("getEquationSimulationLevel");

            String equation = (String) getEquationSimulationLevelMethod.invoke(config);

            Class<?> expressionBuilderClass = Class.forName("net.objecthunter.exp4j.ExpressionBuilder");
            double expressionValue = getExpressionValue(expressionBuilderClass, equation);

            String placeholderLower = placeholder.toLowerCase(Locale.ROOT);
            return switch (placeholderLower) {
                case "value_experience" -> df.format(value);
                case "value_level" -> String.valueOf(value / expressionValue);
                default -> "-1";
            };
        } catch (Exception e) {
            return "-1";
        }
    }

    /**
     * Evaluates the mathematical expression using reflection.
     *
     * @param expressionBuilderClass the ExpressionBuilder class
     * @param equation               the equation string
     * @return the evaluated result as a double
     * @throws NoSuchMethodException     if a method is not found
     * @throws InstantiationException    if instantiation fails
     * @throws IllegalAccessException    if access is denied
     * @throws InvocationTargetException if the method invocation fails
     */
    private static Double getExpressionValue(Class<?> expressionBuilderClass, String equation) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?> expressionBuilderConstructor = expressionBuilderClass.getConstructor(String.class);
        Object expressionBuilderInstance = expressionBuilderConstructor.newInstance(equation);

        Method buildMethod = expressionBuilderClass.getMethod("build");
        Object expression = buildMethod.invoke(expressionBuilderInstance);

        Method evaluateMethod = expression.getClass().getMethod("evaluate");
        return (Double) evaluateMethod.invoke(expression);
    }
}
