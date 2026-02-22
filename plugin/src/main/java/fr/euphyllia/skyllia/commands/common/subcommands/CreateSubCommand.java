package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.cache.commands.CommandCacheExecution;
import fr.euphyllia.skyllia.cache.island.IslandCreationQueue;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.IslandCreationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CreateSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(CreateSubCommand.class);

    public CompletableFuture<Void> runCreateIsland(Skyllia plugin, Player player, String[] args) {
        return CompletableFuture.runAsync(() -> {
            final UUID playerId = player.getUniqueId();

            if (!acquireCommandLock(player, playerId)) return;

            try {
                if (SkylliaAPI.getIslandByPlayerId(playerId) != null) {
                    new HomeSubCommand().onCommand(plugin, player, args);
                    return;
                }

                String schemKey = resolveSchematicKey(player, playerId, args);
                if (schemKey == null) return;

                ConfigLoader.language.sendMessage(player, "island.create-in-progress");

                IslandCreationManager service = new IslandCreationManager(plugin);
                Island result = service.createIslandForPlayer(player, schemKey);
                if (result == null) {
                    fail(player, playerId, "island.create-failed");
                    return;
                }
                ConfigLoader.language.sendMessage(player, "island.create-finish");
            } catch (Exception e) {
                ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
            } finally {
                CommandCacheExecution.removeCommandExec(playerId, "create");
            }
        }, command -> Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> command.run()));
    }


    @Override
    public void onExecute(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return;
        }
        if (IslandCreationQueue.isQueued(player.getUniqueId())) {
            ConfigLoader.language.sendMessage(player, "island.create.already-in-queue");
            return;
        }
        if (!sender.hasPermission("skyllia.island.command.create")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return;
        }

        boolean bypass = ConfigLoader.general.isAllowBypassIslandQueue()
                && player.hasPermission("skyllia.island.bypass.queue");

        if (bypass) {
            runCreateIsland(Skyllia.getInstance(), player, args);
        } else {
            IslandCreationQueue.queuePlayer(player, args);
        }
    }


    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            List<String> nameSchem = ConfigLoader.schematicManager.getIslandTypes();
            if (nameSchem.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> list = new ArrayList<>();
            for (String schem : nameSchem) {
                if (sender.hasPermission("skyllia.island.command.create.%s".formatted(schem))) {
                    if (schem.toLowerCase().startsWith(partial)) {
                        list.add(schem);
                    }
                }
            }
            return list;
        }

        return Collections.emptyList();
    }

    private boolean acquireCommandLock(Player player, UUID playerId) {
        if (CommandCacheExecution.isAlreadyExecute(playerId, "create")) {
            ConfigLoader.language.sendMessage(player, "island.generic.command-in-progress");
            return false;
        }

        CommandCacheExecution.addCommandExecute(playerId, "create");

        if (!player.hasPermission("skyllia.island.command.create")) {
            CommandCacheExecution.removeCommandExec(playerId, "create");
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return false;
        }
        return true;
    }

    private String resolveSchematicKey(Player player, UUID playerId, String[] args) {
        List<String> schematicsKeys = ConfigLoader.schematicManager.getIslandTypes();
        if (schematicsKeys.isEmpty()) {
            fail(player, playerId, "island.schematic-not-exist");
            return null;
        }

        String schemKey = (args.length > 0 && schematicsKeys.contains(args[0]))
                ? args[0]
                : schematicsKeys.getFirst();

        if (!player.hasPermission("skyllia.island.command.create.%s".formatted(schemKey))) {
            fail(player, playerId, "island.player.permission-denied");
            return null;
        }
        return schemKey;
    }

    private void fail(Player player, UUID playerId, String messageKey) {
        CommandCacheExecution.removeCommandExec(playerId, "create");
        ConfigLoader.language.sendMessage(player, messageKey);
    }
}
