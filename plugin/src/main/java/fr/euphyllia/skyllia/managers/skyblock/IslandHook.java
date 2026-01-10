package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.*;
import fr.euphyllia.skyllia.api.permissions.CompiledPermissions;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * An implementation of {@link Island} for managing island data and operations.
 */
public class IslandHook extends Island {

    private final Skyllia plugin;
    private final UUID islandId;
    private final Timestamp createDate;
    private final Position position;
    private final int maxMemberInIsland;
    private double islandSize;
    private transient volatile CompiledPermissions compiledPermissions;

    /**
     * Constructs a new {@code IslandHook} instance.
     *
     * @param islandId   The UUID of the island.
     * @param maxMembers The maximum number of members allowed on the island.
     * @param position   The region-based position of the island (X/Z).
     * @param size       The radius (size) of the island.
     * @param date       The creation date, or {@code null} if unknown.
     */
    public IslandHook(UUID islandId,
                      int maxMembers,
                      Position position,
                      double size,
                      Timestamp date) {
        this.plugin = Skyllia.getInstance();
        this.islandId = islandId;
        this.createDate = date;
        this.position = position;
        this.maxMemberInIsland = maxMembers;
        this.islandSize = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Players getOwner() {
        return this.plugin.getInterneAPI().getSkyblockManager().getOwnerByIslandId(this.islandId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp getCreateDate() {
        return this.createDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() {
        return this.islandId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSize() {
        return this.islandSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setSize(double newSize) {
        double oldSize = this.islandSize;
        this.islandSize = newSize;
        // Update in database
        boolean isUpdated = this.plugin.getInterneAPI()
                .getSkyblockManager()
                .setSizeIsland(this, newSize);

        if (isUpdated) {
            Bukkit.getPluginManager().callEvent(new SkyblockChangeSizeEvent(this, oldSize, newSize));
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable List<WarpIsland> getWarps() {
        return this.plugin.getInterneAPI().getSkyblockManager().getWarpsIsland(this.islandId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable WarpIsland getWarpByName(String name) {
        return this.plugin.getInterneAPI().getSkyblockManager().getWarpIslandByName(this.islandId, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addWarps(String name, Location loc, boolean ignoreEvent) {
        SkyblockCreateWarpEvent event = new SkyblockCreateWarpEvent(this, name, loc);
        if (!ignoreEvent) {
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
        }
        return this.plugin.getInterneAPI().getSkyblockManager()
                .addWarpsIsland(this.islandId, event.getWarpName(), event.getWarpLocation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delWarp(String name) {
        SkyblockDeleteWarpEvent event = new SkyblockDeleteWarpEvent(this, name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        return this.plugin.getInterneAPI().getSkyblockManager()
                .delWarpsIsland(this.islandId, event.getWarpName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisable() {
        return this.plugin.getInterneAPI().getSkyblockManager().isDisabledIsland(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setDisable(boolean disable) {
        SkyblockDeleteEvent event = new SkyblockDeleteEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        return this.plugin.getInterneAPI().getSkyblockManager().disableIsland(this, disable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivateIsland() {
        return this.plugin.getInterneAPI().getSkyblockManager().isPrivateIsland(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setPrivateIsland(boolean privateIsland) {
        return this.plugin.getInterneAPI().getSkyblockManager().setPrivateIsland(this, privateIsland);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Players> getMembers() {
        return this.plugin.getInterneAPI().getSkyblockManager().getMembersInIsland(this);
    }

    @Override
    public List<Players> getBannedMembers() {
        return this.plugin.getInterneAPI().getSkyblockManager().getBannedMembersInIsland(this);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Players getMember(UUID mojangId) {
        return this.plugin.getInterneAPI().getSkyblockManager().getMemberInIsland(this, mojangId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Players getMember(String playerName) {
        return this.plugin.getInterneAPI().getSkyblockManager().getMemberInIsland(this, playerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeMember(Players oldMember) {
        return this.plugin.getInterneAPI().getSkyblockManager().deleteMember(this, oldMember);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateMember(Players member) {
        return this.plugin.getInterneAPI().getSkyblockManager().updateMember(this, member);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Position getPosition() {
        return this.position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxMembers() {
        int value = this.plugin.getInterneAPI().getSkyblockManager().getMaxMemberInIsland(this);
        return (value == -1) ? this.maxMemberInIsland : value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setMaxMembers(int newMax) {
        return this.plugin.getInterneAPI().getSkyblockManager().setMaxMemberInIsland(this, newMax);
    }

    @Override
    public final CompiledPermissions getCompiledPermissions() {
        CompiledPermissions local = this.compiledPermissions;
        if (local != null) return local;

        synchronized (this) {
            local = this.compiledPermissions;
            if (local != null) return local;

            PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();

            var query = Skyllia.getInstance()
                    .getInterneAPI()
                    .getIslandQuery()
                    .getIslandPermissionQuery();

            CompiledPermissions loaded = null;
            if (query != null) {
                loaded = query.loadCompiled(getId(), registry);
            }

            local = (loaded != null) ? loaded : new CompiledPermissions(registry);
            this.compiledPermissions = local;
            return local;
        }
    }

    public final void invalidateCompiledPermissions() {
        this.compiledPermissions = null;
    }
}
