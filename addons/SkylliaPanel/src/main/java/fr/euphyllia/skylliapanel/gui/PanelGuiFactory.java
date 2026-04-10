package fr.euphyllia.skylliapanel.gui;

import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skylliapanel.SkylliaPanelPlugin;
import fr.euphyllia.skylliapanel.configuration.GuiDefinition;
import fr.euphyllia.skylliapanel.gui.dynamic.DynamicPanelGui;
import org.bukkit.entity.Player;

public class PanelGuiFactory {

    public static void open(Player player, String fileName) {
        int page = 1;
        RoleType role = null;
        String cleanName = fileName;

        int queryIndex = fileName.indexOf('?');
        if (queryIndex >= 0) {
            String query = fileName.substring(queryIndex + 1);
            cleanName = fileName.substring(0, queryIndex);
            for (String param : query.split("&")) {
                if (param.startsWith("page=")) {
                    try {
                        page = Integer.parseInt(param.substring(5));
                    } catch (NumberFormatException ignored) {
                    }
                } else if (param.startsWith("role=")) {
                    try {
                        role = RoleType.valueOf(param.substring(5).toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        SkylliaPanelPlugin plugin = SkylliaPanelPlugin.getInstance();

        GuiDefinition def = null; // Todo recherche panel
        if (def != null) {
            if (def.isDynamic()) {
                new DynamicPanelGui(def, page).open(player);
            } else {
                new PanelGui(def).open(player);
            }
            return;
        }

        // Todo send message : gui not found
    }
}
