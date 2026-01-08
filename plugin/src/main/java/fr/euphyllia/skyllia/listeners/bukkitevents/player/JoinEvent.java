package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.commands.CacheCommands;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class JoinEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(JoinEvent.class);

    public JoinEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();
        final String worldName = player.getWorld().getName();

        Runnable task = () -> {
            CacheCommands.refreshFor(playerId);
            SkyblockManager skyblockManager = api.getSkyblockManager();

            Island island = SkylliaAPI.getIslandByPlayerId(playerId);

            if (island != null) {
                Players member = island.getMember(playerId);
                if (member != null) {
                    String currentName = player.getName();
                    if (!member.getLastKnowName().equals(currentName)) {
                        member.setLastKnowName(currentName);
                        skyblockManager.updateMember(island, member);
                    }
                }
            }

            boolean shouldTeleportSpawn = island == null ||
                    (ConfigLoader.playerManager.isTeleportOwnIslandOnJoin() && !WorldUtils.isWorldSkyblock(worldName));

            if (shouldTeleportSpawn) {
                if (ConfigLoader.playerManager.isTeleportSpawnIfNoIsland()) {
                    PlayerUtils.teleportPlayerSpawn(player);
                }
            } else {
                api.updateCache(player);
                if (ConfigLoader.playerManager.isTeleportOwnIslandOnJoin() && WorldUtils.isWorldSkyblock(worldName)) {
                    Location centerIsland = RegionHelper.getCenterRegion(
                            player.getWorld(), island.getPosition().x(), island.getPosition().z());
                    api.getPlayerNMS().setOwnWorldBorder(api.getPlugin(), player, centerIsland, island.getSize(), 0, 0);
                }
            }
        };

        executeAsync(task);
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onCheckPlayerClearStuffLogin(PlayerLoginEvent playerLoginEvent) {
        Player player = playerLoginEvent.getPlayer();

        Runnable task = () -> {
            for (RemovalCause cause : RemovalCause.values()) {
                boolean exist = api.getSkyblockManager().checkClearMemberExist(player.getUniqueId(), cause);
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
                if (ConfigLoader.playerManager.isClearInventoryWhenKicked()) player.getInventory().clear();
                if (ConfigLoader.playerManager.isClearEnderChestWhenKicked()) player.getEnderChest().clear();
                if (ConfigLoader.playerManager.isResetExperienceWhenKicked()) {
                    player.setLevel(0);
                    player.setExp(0);
                    player.setTotalExperience(0);
                    player.sendExperienceChange(0, 0);
                }
            }
            case ISLAND_DELETED -> {
                if (ConfigLoader.playerManager.isClearInventoryWhenDelete()) player.getInventory().clear();
                if (ConfigLoader.playerManager.isClearEnderChestWhenDelete()) player.getEnderChest().clear();
                if (ConfigLoader.playerManager.isResetExperienceWhenDelete()) {
                    player.setLevel(0);
                    player.setExp(0);
                    player.setTotalExperience(0);
                    player.sendExperienceChange(0, 0);
                }
            }
            case LEAVE -> {
                if (ConfigLoader.playerManager.isClearInventoryWhenLeave()) player.getInventory().clear();
                if (ConfigLoader.playerManager.isClearEnderChestWhenLeave()) player.getEnderChest().clear();
                if (ConfigLoader.playerManager.isResetExperienceWhenLeave()) {
                    player.setLevel(0);
                    player.setExp(0);
                    player.setTotalExperience(0);
                    player.sendExperienceChange(0, 0);
                }
            }
        }
    }
}
