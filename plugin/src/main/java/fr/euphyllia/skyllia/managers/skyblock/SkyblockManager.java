package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.IslandType;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
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

    public CompletableFuture<@Nullable Boolean> createIsland(UUID islandId, IslandType islandType) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            Island futurIsland = new IslandHook(
                    this.plugin,
                    islandId,
                    islandType.maxMembers(),
                    null,
                    islandType.rayon(),
                    null
            );
            boolean create = this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().insertIslands(futurIsland).join();
            completableFuture.complete(create);
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Island> getIslandByIslandId(UUID islandId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByIslandId(islandId);
    }

    public CompletableFuture<Boolean> disableIsland(Island island, boolean disableValue) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updateDisable(island, disableValue);
    }

    public CompletableFuture<Boolean> isDisabledIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isDisabledIsland(island);
    }

    public CompletableFuture<Boolean> setPrivateIsland(Island island, boolean privateIsland) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updatePrivate(island, privateIsland);
    }

    public CompletableFuture<Boolean> isPrivateIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isPrivateIsland(island);
    }

    public CompletableFuture<@Nullable Island> getIslandByOwner(UUID playerId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByOwnerId(playerId);
    }

    public CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByPlayerId(playerId);
    }

    public CompletableFuture<Boolean> addWarpsIsland(Island island, String name, Location playerLocation) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().updateWarp(island, name, playerLocation);
    }

    public CompletableFuture<Boolean> delWarpsIsland(Island island, String name) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().deleteWarp(island, name);
    }

    public CompletableFuture<@Nullable WarpIsland> getWarpIslandByName(Island island, String name) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().getWarpByName(island, name);
    }

    public CompletableFuture<@Nullable CopyOnWriteArrayList<WarpIsland>> getWarpsIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().getListWarp(island);
    }

    public CompletableFuture<Boolean> updateMember(Island island, Players players) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().updateMember(island, players);
    }

    public CompletableFuture<CopyOnWriteArrayList<Players>> getMembersInIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getMembersInIsland(island);
    }

    public CompletableFuture<Players> getMemberInIsland(Island island, UUID playerId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getPlayersIsland(island, playerId);
    }

    public CompletableFuture<@Nullable Players> getMemberInIsland(Island island, String playerName) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getPlayersIsland(island, playerName);
    }

    public CompletableFuture<Boolean> addClearMemberNextLogin(UUID playerId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().addMemberClear(playerId);
    }

    public CompletableFuture<Boolean> deleteClearMember(UUID playerId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().deleteMemberClear(playerId);
    }

    public CompletableFuture<Boolean> checkClearMemberExist(UUID playerId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().checkClearMemberExist(playerId);
    }

    public CompletableFuture<Boolean> updatePermissionIsland(Island island, PermissionsType permissionsType, RoleType roleType, long permissions) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandPermissionQuery().updateIslandsPermission(island, permissionsType, roleType, permissions);
    }

    public CompletableFuture<PermissionRoleIsland> getPermissionIsland(UUID islandId, PermissionsType permissionsType, RoleType roleType) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandPermissionQuery().getIslandPermission(islandId, permissionsType, roleType);
    }

    public CompletableFuture<Boolean> updateGamerule(Island island, long gameRuleIsland) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandPermissionQuery().updateIslandGameRule(island, gameRuleIsland);
    }

    public CompletableFuture<Long> getGameRulePermission(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandPermissionQuery().getIslandGameRule(island);
    }

    public CompletableFuture<Boolean> deleteMember(Island island, Players oldMember) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().deleteMember(island, oldMember);
    }

    public CompletableFuture<@Nullable Players> getOwnerByIslandID(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getOwnerInIslandId(island);
    }

    public CompletableFuture<Integer> getMaxMemberInIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getMaxMemberInIsland(island);
    }

    public CompletableFuture<Boolean> setMaxMemberInIsland(Island island, int newValue) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().setMaxMemberInIsland(island, newValue);
    }

    public CompletableFuture<Boolean> setSizeIsland(Island island, double newValue) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().setSizeIsland(island, newValue);
    }
}
