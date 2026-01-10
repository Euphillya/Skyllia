package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.SchematicUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class SchematicSubCommands implements SubCommandInterface {

    private static final Map<UUID, Location> pos1Map = new ConcurrentHashMap<>();
    private static final Map<UUID, Location> pos2Map = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SchematicSubCommands.class);

    /**
     * Handles the execution of a sub-command.
     *
     * @param plugin the {@link Plugin} instance that is executing the command
     * @param sender the {@link CommandSender} who issued the command
     * @param args   the arguments provided with the command
     * @return {@code true} if the command was successfully handled, {@code false} otherwise
     */
    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.schematic")) {
            ConfigLoader.language.sendMessage(sender, "island.player.permission-denied");
            return true;
        }
        if (args.length < 1) {
            ConfigLoader.language.sendMessage(sender, "island.admin.schematic.args-missing");
            return true;
        }
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.admin.schematic.player-only");
            return true;
        }

        if (args[0].equalsIgnoreCase("pos1")) {
            pos1Map.put(player.getUniqueId(), player.getLocation());
            ConfigLoader.language.sendMessage(sender, "island.admin.schematic.pos1-selected");
            return true;
        } else if (args[0].equalsIgnoreCase("pos2")) {
            pos2Map.put(player.getUniqueId(), player.getLocation());
            ConfigLoader.language.sendMessage(sender, "island.admin.schematic.pos2-selected");
            return true;
        } else if (args[0].equalsIgnoreCase("save")) {
            if (!pos1Map.containsKey(player.getUniqueId()) || !pos2Map.containsKey(player.getUniqueId())) {
                ConfigLoader.language.sendMessage(sender, "island.admin.schematic.positions-not-set");
                return true;
            }

            Bukkit.getRegionScheduler().run(plugin, pos1Map.get(player.getUniqueId()), task -> {

                Location pos1 = pos1Map.get(player.getUniqueId());
                Location pos2 = pos2Map.get(player.getUniqueId());
                String schematicName = args[1];

                try {
                    SchematicUtils.createSchematic(sender, pos1, pos2, player.getLocation().toCenterLocation(), schematicName);
                } catch (Exception e) {
                    log.error("Failed to save schematic {}", schematicName, e);
                    ConfigLoader.language.sendMessage(sender, "island.admin.schematic.save-failed");
                }
            });


            return true;
        }
        return false;
    }

    /**
     * Provides tab completion suggestions for the sub-command.
     *
     * @param plugin the {@link Plugin} instance that is executing the command
     * @param sender the {@link CommandSender} who is tab completing
     * @param args   the arguments provided with the command so far
     * @return a {@link List} of suggestions for tab completion, or {@code null} if no suggestions are available
     */
    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0) {
            return List.of("pos1", "pos2", "save");
        }
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Stream.of("pos1", "pos2", "save")
                    .filter(cmd -> cmd.startsWith(prefix))
                    .toList();
        }
        return List.of();
    }
}
