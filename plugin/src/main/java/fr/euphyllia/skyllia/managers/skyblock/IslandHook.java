package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.event.SkyblockChangeSizeEvent;
import fr.euphyllia.skyllia.api.event.SkyblockCreateWarpEvent;
import fr.euphyllia.skyllia.api.event.SkyblockDeleteEvent;
import fr.euphyllia.skyllia.api.event.SkyblockDeleteWarpEvent;
import fr.euphyllia.skyllia.api.permissions.CompiledPermissions;
import fr.euphyllia.skyllia.api.permissions.IslandFlags;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.HeightType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link Island} for managing island data and operations.
 */
public class IslandHook extends Island {

    /**
     * Sentinel value stored in the height cache to mean "no custom limit set".
     * Using Integer cache means null can't be stored in ConcurrentHashMap,
     * so we use this constant instead.
     */
    private static final int HEIGHT_NOT_SET = Integer.MIN_VALUE;

    private final Skyllia plugin;
    private final UUID islandId;
    private final Timestamp createDate;
    private final RegionCoordinate position;
    private final int maxMemberInIsland;
    private final Map<World, Location> islandCenterLocations;
    private final ConcurrentHashMap<String, Integer> buildMinHeightCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> buildMaxHeightCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, IslandFlags> islandFlagsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bounds> boundsCache = new ConcurrentHashMap<>();
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
                      RegionCoordinate position,
                      double size,
                      Timestamp date) {
        this.plugin = Skyllia.getInstance();
        this.islandId = islandId;
        this.createDate = date;
        this.position = position;
        this.maxMemberInIsland = maxMembers;
        this.islandSize = size;
        this.islandCenterLocations = new ConcurrentHashMap<>();
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
        boolean isUpdated = this.plugin.getInterneAPI()
                .getSkyblockManager()
                .setSizeIsland(this, newSize);

        if (isUpdated) {
            boundsCache.clear();
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
    public boolean removeMember(Players oldMember, RemovalCause cause) {
        return this.plugin.getInterneAPI().getSkyblockManager().deleteMember(this, oldMember, cause);
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
    public RegionCoordinate getRegionCoordinate() {
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

    @Override
    public final void invalidateCompiledPermissions() {
        this.compiledPermissions = null;
    }

    @Override
    public IslandFlags getIslandFlags(String worldName) {
        IslandFlags local = islandFlagsCache.get(worldName);
        if (local != null) return local;

        synchronized (this) {
            local = islandFlagsCache.get(worldName);
            if (local != null) return local;

            var flagRegistry = SkylliaAPI.getFlagRegistry();

            var query = Skyllia.getInstance()
                    .getInterneAPI()
                    .getIslandQuery()
                    .getIslandPermissionQuery();

            IslandFlags loaded = null;
            if (query != null) {
                loaded = query.loadIslandFlags(getId(), flagRegistry, worldName);
            }

            local = (loaded != null) ? loaded : new IslandFlags(flagRegistry);
            islandFlagsCache.put(worldName, local);
            return local;
        }
    }

    @Override
    public void invalidateIslandFlags(String worldName) {
        islandFlagsCache.remove(worldName);
    }

    @Override
    public Location getCenterLocation(World world) {
        if (islandCenterLocations.isEmpty()) {
            List<Location> stored = this.plugin.getInterneAPI()
                    .getSkyblockManager()
                    .getCenterLocations(this);
            for (Location loc : stored) {
                if (loc.getWorld() != null) {
                    islandCenterLocations.put(loc.getWorld(), loc);
                }
            }
        }
        Location location = islandCenterLocations.get(world);
        if (location != null) return location;

        Location fallback = RegionHelper.getCenterRegion(world, this.position.x(), this.position.z());
        fallback.setY(64.0);
        setCenterLocation(fallback);
        return fallback;
    }

    @Override
    public void setCenterLocation(Location location) {
        this.islandCenterLocations.put(location.getWorld(), location);
        boundsCache.remove(location.getWorld().getName());
        this.plugin.getInterneAPI().getSkyblockManager().updateCenterLocation(this, location);
    }

    @Override
    public @Nullable Integer getBuildMinHeight(String worldName) {
        Integer cached = buildMinHeightCache.get(worldName);
        if (cached != null) {
            return cached == HEIGHT_NOT_SET ? null : cached;
        }
        Integer db = this.plugin.getInterneAPI()
                .getIslandQuery()
                .getIslandBuildHeightQuery()
                .getMinHeight(this, worldName);
        buildMinHeightCache.put(worldName, db != null ? db : HEIGHT_NOT_SET);
        return db;
    }

    @Override
    public @Nullable Integer getBuildMaxHeight(String worldName) {
        Integer cached = buildMaxHeightCache.get(worldName);
        if (cached != null) {
            return cached == HEIGHT_NOT_SET ? null : cached;
        }
        Integer db = this.plugin.getInterneAPI()
                .getIslandQuery()
                .getIslandBuildHeightQuery()
                .getMaxHeight(this, worldName);
        buildMaxHeightCache.put(worldName, db != null ? db : HEIGHT_NOT_SET);
        return db;
    }

    @Override
    public boolean setBuildHeight(String worldName, HeightType type, int value) {
        boolean ok;
        if (type == HeightType.MIN) {
            ok = this.plugin.getInterneAPI()
                    .getIslandQuery()
                    .getIslandBuildHeightQuery()
                    .setMinHeight(this, worldName, value);
            if (ok) buildMinHeightCache.put(worldName, value);
        } else {
            ok = this.plugin.getInterneAPI()
                    .getIslandQuery()
                    .getIslandBuildHeightQuery()
                    .setMaxHeight(this, worldName, value);
            if (ok) buildMaxHeightCache.put(worldName, value);
        }
        return ok;
    }

    @Override
    public Location getMinimumPoint(World world) {
        Location center = getCenterLocation(world);

        int minY = getBuildMinHeight(world.getName()) != null
                ? getBuildMinHeight(world.getName())
                : world.getMinHeight();

        double halfSize = (getSize() - 1D) / 2D;

        return new Location(
                world,
                center.getBlockX() - halfSize,
                minY,
                center.getBlockZ() - halfSize
        );
    }

    @Override
    public Location getMaximumPoint(World world) {
        Location center = getCenterLocation(world);

        int maxY = getBuildMaxHeight(world.getName()) != null
                ? getBuildMaxHeight(world.getName())
                : world.getMaxHeight() - 1;

        double halfSize = (getSize() - 1D) / 2D;

        return new Location(
                world,
                center.getBlockX() + halfSize,
                maxY,
                center.getBlockZ() + halfSize
        );

    }

    @Override
    public boolean isInside(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        return isInside(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public boolean isInside(@NotNull World world, int blockX, int blockY, int blockZ) {
        String worldName = world.getName();
        Bounds b = boundsCache.get(worldName);
        if (b == null) {
            b = computeBounds(world);
            boundsCache.put(worldName, b);
        }
        return b.contains(blockX, blockY, blockZ);
    }

    private Bounds computeBounds(World world) {
        Location center = getCenterLocation(world);
        String worldName = world.getName();
        double halfSize = (getSize() - 1D) / 2D;

        int cx = center.getBlockX();
        int cz = center.getBlockZ();

        Integer customMin = getBuildMinHeight(worldName);
        int minY = (customMin != null) ? customMin : world.getMinHeight();

        Integer customMax = getBuildMaxHeight(worldName);
        int maxY = (customMax != null) ? customMax : world.getMaxHeight() - 1;

        return new Bounds(
                (int) Math.floor(cx - halfSize),
                (int) Math.floor(cx + halfSize),
                (int) Math.floor(cz - halfSize),
                (int) Math.floor(cz + halfSize),
                minY, maxY
        );
    }

    private record Bounds(int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        boolean contains(int x, int y, int z) {
            return x >= minX && x <= maxX
                    && z >= minZ && z <= maxZ
                    && y >= minY && y <= maxY;
        }
    }
}
