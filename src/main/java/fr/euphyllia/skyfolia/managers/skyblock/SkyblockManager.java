package fr.euphyllia.skyfolia.managers.skyblock;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkyblockManager {

    private final Main plugin;
    private final Logger logger = LogManager.getLogger("fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager");

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
            Position position = this.getFuturPositionIsland().join();
            Island island = new Island(
                    "osef",
                    UUID.randomUUID(),
                    player.getUniqueId(),
                    0,
                    0,
                    position
            );
            UUID islandId = this.plugin.getInterneAPI().getIslandQuery().insertIslands(island).join();
            if (islandId == null) {
                completableFuture.complete(null);
            } else {
                completableFuture.complete(island);
            }
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Position> getFuturPositionIsland() {
        CompletableFuture<Position> positionCompletableFuture = new CompletableFuture<>();
        int islandExist = this.plugin.getInterneAPI().getIslandQuery().getCountIslandExist().join();
        if (islandExist == -1) {
            throw new RuntimeException("A problem with the database has occurred.");
        }
        // Todo ? Bug quand il y aura duplication, à trouver un moyen que la base de donnée fasse directement le calcul.
        double r = Math.floor((Math.sqrt(islandExist + 1d) - 1) / 2) + 1;
        double p = (8 * r * (r - 1)) / 2;
        double en = r * 2;
        double a = (islandExist - p) % (r * 8);
        int loc = (int) Math.floor(a / (r * 2));
        int regionX = 0;
        int regionZ = switch (loc) {
            case 0 -> {
                regionX = (int) (a - r);
                yield (int) (-r);
            }
            case 1 -> {
                regionX = (int) r;
                yield (int) ((a % en) - r);
            }
            case 2 -> {
                regionX = (int) (r - (a % en));
                yield (int) r;
            }
            case 3 -> {
                regionX = (int) (-r);
                yield (int) (r - (a % en));
            }
            default -> throw new RuntimeException("A problem with the generation of the island position has occurred.");
        };
        positionCompletableFuture.complete(new Position(regionX, regionZ));
        return positionCompletableFuture;
    }



}
