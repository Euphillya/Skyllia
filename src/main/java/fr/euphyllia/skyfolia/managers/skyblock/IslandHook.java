package fr.euphyllia.skyfolia.managers.skyblock;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import fr.euphyllia.skyfolia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyfolia.utils.exception.MaxIslandSizeExceedException;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class IslandHook extends Island {

    private final Main plugin;
    private final Position position;
    private final UUID islandId;
    private final Timestamp createDate;
    private final UUID ownerId;
    private final String islandType;
    private int size;

    /**
     * @param main       Plugin Skyfolia
     * @param islandType Type Island (config.toml)
     * @param islandId   Island ID
     * @param ownerId    Owner Island
     * @param position   Position X/Z region File
     * @param size       Rayon Island
     * @param date       Create Date
     */
    public IslandHook(Main main, String islandType, UUID islandId, UUID ownerId, Position position, int size, Timestamp date) throws MaxIslandSizeExceedException {
        this.plugin = main;
        this.islandType = islandType;
        this.islandId = islandId;
        this.ownerId = ownerId;
        this.createDate = date;
        this.position = position;
        if (size <= 255) {
            throw new MaxIslandSizeExceedException("Size exceeded !");
        }
        this.size = size;
    }

    @Override
    public Timestamp getCreateDate() {
        return this.createDate;
    }

    @Override
    public UUID getId() {
        return this.islandId;
    }

    @Override
    public int getSize() throws MaxIslandSizeExceedException {
        if (this.size == -1) {
            // check db
            throw new UnsupportedOperationException("pas encore implémenter");
        } else {
            if (size <= 255) {
                throw new MaxIslandSizeExceedException("Size exceeded !");
            }
            return this.size;
        }
    }

    @Override
    public void setSize(int rayon) throws MaxIslandSizeExceedException{
        if (rayon <= 255) {
            throw new MaxIslandSizeExceedException("Size exceeded !");
        }
        this.size = rayon;
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
        return this.plugin.getInterneAPI().getSkyblockManager().isDisabledIsland(this).join();
    }

    @Override
    public void setDisable(boolean disable) {
        this.plugin.getInterneAPI().getSkyblockManager().disableIsland(this).join();
    }

    @Override
    public boolean isPrivateIsland() {
        return this.plugin.getInterneAPI().getSkyblockManager().isPrivateIsland(this).join();
    }

    @Override
    public void setPrivateIsland(boolean privateIsland) {
        this.plugin.getInterneAPI().getSkyblockManager().setPrivateIsland(this).join();
    }

    @Override
    public CopyOnWriteArrayList<Players> getMembers() {
        return this.plugin.getInterneAPI().getSkyblockManager().getMembersInIsland(this).join();
    }

    @Override
    public Players getMember(UUID mojangId) {
        return this.plugin.getInterneAPI().getSkyblockManager().getMemberInIsland(this, mojangId).join();
    }

    @Override
    public Players getMember(String playerName) {
        return this.plugin.getInterneAPI().getSkyblockManager().getMemberInIsland(this, playerName).join();
    }

    @Override
    public void removeMember(Players players) {
        throw new UnsupportedOperationException("pas encore implémenter");
    }

    @Override
    public boolean updateMember(Players member) {
        return this.plugin.getInterneAPI().getSkyblockManager().updateMember(this, member).join();
    }

    @Override
    public UUID getOwnerId() {
        return this.ownerId;
    }

    @Override
    public void setOwnerId(UUID ownerId) {
        // Todo ? a faire
        throw new UnsupportedOperationException("pas encore implémenter");
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
        throw new UnsupportedOperationException("pas encore implémenter");
    }
}
