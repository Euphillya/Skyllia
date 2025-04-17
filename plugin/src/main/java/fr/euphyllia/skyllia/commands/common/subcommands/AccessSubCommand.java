package fr.euphyllia.skyllia.commands.common.subcommands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.commands.SubCommandInterface;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.managers.PermissionsManagers;
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
            ConfigLoader.language.sendMessage(sender, "island.player.player-only-command");
            return true;
        }
        if (!PermissionImp.hasPermission(sender, "skyllia.island.command.access")) {
            ConfigLoader.language.sendMessage(player, "island.player.permission-denied");
            return true;
        }

        SkyblockManager skyblockManager = Skyllia.getPlugin(Skyllia.class).getInterneAPI().getSkyblockManager();
        Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

        if (island == null) {
            ConfigLoader.language.sendMessage(player, "island.player.no-island");
            return true;
        }

        Players executorPlayer = island.getMember(player.getUniqueId());

        if (!PermissionsManagers.testPermissions(executorPlayer, player, island, PermissionsCommandIsland.ACCESS, false)) {
            return true;
        }

        boolean statusAccessUpdate = !island.isPrivateIsland();

        boolean isUpdate = island.setPrivateIsland(statusAccessUpdate);
        if (isUpdate) {
            if (statusAccessUpdate) {
                ConfigLoader.language.sendMessage(player, "island.access.close");
                ConfigLoader.worldManager.getWorldConfigs().forEach((name, environnements) -> {
                    RegionUtils.getEntitiesInRegion(Skyllia.getPlugin(Skyllia.class), ConfigLoader.general.getRegionDistance(), EntityType.PLAYER, Bukkit.getWorld(name), island.getPosition(), island.getSize(), entity -> {
                        Player playerInIsland = (Player) entity;
                        if (PermissionImp.hasPermission(entity, "skyllia.island.command.access.bypass")) return;
                        Runnable teleportPlayerRun = () -> {
                            Players players = island.getMember(playerInIsland.getUniqueId());
                            if (players == null || players.getRoleType().equals(RoleType.BAN) || players.getRoleType().equals(RoleType.VISITOR)) {
                                PlayerUtils.teleportPlayerSpawn(playerInIsland);
                            }
                        };
                        Bukkit.getAsyncScheduler().runNow(Skyllia.getPlugin(Skyllia.class), scheduledTask -> {
                            teleportPlayerRun.run();
                        });
                    });
                });
            } else {
                ConfigLoader.language.sendMessage(player, "island.access.open");
            }
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull Plugin plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
