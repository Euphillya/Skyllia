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
        Player player = playerJoinEvent.getPlayer();

        Runnable task = () -> {
            SkyblockManager skyblockManager = api.getSkyblockManager();
            Island island = skyblockManager.getIslandByPlayerId(player.getUniqueId()).join();

            boolean shouldTeleportSpawn = island == null ||
                    (ConfigToml.teleportPlayerOnIslandWhenJoin && !WorldUtils.isWorldSkyblock(player.getWorld().getName()));

            if (shouldTeleportSpawn) {
                if (ConfigToml.teleportPlayerNotIslandWhenJoin) {
                    PlayerUtils.teleportPlayerSpawn(player);
                }
            } else {
                api.updateCache(player);
                if (ConfigToml.teleportPlayerOnIslandWhenJoin && WorldUtils.isWorldSkyblock(player.getWorld().getName())) {
                    Location centerIsland = RegionHelper.getCenterRegion(
                            player.getWorld(), island.getPosition().x(), island.getPosition().z());
                    api.getPlayerNMS().setOwnWorldBorder(api.getPlugin(), player, centerIsland, island.getSize(), 0, 0);
                }
            }
        };

        executeAsync(task);
    }

    @EventHandler
    public void onCheckPlayerClearStuffLogin(PlayerLoginEvent playerLoginEvent) {
        Player player = playerLoginEvent.getPlayer();

        Runnable task = () -> {
            for (RemovalCause cause : RemovalCause.values()) {
                boolean exist = api.getSkyblockManager().checkClearMemberExist(player.getUniqueId(), cause).join();
                if (!exist) continue;

                api.getSkyblockManager().deleteClearMember(player.getUniqueId(), cause);

                Runnable playerTask = () -> {
                    clearPlayerData(player, cause);
                    player.setGameMode(GameMode.SURVIVAL);
                };

                player.getScheduler().execute(api.getPlugin(), playerTask, null, 1L);
            }
        };

        executeAsync(task);
    }

    private void executeAsync(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(api.getPlugin(), scheduledTask -> task.run());
    }

    private void clearPlayerData(Player player, RemovalCause cause) {
        switch (cause) {
            case KICKED -> {
                if (ConfigToml.clearInventoryWhenKickedIsland) player.getInventory().clear();
                if (ConfigToml.clearEnderChestWhenKickedIsland) player.getEnderChest().clear();
                if (ConfigToml.resetExperiencePlayerWhenKickedIsland) {
                    player.setTotalExperience(0);
                    player.sendExperienceChange(0, 0);
                }
            }
            case ISLAND_DELETED -> {
                if (ConfigToml.clearInventoryWhenDeleteIsland) player.getInventory().clear();
                if (ConfigToml.clearEnderChestWhenDeleteIsland) player.getEnderChest().clear();
                if (ConfigToml.resetExperiencePlayerWhenDeleteIsland) {
                    player.setTotalExperience(0);
                    player.sendExperienceChange(0, 0);
                }
            }
            case LEAVE -> {
                if (ConfigToml.clearInventoryWhenLeaveIsland) player.getInventory().clear();
                if (ConfigToml.clearEnderChestWhenLeaveIsland) player.getEnderChest().clear();
                if (ConfigToml.resetExperiencePlayerWhenLeaveIsland) {
                    player.setTotalExperience(0);
                    player.sendExperienceChange(0, 0);
                }
            }
        }
    }
}
