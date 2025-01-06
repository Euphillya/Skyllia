package fr.euphyllia.skylliainfo.hook;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SkylliaValueHook {

    public static void sendMessage(MiniMessage miniMessage, Player player, UUID islandId) {
        fr.excaliamc.skyllia_value.database.cache.CacheValue cacheValue = fr.excaliamc.skyllia_value.Main.getPlugin(fr.excaliamc.skyllia_value.Main.class)
                .getAPI().getCacheValue();

        player.sendMessage(miniMessage.deserialize(
                "<yellow>Island Value: </yellow><white>" + cacheValue.getValueIslandBySkyblockId(islandId) + "</white>"));
    }
}
