package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.event.SkyblockRemoveEvent;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.configuration.LanguageToml;
import fr.euphyllia.skyfolia.configuration.section.WorldConfig;
import fr.euphyllia.skyfolia.managers.skyblock.PermissionManager;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.PlayerUtils;
import fr.euphyllia.skyfolia.utils.WorldEditUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DeleteSubCommand implements SubCommandInterface {

    private final Logger logger = LogManager.getLogger(DeleteSubCommand.class);

    public static void checkClearPlayer(Main plugin, SkyblockManager skyblockManager, Players players) {
        Player bPlayer = Bukkit.getPlayer(players.getMojangId());
        if (bPlayer != null && bPlayer.isOnline()) {
            PlayerUtils.teleportPlayerSpawn(plugin, bPlayer);
            bPlayer.getScheduler().run(plugin, t -> {
                if (ConfigToml.clearInventoryWhenDeleteIsland) {
                    bPlayer.getInventory().clear();
                }
                if (ConfigToml.clearEnderChestWhenDeleteIsland) {
                    bPlayer.getEnderChest().clear();
                }
                if (ConfigToml.clearEnderChestWhenDeleteIsland) {
                    bPlayer.setTotalExperience(0);
                    bPlayer.sendExperienceChange(0, 0); // Mise à jour du packet
                }
                bPlayer.setGameMode(GameMode.SURVIVAL);
            }, null);
        } else {
            skyblockManager.addClearMemberNextLogin(players.getMojangId());
        }
    }

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("skyfolia.island.command.delete")) {
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

                    if (!executorPlayer.getRoleType().equals(RoleType.OWNER)) {
                        LanguageToml.sendMessage(plugin, player, LanguageToml.messageOnlyOwnerCanDeleteIsland);
                        return;
                    }


                    island.setDisable(true);
                    this.updatePlayer(plugin, skyblockManager, island);

                    for (WorldConfig worldConfig : ConfigToml.worldConfigs) {
                        WorldEditUtils.deleteIsland(plugin, island, Bukkit.getWorld(worldConfig.name()), island.getSize());
                    }

                    LanguageToml.sendMessage(plugin, player, LanguageToml.messageIslandDeleteSuccess);

                    SkyblockRemoveEvent skyblockRemoveEvent = new SkyblockRemoveEvent(island);
                    Bukkit.getServer().getPluginManager().callEvent(skyblockRemoveEvent);
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
        return null;
    }

    private void updatePlayer(Main plugin, SkyblockManager skyblockManager, Island island) {
        for (Players players : island.getMembers()) {
            players.setRoleType(RoleType.VISITOR);
            island.updateMember(players);
            checkClearPlayer(plugin, skyblockManager, players);
        }
    }
}
