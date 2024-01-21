package fr.euphyllia.skyllia.listeners.bukkitevents;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.RegionUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class JoinEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(JoinEvent.class);

    public JoinEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler
    public void onLoadIslandInJoinEvent(PlayerJoinEvent playerJoinEvent) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                Player player = playerJoinEvent.getPlayer();
                SkyblockManager skyblockManager = this.api.getSkyblockManager();
                Island island = skyblockManager.getIslandByOwner(player.getUniqueId()).join();

                if (island == null) {
                    PlayerUtils.teleportPlayerSpawn(this.api.getPlugin(), player);
                } else {
                    this.api.updateCache(player);
                    World world = player.getLocation().getWorld();
                    if (Boolean.TRUE.equals(WorldUtils.isWorldSkyblock(world.getName()))) {
                        Location centerIsland = RegionUtils.getCenterRegion(world, island.getPosition().x(), island.getPosition().z());
                        try {
                            PlayerUtils.setOwnWorldBorder(this.api.getPlugin(), player, centerIsland, "", island.getSize(), 0, 0);
                        } catch (UnsupportedMinecraftVersionException e) {
                            logger.log(Level.FATAL, e.getMessage(), e);
                        }
                    }

                }
            });
        } finally {
            executor.shutdown();
        }
    }

    @EventHandler
    public void onCheckPlayerClearStuffLogin(PlayerLoginEvent playerLoginEvent) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            executor.execute(() -> {
                Player player = playerLoginEvent.getPlayer();
                boolean exist = this.api.getSkyblockManager().checkClearMemberExist(player.getUniqueId()).join();
                if (!exist) return;
                this.api.getSkyblockManager().deleteClearMember(player.getUniqueId());
                player.getScheduler().run(this.api.getPlugin(), t -> {
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
                }, null);
            });
        } finally {
            executor.shutdown();
        }
    }

}
