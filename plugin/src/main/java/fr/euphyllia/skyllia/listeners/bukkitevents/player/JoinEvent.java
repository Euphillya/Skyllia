package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class JoinEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(JoinEvent.class);

    public JoinEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler
    public void onLoadIslandInJoinEvent(PlayerJoinEvent playerJoinEvent) {
        Bukkit.getAsyncScheduler().runNow(api.getPlugin(), task -> {
            Player player = playerJoinEvent.getPlayer();
            SkyblockManager skyblockManager = this.api.getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

            if (island == null || (ConfigToml.teleportPlayerOnIslandWhenJoin && !Boolean.TRUE.equals(WorldUtils.isWorldSkyblock(player.getLocation().getWorld().getName())))) {
                if (ConfigToml.teleportPlayerNotIslandWhenJoin) {
                    PlayerUtils.teleportPlayerSpawn(player);
                }
            } else {
                if (ConfigToml.teleportPlayerOnIslandWhenJoin) {
                    World world = player.getLocation().getWorld();
                    if (Boolean.TRUE.equals(WorldUtils.isWorldSkyblock(world.getName()))) {
                        Location centerIsland = RegionHelper.getCenterRegion(world, island.getPosition().x(), island.getPosition().z());
                        this.api.getPlayerNMS().setOwnWorldBorder(this.api.getPlugin(), player, centerIsland, island.getSize(), 0, 0);
                    }
                }
            }
        });
    }

    @EventHandler
    public void onCheckPlayerClearStuffLogin(PlayerLoginEvent playerLoginEvent) {
        Player player = playerLoginEvent.getPlayer();
        Bukkit.getAsyncScheduler().runNow(api.getPlugin(), task -> {
            for (RemovalCause cause : RemovalCause.values()) {
                boolean exist = this.api.getSkyblockManager().checkClearMemberExist(player.getUniqueId(), cause).join();
                if (!exist) return;
                this.api.getSkyblockManager().deleteClearMember(player.getUniqueId(), cause);
                player.getScheduler().execute(api.getPlugin(), () -> {
                    switch (cause) {
                        case KICKED -> {
                            if (ConfigToml.clearInventoryWhenKickedIsland) {
                                player.getInventory().clear();
                            }
                            if (ConfigToml.clearEnderChestWhenKickedIsland) {
                                player.getEnderChest().clear();
                            }
                            if (ConfigToml.resetExperiencePlayerWhenKickedIsland) {
                                player.setTotalExperience(0);
                                player.sendExperienceChange(0, 0); // Mise à jour du packet
                            }
                        }
                        case ISLAND_DELETED -> {
                            if (ConfigToml.clearInventoryWhenDeleteIsland) {
                                player.getInventory().clear();
                            }
                            if (ConfigToml.clearEnderChestWhenDeleteIsland) {
                                player.getEnderChest().clear();
                            }
                            if (ConfigToml.resetExperiencePlayerWhenDeleteIsland) {
                                player.setTotalExperience(0);
                                player.sendExperienceChange(0, 0); // Mise à jour du packet
                            }
                        }
                        case LEAVE -> {
                            if (ConfigToml.clearInventoryWhenLeaveIsland) {
                                player.getInventory().clear();
                            }
                            if (ConfigToml.clearEnderChestWhenLeaveIsland) {
                                player.getEnderChest().clear();
                            }
                            if (ConfigToml.resetExperiencePlayerWhenLeaveIsland) {
                                player.setTotalExperience(0);
                                player.sendExperienceChange(0, 0);
                            }
                        }
                    }
                    player.setGameMode(GameMode.SURVIVAL);
                }, null, 1L);
            }
        });
    }

}
