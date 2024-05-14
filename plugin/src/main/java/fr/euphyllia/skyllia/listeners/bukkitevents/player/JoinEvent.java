package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
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

public class JoinEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(JoinEvent.class);

    public JoinEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler
    public void onLoadIslandInJoinEvent(PlayerJoinEvent playerJoinEvent) {
        SkylliaAPI.getNativeScheduler()
                .runTask(SchedulerType.ASYNC, schedulerTask -> {
                    Player player = playerJoinEvent.getPlayer();
                    SkyblockManager skyblockManager = this.api.getSkyblockManager();
                    Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

                    if (island == null) {
                        if (ConfigToml.teleportPlayerNotIslandWhenJoin) {
                            PlayerUtils.teleportPlayerSpawn(player);
                        }
                    } else {
                        this.api.updateCache(player);
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
        SkylliaAPI.getNativeScheduler()
                .runTask(SchedulerType.ASYNC, schedulerTask -> {
                    Player player = playerLoginEvent.getPlayer();
                    boolean exist = this.api.getSkyblockManager().checkClearMemberExist(player.getUniqueId()).join();
                    if (!exist) return;
                    this.api.getSkyblockManager().deleteClearMember(player.getUniqueId());
                    SkylliaAPI.getScheduler()
                            .runTask(SchedulerType.SYNC, player, schedulerTask1 -> {
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

    }

}
