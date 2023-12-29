package fr.euphyllia.skyfolia.managers.skyblock;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.api.skyblock.model.WarpIsland;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class SkyblockManager {

    private final Main plugin;
    private final Logger logger = LogManager.getLogger(SkyblockManager.class);

    public SkyblockManager(Main main) {
        this.plugin = main;
    }

    public CompletableFuture<@Nullable Island> createIsland(Player player, IslandType islandType) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        try {
            UUID idIsland = UUID.randomUUID();
            Island futurIsland = new IslandManager(
                    this.plugin,
                    islandType.name(),
                    idIsland,
                    player.getUniqueId(),
                    0,
                    0,
                    null,
                    null
            );
            boolean create = this.plugin.getInterneAPI().getIslandQuery().insertIslands(futurIsland).join();
            if (create) {
                Island island = this.getIslandByOwner(player).join();
                completableFuture.complete(island);
            }

        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage());
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> disableIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updateDisable(island);
    }

    public CompletableFuture<Boolean> changeStatusIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updatePrivate(island);
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

    public CompletableFuture<Boolean> addWarpsIsland(Island island, String name, Location playerLocation) {
        // Todo ? Vérifier le nombre possible à créer
        // Todo ? Egalement s'il a le droit
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().updateWarp(island, name, playerLocation);
    }

    public CompletableFuture<@Nullable WarpIsland> getWarpIslandByName(Island island, String name) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().getWarpByName(island, name);
    }

    public CompletableFuture<@Nullable CopyOnWriteArrayList<WarpIsland>> getWarpsIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().getListWarp(island);
    }

    public CompletableFuture<RoleType> getRoleTypePlayer(Island island, OfflinePlayer player) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getRoleTypeMemberInIsland(island, player.getUniqueId());
    }

    public CompletableFuture<Boolean> setRoleTypePlayer(Island island, OfflinePlayer player, RoleType roleType) {
        return this.setRoleTypePlayer(island, player.getUniqueId(), roleType);
    }

    public CompletableFuture<Boolean> setRoleTypePlayer(Island island, UUID playerId, RoleType roleType) {
        // Todo ? Si le rolType est visitor, le supprimer de la db
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().setRoleTypeMemberInIsland(island, playerId, roleType);
    }

    public CompletableFuture<CopyOnWriteArrayList<Players>> getMembersInIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getMembersInIsland(island);
    }
}
