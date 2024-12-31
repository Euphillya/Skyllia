package fr.euphyllia.skylliainfo.hook;

import fr.euphyllia.skylliaore.api.Generator;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SkylliaOreHook {

    public static void sendMessage(MiniMessage miniMessage, Player player, UUID islandId) {
        Generator generator = fr.euphyllia.skylliaore.Main.getCache().getGeneratorIsland(islandId);
        player.sendMessage(miniMessage.deserialize(
                "<yellow>Generator Types: </yellow><white>" + generator.name() + "</white>"));
    }
}
