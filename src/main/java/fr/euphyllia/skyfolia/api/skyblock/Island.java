package fr.euphyllia.skyfolia.api.skyblock;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Island {

    private UUID islandId;
    private int maxMembers;
    private ConcurrentHashMap<String, Location> warps;
    private boolean privateIsland;
    private CopyOnWriteArrayList<Players> members;
    private boolean disable;
    private long createDate;
    private UUID ownerId;
    private final int X;
    private final int Z;

    public Island(UUID futurIslandId, UUID ownerId, int disable, int privateIsland, int maxMembers, int regionX, int regionZ) {
        this.islandId = futurIslandId;
        this.ownerId = ownerId;
        this.disable = disable == 1;
        this.members = new CopyOnWriteArrayList<>();
        this.privateIsland = privateIsland == 1;
        this.warps = new ConcurrentHashMap<>();
        this.createDate = System.currentTimeMillis();
        this.maxMembers = maxMembers;
        this.X = regionX;
        this.Z = regionZ;
    }

    public Island(UUID islandId, int disable, int privateIsland, int maxMembers, CopyOnWriteArrayList<Players> membersList, ConcurrentHashMap<String, Location> warpsMap,  int regionX, int regionZ, long date) {
        this.islandId = islandId;
        this.disable = disable == 1;
        this.members = membersList;
        this.privateIsland = privateIsland == 1;
        this.warps = warpsMap;
        this.createDate = date;
        this.maxMembers = maxMembers;
        this.X = regionX;
        this.Z = regionZ;
    }

    public long getCreateDate() {
        return this.createDate;
    }

    public UUID getIslandId() {
        return this.islandId;
    }

    public ConcurrentMap<String, Location> getWarps() {
        return this.warps;
    }

    public boolean addWarps(String name, Location loc) {
        try {
            this.warps.put(name, loc);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDisable() {
        return this.disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public boolean isPrivateIsland() {
        return this.privateIsland;
    }

    public void setPrivateIsland(boolean privateIsland) {
        this.privateIsland = privateIsland;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public CopyOnWriteArrayList<Players> getMembers() {
        return this.members;
    }

    public boolean addMembers(Players member) {
        if (this.maxMembers >= this.members.size()) {
            this.members.add(member);
            return true;
        } else {
            return false;
        }
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }

    public void setMembers(CopyOnWriteArrayList<Players> members) {
        this.members = members;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public int getRegionX() {
        return this.X;
    }

    public int getRegionZ() {
        return this.Z;
    }
}
