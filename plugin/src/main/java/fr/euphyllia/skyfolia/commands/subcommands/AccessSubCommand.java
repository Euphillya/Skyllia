package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.managers.skyblock.PermissionManager;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.PlayerUtils;
import fr.euphyllia.skyfolia.utils.RegionUtils;
import org.apache.logging.log4j.Level;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AccessSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(AccessSubCommand.class);

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyfolia.island.command.access")) {
            LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
            return true;
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                try {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();

                    if (island == null) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerHasNotIsland);
                        return;
                    }

                    Players executorPlayer = island.getMember(player.getUniqueId());

                    PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), PermissionsType.COMMANDS, executorPlayer.getRoleType()).join();

                    PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
                    if (!executorPlayer.getRoleType().equals(RoleType.OWNER) && !permissionManager.hasPermission(PermissionsCommandIsland.ACCESS)) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messagePlayerPermissionDenied);
                        return;
                    }

                    boolean statusAccessUpdate = !island.isPrivateIsland();

                    island.setPrivateIsland(statusAccessUpdate);
                    if (statusAccessUpdate) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageAccessIslandClose);
                        ConfigToml.worldConfigs.forEach(worldConfig -> {
                            RegionUtils.getEntitiesInRegion(plugin, EntityType.PLAYER, Bukkit.getWorld(worldConfig.name()), island.getPosition().regionX(), island.getPosition().regionZ(), entity -> {
                                Player playerInIsland = (Player) entity;
                                if (playerInIsland.hasPermission("skyfolia.island.command.access.bypass")) return;
                                Players players = island.getMember(playerInIsland.getUniqueId());
                                if (players == null || players.getRoleType().equals(RoleType.BAN) || players.getRoleType().equals(RoleType.VISITOR)) {
                                    PlayerUtils.teleportPlayerSpawn(plugin, playerInIsland);
                                }
                            });
                        });
                    } else {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageAccessIslandOpen);
                    }
                } catch (Exception e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageError);
                }
            });
        } finally {
            executor.shutdown();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
