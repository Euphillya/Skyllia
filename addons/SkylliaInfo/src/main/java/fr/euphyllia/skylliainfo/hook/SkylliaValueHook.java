package fr.euphyllia.skylliainfo.hook;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class SkylliaValueHook {
    public static void sendMessage(MiniMessage miniMessage, Player player, UUID islandId) {
        try {
            Class<?> mainClass = Class.forName("fr.excaliamc.skyllia_value.Main");

            Method getPluginMethod = mainClass.getMethod("getPlugin", Class.class);

            Object mainInstance = getPluginMethod.invoke(null, mainClass);

            Method getAPIMethod = mainClass.getMethod("getAPI");

            Object apiInstance = getAPIMethod.invoke(mainInstance);

            Class<?> apiClass = Class.forName("fr.excaliamc.skyllia_value.api.API");

            Method getCacheValueMethod = apiClass.getMethod("getCacheValue");

            Object cacheValue = getCacheValueMethod.invoke(apiInstance);

            Class<?> cacheValueClass = Class.forName("fr.excaliamc.skyllia_value.database.cache.CacheValue");

            Method getValueIslandMethod = cacheValueClass.getMethod("getValueIslandBySkyblockId", UUID.class);

            Object islandValue = getValueIslandMethod.invoke(cacheValue, islandId);

            player.sendMessage(miniMessage.deserialize(
                    "<yellow>Island Value: </yellow><white>" + islandValue + "</white>"));

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
