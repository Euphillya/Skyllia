package fr.euphyllia.skyfolia.commands.subcommands;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.event.SkyblockRemoveEvent;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.commands.SubCommandInterface;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.configuration.section.WorldConfig;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.PlayerUtils;
import fr.euphyllia.skyfolia.utils.WorldEditUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
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

    @Override
    public boolean onCommand(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                return true;
            }
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            try {
                executor.execute(() -> {
                    SkyblockManager skyblockManager = plugin.getInterneAPI().getSkyblockManager();
                    Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();
                    if (island == null) {
                        // pas d'ile
                        return;
                    }
                    if (!island.getOwnerId().equals(player.getUniqueId())) {
                        return; // Seul l'owner peut !
                    }

                    this.teleportPlayerSpawn(plugin, island);

                    island.setDisable(true);

                    for (WorldConfig worldConfig : ConfigToml.worldConfigs) {
                        WorldEditUtils.deleteIsland(plugin, island, Bukkit.getWorld(worldConfig.name()), 50);
                    }

                    SkyblockRemoveEvent skyblockRemoveEvent = new SkyblockRemoveEvent(island);
                    Bukkit.getServer().getPluginManager().callEvent(skyblockRemoveEvent);
                });
            } finally {
                executor.shutdown();
            }

            return true;
        } catch (Exception ex) {
            logger.fatal("Suppression ile a un bug", ex);
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Main plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    private void teleportPlayerSpawn(Main plugin, @NotNull Island island) {
        for (Players players : island.getMembers()) {
            Player member = Bukkit.getPlayer(players.getMojangId());
            if (member == null) continue;
            if (member.isOnline()) {
                PlayerUtils.teleportPlayerSpawn(plugin, member);
            }
        }
    }

    /* Todo Va être déplacer
    player.setGameMode(GameMode.SPECTATOR);
        if (ConfigToml.clearInventoryWhenDeleteIsland) {
            player.getInventory().clear();
        }
        if (ConfigToml.clearEnderChestWhenDeleteIsland) {
            player.getEnderChest().clear();
        }
        if (ConfigToml.clearEnderChestWhenDeleteIsland) {
            player.setTotalExperience(0);
            player.sendExperienceChange(0, 0); // Mise à jour du packet
        }

        player.setGameMode(GameMode.SURVIVAL);
     */
}
