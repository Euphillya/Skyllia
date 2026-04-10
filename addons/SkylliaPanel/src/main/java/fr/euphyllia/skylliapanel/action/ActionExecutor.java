package fr.euphyllia.skylliapanel.action;

import fr.euphyllia.skylliapanel.SkylliaPanelPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ActionExecutor {

    private static final Logger log = LoggerFactory.getLogger(ActionExecutor.class);
    private static SkylliaPanelPlugin plugin = SkylliaPanelPlugin.getInstance();

    private ActionExecutor() {}

    public static void execute(Player player, List<ActionDefinition> actions) {
        if (actions == null || actions.isEmpty()) return;

        for (ActionDefinition action : actions) {
            try {
                executeOne(player, action);
            } catch (Exception e) {
                log.error("Error executing action {} for player {}: {}", action.type(), player.getName(), e.getMessage());
            }
        }
    }

    private static void executeOne(Player player, ActionDefinition action) {
        switch (action.type()) {
            case OPEN_GUI -> {
                String fileName = action.value();
                if (fileName.isBlank()) {
                    log.warn("Action OPEN_GUI requires a file name");
                    return;
                }
                if (Bukkit.isOwnedByCurrentRegion(player)) {
                    // Open Inventory
                } else {
                    player.getScheduler().run(plugin, scheduledTask -> {
                        // Open Inventory
                    }, null);
                }
            }
            case COMMAND_PLAYER -> {
                String cmd = resolvePlaceholders(player, action.value());
                if (Bukkit.isOwnedByCurrentRegion(player)) {
                    player.performCommand(cmd);
                } else {
                    player.getScheduler().run(plugin, scheduledTask -> {
                        player.performCommand(cmd);
                    }, null);
                }
            }
            case COMMAND_SERVER -> {
                String cmd = resolvePlaceholders(player, action.value());
                Bukkit.getGlobalRegionScheduler().execute(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            }
            case CLOSE -> {
                if (Bukkit.isOwnedByCurrentRegion(player)) {
                    player.closeInventory();
                } else {
                    player.getScheduler().run(plugin, scheduledTask -> {
                        player.closeInventory();
                    }, null);
                }
            }
            default -> log.warn("Unknown action type: {}", action.type());
        }
    }

    private static String resolvePlaceholders(Player player, String input) {
        return PlaceholderAPI.setPlaceholders(player, input);
    }
}
