package fr.euphyllia.skylliapanel.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import fr.euphyllia.skylliapanel.SkylliaPanelPlugin;
import fr.euphyllia.skylliapanel.configuration.ButtonDefinition;
import fr.euphyllia.skylliapanel.configuration.GuiDefinition;
import fr.euphyllia.skylliapanel.util.ItemFactory;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PanelGui {

    private static final Logger log = LoggerFactory.getLogger(PanelGui.class);
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final GuiDefinition definition;

    public PanelGui(GuiDefinition definition) {
        this.definition = definition;
    }

    public void open(Player player) {
        String resolvedTitle = PlaceholderAPI.setPlaceholders(player, definition.title());

        Gui gui = Gui.gui()
                .title(miniMessage.deserialize(resolvedTitle))
                .rows(definition.rows())
                .disableAllInteractions()
                .create();

        for (ButtonDefinition btn : definition.buttons()) {
            try {
                GuiItem item = ItemFactory.buildGuiItem(player, btn);
                gui.setItem(btn.slot(), item);
            } catch (Exception e) {
                log.warn("Could not build gui item for button {}", btn.name(), e);
            }
        }

        player.getScheduler().run(
                SkylliaPanelPlugin.getInstance(),
                task -> gui.open(player),
                null
        );
    }
}
