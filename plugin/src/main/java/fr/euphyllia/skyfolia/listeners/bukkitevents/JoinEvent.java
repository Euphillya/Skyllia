package fr.euphyllia.skyfolia.listeners.bukkitevents;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyfolia.utils.PlayerUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class JoinEvent implements Listener {

    private final InterneAPI api;

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
                    // Todo load cache
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
