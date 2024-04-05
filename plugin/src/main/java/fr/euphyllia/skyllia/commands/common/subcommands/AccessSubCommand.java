package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.commands.SubCommandInterface;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.RegionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AccessSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(AccessSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyllia.island.command.access")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

        if (island == null) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerHasNotIsland);
            return true;
        }

        Players executorPlayer = island.getMember(player.getUniqueId());
        if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
            PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

            PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
            if (!permissionManager.hasPermission(PermissionsCommandIsland.ACCESS)) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                return true;
            }
        }

        boolean statusAccessUpdate = !island.isPrivateIsland();

        boolean isUpdate = island.setPrivateIsland(statusAccessUpdate);
        if (isUpdate) {
            if (statusAccessUpdate) {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messageAccessIslandClose);
                ConfigToml.worldConfigs.forEach(worldConfig -> {
                    RegionUtils.getEntitiesInRegion(plugin, EntityType.PLAYER, Bukkit.getWorld(worldConfig.name()), island.getPosition().x(), island.getPosition().z(), entity -> {
                        Player playerInIsland = (Player) entity;
                        if (playerInIsland.hasPermission("skyllia.island.command.access.bypass")) return;
                        Players players = island.getMember(playerInIsland.getUniqueId());
                        if (players == null || players.getRoleType().equals(RoleType.BAN) || players.getRoleType().equals(RoleType.VISITOR)) {
                            PlayerUtils.teleportPlayerSpawn(playerInIsland);
                        }
                    });
                });
            } else {
                LanguageToml.sendMessage(plugin, player, LanguageToml.messageAccessIslandOpen);
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
