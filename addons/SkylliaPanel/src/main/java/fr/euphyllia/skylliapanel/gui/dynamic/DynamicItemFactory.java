package fr.euphyllia.skylliapanel.gui.dynamic;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import fr.euphyllia.skylliapanel.action.ActionDefinition;
import fr.euphyllia.skylliapanel.action.ActionExecutor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DynamicItemFactory {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final Logger log = LoggerFactory.getLogger(DynamicItemFactory.class);

    private DynamicItemFactory() {
    }

    public static GuiItem build(Player viewer, DynamicItemDefinition def, DynamicContext context) {
        ItemBuilder builder = buildItemBuilder(viewer, def, context);

        return builder.asGuiItem(event -> {
            event.setCancelled(true);
            ClickType click = event.getClick();

            List<ActionDefinition> actions;
            if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) {
                actions = def.leftClickActions();
            } else if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
                actions = def.rightClickActions();
            } else {
                actions = def.leftClickActions(); // fallback
            }

            // Résolution des placeholders dynamiques dans les valeurs d'action
            List<ActionDefinition> resolvedActions = actions.stream()
                    .map(a -> new ActionDefinition(a.type(), context.resolve(a.value())))
                    .toList();

            ActionExecutor.execute(viewer, resolvedActions);
        });
    }

    private static ItemBuilder buildItemBuilder(Player viewer, DynamicItemDefinition def, DynamicContext context) {

        String material = def.material();
        Material mat = Material.matchMaterial(material);
        if (mat == null) {
            mat = Material.STONE; // default fallback
            log.warn("Invalid material '{}' for dynamic item, defaulting to STONE", material);
        }

        ItemStack itemStack = new ItemStack(mat);
        itemStack.editMeta(itemMeta -> {
            if (!def.name().isBlank()) {
                String resolved = resolve(viewer, def.name(), context);
                itemMeta.itemName(miniMessage.deserialize(resolved));
            }
            if (!def.lore().isEmpty()) {
                List<Component> loreComponents = def.lore().stream()
                        .map(line -> miniMessage.deserialize(resolve(viewer, line, context)))
                        .toList();
                itemMeta.lore(loreComponents);
            }
            if (def.glow()) {
                itemMeta.addEnchant(Enchantment.LURE, 1, false);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            if (def.customModelData() > 0) {
                itemMeta.setCustomModelData(def.customModelData());
            }
            if (def.itemModel() != null) {
                itemMeta.setItemModel(NamespacedKey.fromString(def.itemModel()));
            }
        });
        return ItemBuilder.from(itemStack);
    }

    private static String resolve(Player viewer, String input, DynamicContext context) {
        String step1 = context.resolve(input);
        return PlaceholderAPI.setPlaceholders(viewer, step1);
    }


}
