package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class AccessSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(AccessSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            LanguageToml.sendMessage(sender, LanguageToml.messageCommandPlayerOnly);
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.access")) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        SkyblockManager skyblockManager = Main.getPlugin(Main.class).getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

        if (island == null) {
            LanguageToml.sendMessage(player, LanguageToml.messagePlayerHasNotIsland);
            return true;
        }

        Players executorPlayer = island.getMember(player.getUniqueId());
        if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
            PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

            PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
            if (!permissionManager.hasPermission(PermissionsCommandIsland.ACCESS)) {
                LanguageToml.sendMessage(player, LanguageToml.messagePlayerPermissionDenied);
                return true;
            }
        }

        boolean statusAccessUpdate = !island.isPrivateIsland();

        boolean isUpdate = island.setPrivateIsland(statusAccessUpdate);
        if (isUpdate) {
            if (statusAccessUpdate) {
                LanguageToml.sendMessage(player, LanguageToml.messageAccessIslandClose);
                ConfigToml.worldConfigs.forEach(worldConfig -> {
                    RegionUtils.getEntitiesInRegion(Main.getPlugin(Main.class), ConfigToml.regionDistance, EntityType.PLAYER, Bukkit.getWorld(worldConfig.name()), island.getPosition(), island.getSize(), entity -> {
                        Player playerInIsland = (Player) entity;
                        if (PermissionImp.hasPermission(entity, "skyllia.island.command.access.bypass")) return;
                        Runnable teleportPlayerRun = () -> {
                            Players players = island.getMember(playerInIsland.getUniqueId());
                            if (players == null || players.getRoleType().equals(RoleType.BAN) || players.getRoleType().equals(RoleType.VISITOR)) {
                                PlayerUtils.teleportPlayerSpawn(playerInIsland);
                            }
                        };
                        Bukkit.getAsyncScheduler().runNow(Main.getPlugin(Main.class), scheduledTask -> {
                            teleportPlayerRun.run();
                        });
                    });
                });
            } else {
                LanguageToml.sendMessage(player, LanguageToml.messageAccessIslandOpen);
            }
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
