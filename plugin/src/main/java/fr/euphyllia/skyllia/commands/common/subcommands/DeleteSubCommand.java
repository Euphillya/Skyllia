package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DeleteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(DeleteSubCommand.class);

    public static void checkClearPlayer(SkyblockManager skyblockManager, Players players, RemovalCause cause) {
        Player bPlayer = Bukkit.getPlayer(players.getMojangId());
        if (bPlayer != null && bPlayer.isOnline()) {
            PlayerUtils.teleportPlayerSpawn(bPlayer);
            bPlayer.getScheduler().execute(Skyllia.getInstance(), () -> {
                switch (cause) {
                    case KICKED -> {
                        if (ConfigLoader.playerManager.isClearInventoryWhenKicked()) {
                            bPlayer.getInventory().clear();
                        }
                        if (ConfigLoader.playerManager.isClearEnderChestWhenKicked()) {
                            bPlayer.getEnderChest().clear();
                        }
                        if (ConfigLoader.playerManager.isResetExperienceWhenKicked()) {
                            bPlayer.setTotalExperience(0);
                            bPlayer.setExp(0);
                            bPlayer.setLevel(0);
                            bPlayer.sendExperienceChange(0, 0); // Mise à jour du packet
                        }
                    }
                    case ISLAND_DELETED -> {
                        if (ConfigLoader.playerManager.isClearInventoryWhenDelete()) {
                            bPlayer.getInventory().clear();
                        }
                        if (ConfigLoader.playerManager.isClearEnderChestWhenDelete()) {
                            bPlayer.getEnderChest().clear();
                        }
                        if (ConfigLoader.playerManager.isResetExperienceWhenDelete()) {
                            bPlayer.setTotalExperience(0);
                            bPlayer.setExp(0);
                            bPlayer.setLevel(0);
                            bPlayer.sendExperienceChange(0, 0); // Mise à jour du packet
                        }
                    }
                    case LEAVE -> {
                        if (ConfigLoader.playerManager.isClearInventoryWhenLeave()) {
                            bPlayer.getInventory().clear();
                        }
                        if (ConfigLoader.playerManager.isClearEnderChestWhenLeave()) {
                            bPlayer.getEnderChest().clear();
                        }
                        if (ConfigLoader.playerManager.isResetExperienceWhenLeave()) {
                            bPlayer.setTotalExperience(0);
                            bPlayer.setExp(0);
                            bPlayer.setLevel(0);
                            bPlayer.sendExperienceChange(0, 0);
                        }
                    }
                }
                bPlayer.setGameMode(GameMode.SURVIVAL);
            }, null, 1L);
        } else {
            skyblockManager.addClearMemberNextLogin(players.getMojangId(), cause);
        }
    }

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.delete")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }
        if (args.length != 1) {
            ConfigLoader.language.sendMessage(player, "island.delete.args-missing");
            return true;
        }
        String confirm = args[0];
        if (!confirm.equalsIgnoreCase("confirm")) {
            ConfigLoader.language.sendMessage(player, "admin.delete-no-confirm");
            return true;
        }
        try {
            SkyblockManager skyblockManager = Skyllia.getInstance().getInterneAPI().getSkyblockManager();
            Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
            if (island == null) {
                ConfigLoader.language.sendMessage(player, "island.player.no-island");
                return true;
            }

            Players executorPlayer = island.getMember(player.getUniqueId());

            if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                ConfigLoader.language.sendMessage(player, "island.delete-only-owner");
                return true;
            }

            // Vérification des membres
            if (ConfigLoader.general.isPreventDeletionIfHasMembers()) {
                long memberCount = island.getMembers().stream()
                        .filter(member -> !member.getMojangId().equals(player.getUniqueId()))
                        .count();
                if (memberCount > 0) {
                    ConfigLoader.language.sendMessage(player, "island.player.delete-has-members");
                    return true;
                }
            }

            skyblockManager.setLockedIsland(island, true).whenCompleteAsync((aBoolean, throwable) -> {
                if (throwable != null) {
                    logger.log(Level.FATAL, "Failed to lock island {}: {}", island.getId(), throwable.getMessage());
                    ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
                    return;
                }
                boolean isDisabled = island.setDisable(true);
                if (isDisabled) {
                    this.updatePlayer(skyblockManager, island);
                    this.kickAllPlayerOnIsland(island);

                    AtomicInteger worldsLeft = new AtomicInteger(ConfigLoader.worldManager.getWorldConfigs().size());
                    AtomicBoolean failed = new AtomicBoolean(false);

                    if (worldsLeft.get() == 0) {
                        skyblockManager.setLockedIsland(island, false).whenCompleteAsync((value, throwable1) -> {
                            if (throwable1 != null) {
                                logger.log(Level.FATAL, "Failed to unlock island {}: {}", island.getId(), throwable1.getMessage());
                            }
                            ConfigLoader.language.sendMessage(player, "island.delete-success");
                        });
                        return;
                    }

                    ConfigLoader.worldManager.getWorldConfigs().forEach((s, envs) -> {
                        Skyllia.getInstance().getInterneAPI().getWorldModifier().deleteIsland(island, Bukkit.getWorld(s), ConfigLoader.general.getRegionDistance(), (success) -> {
                            if (!success) failed.set(true);
                            if (worldsLeft.decrementAndGet() == 0) {
                                skyblockManager.setLockedIsland(island, failed.get()).whenCompleteAsync((value, throwable1) -> {
                                    if (throwable1 != null) {
                                        logger.log(Level.FATAL, "Failed to unlock/lock island {}: {}", island.getId(), throwable1.getMessage());
                                    }
                                    if (!failed.get()) {
                                        ConfigLoader.language.sendMessage(player, "island.delete-success");
                                    } else {
                                        ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
                                    }
                                });
                            }
                        });
                    });
                } else {
                    ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
                }
            });
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            ConfigLoader.language.sendMessage(player, "island.generic.unexpected-error");
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String partial = args[0].trim().toLowerCase();
            if ("confirm".startsWith(partial)) {
                return Collections.singletonList("confirm");
            }
        }
        return Collections.emptyList();
    }

    private void updatePlayer(SkyblockManager skyblockManager, Island island) {
        for (Players players : island.getMembers()) {
            players.setRoleType(RoleType.VISITOR);
            island.updateMember(players);
            checkClearPlayer(skyblockManager, players, RemovalCause.ISLAND_DELETED);
        }
    }

    private void kickAllPlayerOnIsland(final Island island) {
        ConfigLoader.worldManager.getWorldConfigs().forEach((s, environnements) -> {
            RegionUtils.getEntitiesInRegion(Skyllia.getPlugin(Skyllia.class), ConfigLoader.general.getRegionDistance(), EntityType.PLAYER, Bukkit.getWorld(s), island.getPosition(), island.getSize(), entity -> {
                Player playerInIsland = (Player) entity;
                if (PermissionImp.hasPermission(entity, "skyllia.island.command.access.bypass")) return;
                PlayerUtils.teleportPlayerSpawn(playerInIsland);
            });
        });
    }
}
