package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.HeightType;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Admin sub-command: /isadmin setheight <player> <min|max> <value> confirm
 * <p>
 * Sets a per-island, per-world build height limit (min or max) for the island
 * belonging to the target player.
 * <p>
 * The value is silently clamped to the world's actual height bounds:
 * <ul>
 *   <li>MIN is clamped to [{@code world.getMinHeight()}, current max]</li>
 *   <li>MAX is clamped to [current min, {@code world.getMaxHeight()}]</li>
 * </ul>
 * If the value already equals or exceeds the world default it is ignored with a warning.
 */
public class SetHeightSubCommands implements SubCommandInterface {

    private static final Logger logger = LogManager.getLogger(SetHeightSubCommands.class);

    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.setheight")) {
            ConfigLoader.language.sendMessage(sender, "island.player.permission-denied");
            return;
        }

        // /isadmin setheight <player> <min|max> <value> confirm
        if (args.length < 4) {
            ConfigLoader.language.sendMessage(sender, "island.admin.height.args-missing");
            return;
        }

        String playerName = args[0];
        String typeArg = args[1].toLowerCase();
        String valueArg = args[2];
        String confirmArg = args[3];

        if (!confirmArg.equalsIgnoreCase("confirm")) {
            ConfigLoader.language.sendMessage(sender, "island.admin.height.args-missing");
            return;
        }

        HeightType heightType;
        if (typeArg.equals("min")) {
            heightType = HeightType.MIN;
        } else if (typeArg.equals("max")) {
            heightType = HeightType.MAX;
        } else {
            ConfigLoader.language.sendMessage(sender, "island.admin.height.invalid-type");
            return;
        }

        int requestedValue;
        try {
            requestedValue = Integer.parseInt(valueArg);
        } catch (NumberFormatException e) {
            ConfigLoader.language.sendMessage(sender, "island.admin.height.not-an-number");
            return;
        }

        try {
            // Resolve target player UUID
            UUID playerId;
            try {
                playerId = UUID.fromString(playerName);
            } catch (IllegalArgumentException ignored) {
                playerId = Bukkit.getPlayerUniqueId(playerName);
            }

            if (playerId == null) {
                ConfigLoader.language.sendMessage(sender, "island.player.player-not-found");
                return;
            }

            SkyblockManager skyblockManager = Skyllia.getInstance().getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(playerId);

            if (island == null) {
                ConfigLoader.language.sendMessage(sender, "island.player.no-island");
                return;
            }

            // Determine which world to apply the limit to.
            // If the sender is a player in a skyblock world we use that world,
            // otherwise we fall back to the first configured skyblock world.
            World targetWorld = resolveWorld(sender);
            if (targetWorld == null) {
                ConfigLoader.language.sendMessage(sender, "island.admin.height.no-world");
                return;
            }

            String worldName = targetWorld.getName();
            int worldMin = targetWorld.getMinHeight();
            int worldMax = targetWorld.getMaxHeight();

            // Clamp and validate
            int clampedValue;
            if (heightType == HeightType.MIN) {
                if (requestedValue <= worldMin) {
                    // Value at or below world floor — treat as "reset to default" (use world default)
                    ConfigLoader.language.sendMessage(sender, "island.admin.height.below-world-min");
                    return;
                }
                if (requestedValue >= worldMax) {
                    // Would be equal/above ceiling — clamp silently to worldMax - 1
                    clampedValue = worldMax - 1;
                    logger.warn("Requested min height {} for island {} exceeds world max {}; clamped to {}",
                            requestedValue, island.getId(), worldMax, clampedValue);
                } else {
                    clampedValue = requestedValue;
                }
            } else {
                if (requestedValue >= worldMax) {
                    // Value at or above world ceiling — treat as "reset to default"
                    ConfigLoader.language.sendMessage(sender, "island.admin.height.above-world-max");
                    return;
                }
                if (requestedValue <= worldMin) {
                    // Would be at/below floor — clamp silently to worldMin + 1
                    clampedValue = worldMin + 1;
                    logger.warn("Requested max height {} for island {} is below world min {}; clamped to {}",
                            requestedValue, island.getId(), worldMin, clampedValue);
                } else {
                    clampedValue = requestedValue;
                }
            }

            boolean updated = island.setBuildHeight(worldName, heightType, clampedValue);
            if (updated) {
                ConfigLoader.language.sendMessage(sender, "island.admin.height.success");
            } else {
                ConfigLoader.language.sendMessage(sender, "island.admin.height.failed");
            }

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(sender, "island.generic.unexpected-error");
        }
    }

    /**
     * Resolves the world to apply the height limit to.
     * <p>
     * Uses the sender's current world if they are a player in a skyblock world,
     * otherwise returns the first skyblock world found in the world configuration.
     */
    private World resolveWorld(CommandSender sender) {
        if (sender instanceof Player player) {
            World w = player.getWorld();
            if (ConfigLoader.worldManager.getWorldConfig(w.getName()) != null) {
                return w;
            }
        }
        for (WorldConfig wc : ConfigLoader.worldManager.getWorldConfigs().values()) {
            World w = Bukkit.getWorld(wc.getWorldName());
            if (w != null) return w;
        }
        return null;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("skyllia.admins.commands.island.setheight")) {
            return Collections.emptyList();
        }

        // ARG #1 : player name
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ARG #2 : min | max
        if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();
            return Stream.of("min", "max")
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }

        // ARG #3 : example height values
        if (args.length == 3) {
            String partial = args[2].trim();
            return Stream.of("-64", "0", "64", "128", "256", "320")
                    .filter(v -> v.startsWith(partial))
                    .collect(Collectors.toList());
        }

        // ARG #4 : confirm
        if (args.length == 4) {
            String partial = args[3].trim().toLowerCase();
            return Stream.of("confirm")
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
