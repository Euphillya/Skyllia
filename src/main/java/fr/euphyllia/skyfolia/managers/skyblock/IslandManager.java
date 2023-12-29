package fr.euphyllia.skyfolia.managers.skyblock;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.api.skyblock.model.WarpIsland;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class IslandManager extends Island {

    private final Main plugin;
    private final Position position;
    private final UUID islandId;
    private String islandType;
    private boolean privateIsland;
    private boolean disable;
    private final Timestamp createDate;
    private UUID ownerId;

    /**
     * @param main Plugin Skyfolia
     * @param islandType    Type Island (config.toml)
     * @param islandId      Island ID
     * @param ownerId       Owner Island
     * @param disable       Island disable
     * @param privateIsland Island closed visitor ?
     * @param position      Position X/Z region File
     * @param date          Create Date
     */
    public IslandManager(Main main, String islandType, UUID islandId, UUID ownerId, int disable, int privateIsland, Position position, Timestamp date) {
        this.plugin = main;
        this.islandType = islandType;
        this.islandId = islandId;
        this.ownerId = ownerId;
        this.disable = disable == 1;
        this.privateIsland = privateIsland == 1;
        this.createDate = date;
        this.position = position;
    }

    @Override
    public Timestamp getCreateDate() {
        return this.createDate;
    }

    @Override
    public UUID getIslandId() {
        return this.islandId;
    }

    @Override
    public @Nullable CopyOnWriteArrayList<WarpIsland> getWarps() {
        return this.plugin.getInterneAPI().getSkyblockManager().getWarpsIsland(this).join();
    }

    @Override
    public @Nullable WarpIsland getWarpByName(String name) {
        return this.plugin.getInterneAPI().getSkyblockManager().getWarpIslandByName(this, name).join();
    }

    @Override
    public boolean addWarps(String name, Location loc) {
        return this.plugin.getInterneAPI().getSkyblockManager().addWarpsIsland(this, name, loc).join();
    }

    @Override
    public boolean isDisable() {
        return this.disable;
    }

    @Override
    public void setDisable(boolean disable) {
        if (Boolean.TRUE.equals(this.plugin.getInterneAPI().getSkyblockManager().disableIsland(this).join())) {
            this.disable = disable;
        }
    }

    @Override
    public boolean isPrivateIsland() {
        return this.privateIsland;
    }

    @Override
    public void setPrivateIsland(boolean privateIsland) {
        if (Boolean.TRUE.equals(this.plugin.getInterneAPI().getSkyblockManager().changeStatusIsland(this).join())) {
            this.privateIsland = privateIsland;
        }
    }

    @Override
    public CopyOnWriteArrayList<Players> getMembers() {
        return this.plugin.getInterneAPI().getSkyblockManager().getMembersInIsland(this).join();
    }

    @Override
    public boolean updateMember(Players member, RoleType roleType) {
        return this.plugin.getInterneAPI().getSkyblockManager().setRoleTypePlayer(this, member.getMojangId(), roleType).join();
    }

    @Override
    public UUID getOwnerId() {
        return this.ownerId;
    }

    @Override
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public String getIslandType() {
        return this.islandType;
    }

    @Override
    public void setIslandType(String islandType) {
        this.islandType = islandType;
    }
}
