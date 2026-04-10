package fr.euphyllia.skylliapanel.util;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import fr.euphyllia.skylliapanel.action.ActionExecutor;
import fr.euphyllia.skylliapanel.configuration.ButtonDefinition;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemFactory {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final Logger log = LoggerFactory.getLogger(ItemFactory.class);

    private ItemFactory() {}

    public static GuiItem buildGuiItem(Player player, ButtonDefinition btn) {
        ItemBuilder builder = buildItemBuilder(player, btn);

        return builder.asGuiItem(event -> {
            event.setCancelled(true);
            ActionExecutor.execute(player, btn.actions());
        });
    }

    public static ItemBuilder buildItemBuilder(Player player, ButtonDefinition btn) {
        ItemBuilder builder;

        Material material = resolveMaterial(btn.material());
        builder = ItemBuilder.from(material);

        // Todo : Need more settings
        return builder;
    }

    private static Material resolveMaterial(String name) {
        Material mat = Material.matchMaterial(name);
        if (mat == null) {
            log.warn("Invalid material '{}' for button, defaulting to STONE", name);
            return Material.STONE;
        }
        return mat;
    }
}
