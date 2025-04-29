package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.event.*;
import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.cache.island.IslandClosedCache;
import fr.euphyllia.skyllia.cache.island.PlayersInIslandCache;
import fr.euphyllia.skyllia.cache.island.WarpsInIslandCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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

    /**
     * Constructs a new {@code IslandHook} instance.
     *
     * @param Skyllia    The {@link Skyllia} plugin instance.
     * @param islandId   The UUID of the island.
     * @param maxMembers The maximum number of members allowed on the island.
     * @param position   The region-based position of the island (X/Z).
     * @param size       The radius (size) of the island.
     * @param date       The creation date, or {@code null} if unknown.
     * @throws MaxIslandSizeExceedException If the specified size is out of the allowed range.
     */
    public IslandHook(Skyllia Skyllia,
                      UUID islandId,
                      int maxMembers,
                      Position position,
                      double size,
                      Timestamp date) throws MaxIslandSizeExceedException {
        this.plugin = Skyllia;
        this.islandId = islandId;
        this.createDate = date;
        this.position = position;
        this.maxMemberInIsland = maxMembers;

        // Validate the island size
        if (size >= (255 * ConfigLoader.general.getRegionDistance()) || size <= 1) {
            throw new MaxIslandSizeExceedException("The size of the island exceeds the permitted limit! "
                    + "Must be between 2 and " + (255 * ConfigLoader.general.getRegionDistance()) + ".");
        }
        this.islandSize = size;
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
    public boolean setSize(double newSize) throws MaxIslandSizeExceedException {
        if (newSize >= (255 * ConfigLoader.general.getRegionDistance()) || newSize <= 1) {
            throw new MaxIslandSizeExceedException(
                    "The size of the island exceeds the permitted limit! Must be between 2 and "
                            + (255 * ConfigLoader.general.getRegionDistance()) + ".");
        }

        this.islandSize = newSize;
        // Update in database
        boolean isUpdated = this.plugin.getInterneAPI()
                .getSkyblockManager()
                .setSizeIsland(this, newSize)
                .join();

        if (isUpdated) {
            // Fire event asynchronously
            Bukkit.getAsyncScheduler().runNow(plugin, task ->
                    Bukkit.getPluginManager().callEvent(new SkyblockChangeSizeEvent(this, newSize))
            );
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable CopyOnWriteArrayList<WarpIsland> getWarps() {
        return WarpsInIslandCache.getWarpsCached(this.islandId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable WarpIsland getWarpByName(String name) {
        return this.plugin.getInterneAPI().getSkyblockManager().getWarpIslandByName(this.islandId, name).join();
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
        boolean success = this.plugin.getInterneAPI().getSkyblockManager()
                .addWarpsIsland(this.islandId, event.getWarpName(), event.getWarpLocation()).join();
        if (success) {
            WarpsInIslandCache.invalidate(this.islandId);
        }
        return success;
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
        boolean success = this.plugin.getInterneAPI().getSkyblockManager()
                .delWarpsIsland(this.islandId, event.getWarpName()).join();
        if (success) {
            WarpsInIslandCache.invalidate(this.islandId);
        }
        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisable() {
        return this.plugin.getInterneAPI().getSkyblockManager().isDisabledIsland(this).join();
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
        return this.plugin.getInterneAPI().getSkyblockManager().disableIsland(this, disable).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivateIsland() {
        return this.plugin.getInterneAPI().getSkyblockManager().isPrivateIsland(this).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setPrivateIsland(boolean privateIsland) {
        SkyblockChangeAccessEvent event = new SkyblockChangeAccessEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        IslandClosedCache.invalidateIsland(this.getId());
        return this.plugin.getInterneAPI().getSkyblockManager().setPrivateIsland(this, privateIsland).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CopyOnWriteArrayList<Players> getMembers() {
        return this.plugin.getInterneAPI().getSkyblockManager().getMembersInIsland(this).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CopyOnWriteArrayList<Players> getMembersCached() {
        return PlayersInIslandCache.getPlayersCached(this.islandId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Players getMember(UUID mojangId) {
        return this.plugin.getInterneAPI().getSkyblockManager().getMemberInIsland(this, mojangId).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Players getMember(String playerName) {
        return this.plugin.getInterneAPI().getSkyblockManager().getMemberInIsland(this, playerName).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeMember(Players oldMember) {
        return this.plugin.getInterneAPI().getSkyblockManager().deleteMember(this, oldMember).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateMember(Players member) {
        return this.plugin.getInterneAPI().getSkyblockManager().updateMember(this, member).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updatePermission(PermissionsType permissionsType, RoleType roleType, long permissions) {
        boolean isUpdated = this.plugin.getInterneAPI().getSkyblockManager()
                .updatePermissionIsland(this, permissionsType, roleType, permissions).join();
        if (isUpdated) {
            Bukkit.getAsyncScheduler().runNow(plugin, task ->
                    Bukkit.getPluginManager().callEvent(new SkyblockChangePermissionEvent(this, permissionsType, roleType, permissions))
            );
            return true;
        }
        return false;
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
        int value = this.plugin.getInterneAPI().getSkyblockManager().getMaxMemberInIsland(this).join();
        return (value == -1) ? this.maxMemberInIsland : value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setMaxMembers(int newMax) {
        return this.plugin.getInterneAPI().getSkyblockManager().setMaxMemberInIsland(this, newMax).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateGamerule(long gameRuleValue) {
        boolean isUpdated = this.plugin.getInterneAPI().getSkyblockManager().updateGamerule(this, gameRuleValue).join();
        if (isUpdated) {
            Bukkit.getAsyncScheduler().runNow(plugin, task ->
                    Bukkit.getPluginManager().callEvent(new SkyblockChangeGameRuleEvent(this, gameRuleValue))
            );
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getGameRulePermission() {
        return this.plugin.getInterneAPI().getSkyblockManager().getGameRulePermission(this).join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPermission(PermissionsType permissionsType, RoleType roleType) {
        return this.plugin.getInterneAPI()
                .getSkyblockManager()
                .getPermissionIsland(this.islandId, permissionsType, roleType)
                .join()
                .permission();
    }
}
