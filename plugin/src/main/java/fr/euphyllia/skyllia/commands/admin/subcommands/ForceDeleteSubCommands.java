package fr.euphyllia.skyllia.commands.admin.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.commands.common.subcommands.DeleteSubCommand;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.WorldEditUtils;
import org.apache.logging.log4j.Level;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForceDeleteSubCommands implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(ForceDeleteSubCommands.class);


    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PermissionImp.hasPermission(sender, "skyllia.admins.commands.island.delete")) {
            ConfigLoader.language.sendMessage(sender, "island.player.permission-denied");
            return true;
        }

        if (args.length < 2) {
            ConfigLoader.language.sendMessage(sender, "admin.delete-args-missing");
            return true;
        }

        String playerName = args[0];
        String confirm = args[1];
        if (!confirm.equalsIgnoreCase("confirm")) {
            ConfigLoader.language.sendMessage(sender, "admin.delete-no-confirm");
            return true;
        }

        try {
            UUID playerId;
            try {
                playerId = UUID.fromString(playerName);
            } catch (IllegalArgumentException ignored) {
                playerId = Bukkit.getPlayerUniqueId(playerName);
            }
            SkyblockManager skyblockManager = Skyllia.getInstance().getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(playerId).join();
            if (island == null) {
                ConfigLoader.language.sendMessage(sender, "island.player.no-island");
                return true;
            }

            skyblockManager.setLockedIsland(island, true).whenComplete((locked, throwable) -> {
                if (throwable != null) {
                    logger.log(Level.FATAL, "Failed to lock island {}: {}", island.getId(), throwable.getMessage());
                    ConfigLoader.language.sendMessage(sender, "island.generic.unexpected-error");
                    return;
                }

                boolean isDisabled = island.setDisable(true);
                if (isDisabled) {
                    this.updatePlayer(skyblockManager, island);

                    AtomicInteger worldsLeft = new AtomicInteger(ConfigLoader.worldManager.getWorldConfigs().size());
                    AtomicBoolean failed = new AtomicBoolean(false);
                    if (worldsLeft.get() == 0) {
                        skyblockManager.setLockedIsland(island, false).whenComplete((value, throwable1) -> {
                            if (throwable1 != null) {
                                logger.log(Level.FATAL, "Failed to unlock/lock island {}: {}", island.getId(), throwable1.getMessage());
                            }
                            ConfigLoader.language.sendMessage(sender, "island.delete-success");
                        });
                        return;
                    } else {
                        ConfigLoader.worldManager.getWorldConfigs().forEach((name, environnements) -> {
                            WorldEditUtils.deleteIsland(Skyllia.getInstance(), island, Bukkit.getWorld(name), (success) -> {
                                if (!success) failed.set(true);
                                if (worldsLeft.decrementAndGet() == 0) {
                                    skyblockManager.setLockedIsland(island, failed.get()).whenComplete((value, throwable1) -> {
                                        if (throwable1 != null) {
                                            logger.log(Level.FATAL, "Failed to unlock/lock island {}: {}", island.getId(), throwable1.getMessage());
                                        }
                                        if (!failed.get()) {
                                            ConfigLoader.language.sendMessage(sender, "island.delete-success");
                                        } else {
                                            ConfigLoader.language.sendMessage(sender, "island.generic.unexpected-error");
                                        }
                                    });
                                }
                            });
                        });
                    }
                }
            });
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(sender, "island.generic.unexpected-error");
        }

        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!PermissionImp.hasPermission(sender, "skyllia.admins.commands.island.delete")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();

            return new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .collect(Collectors.toList());

        } else if (args.length == 2) {
            String partial = args[1].trim().toLowerCase();

            return Stream.of("confirm")
                    .filter(word -> word.startsWith(partial))
                    .toList();
        }

        return Collections.emptyList();
    }

    private void updatePlayer(SkyblockManager skyblockManager, Island island) {
        for (Players players : island.getMembers()) {
            players.setRoleType(RoleType.VISITOR);
            island.updateMember(players);
            DeleteSubCommand.checkClearPlayer(skyblockManager, players, RemovalCause.ISLAND_DELETED);
        }
    }
}
