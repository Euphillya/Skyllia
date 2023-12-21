package fr.euphyllia.skyfolia.managers.skyblock;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkyblockManager {

    private final Main plugin;
    private final Logger logger = LogManager.getLogger(this);

    public SkyblockManager(Main main) {
        this.plugin = main;
    }

    public CompletableFuture<@Nullable Island> getIslandByOwner(Player player) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        try {
            Island island = this.plugin.getInterneAPI().getIslandQuery().getIslandByOwnerId(player.getUniqueId()).join();
            completableFuture.complete(island);
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            completableFuture.complete(null);
        }
        return completableFuture;
    }
    public CompletableFuture<@Nullable Island> createIsland(Player player) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        try {
            UUID idIsland = UUID.randomUUID();
            Island futurIsland = new Island(
                    "osef",
                    idIsland,
                    player.getUniqueId(),
                    0,
                    0,
                    null
            );
            boolean create = this.plugin.getInterneAPI().getIslandQuery().insertIslands(futurIsland).join();
            if (create) {
                Island island = this.plugin.getInterneAPI().getIslandQuery().getIslandByIslandId(idIsland).join();
                completableFuture.complete(island);
            }

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            completableFuture.complete(null);
        }
        return completableFuture;
    }
}
