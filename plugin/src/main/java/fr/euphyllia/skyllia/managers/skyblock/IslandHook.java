package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.event.*;
import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.cache.PlayersInIslandCache;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.bukkit.Bukkit;
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
    private final int maxMemberInIsland;
    private double size;

    /**
     * @param main       Plugin skyllia
     * @param islandId   Island ID
     * @param maxMembers Max Players In Island
     * @param position   Position X/Z region File
     * @param size       Rayon Island
     * @param date       Create Date
     */
    public IslandHook(Main main, UUID islandId, int maxMembers, Position position, double size, Timestamp date) throws MaxIslandSizeExceedException {
        this.plugin = main;
        this.maxMemberInIsland = maxMembers;
        this.islandId = islandId;
        this.createDate = date;
        this.position = position;
        if (size >= (255 * ConfigToml.regionDistance) || size <= 1) {
            throw new MaxIslandSizeExceedException("The size of the island exceeds the permitted limit! Must be between 2 and %s.".formatted((255 * ConfigToml.regionDistance)));
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
    public double getSize() {
        return this.size;
    }

    @Override
    public boolean setSize(double rayon) throws MaxIslandSizeExceedException {
        if (rayon >= (255 * ConfigToml.regionDistance) || rayon <= 1) {
            throw new MaxIslandSizeExceedException("The size of the island exceeds the permitted limit! Must be between 2 and %s.".formatted(255 * ConfigToml.regionDistance)); // Fix https://github.com/Euphillya/skyllia/issues/9
        }
        this.size = rayon;
        if (Boolean.TRUE.equals(this.plugin.getInterneAPI().getSkyblockManager().setSizeIsland(this, rayon).join())) {
            Bukkit.getAsyncScheduler().runNow(plugin, (task) -> Bukkit.getPluginManager().callEvent(new SkyblockChangeSizeEvent(this, rayon)));
            return true;
        } else {
            return false;
        }
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
    public boolean addWarps(String name, Location loc, boolean ignoreEvent) {
        SkyblockCreateWarpEvent skyblockCreateWarpEvent = new SkyblockCreateWarpEvent(this, name, loc);
        if (!ignoreEvent) {
            Bukkit.getPluginManager().callEvent(skyblockCreateWarpEvent);
            if (skyblockCreateWarpEvent.isCancelled()) {
                return false;
            }
        }
        return this.plugin.getInterneAPI().getSkyblockManager().addWarpsIsland(this, skyblockCreateWarpEvent.getWarpName(), skyblockCreateWarpEvent.getWarpLocation()).join();
    }

    @Override
    public boolean delWarp(String name) {
        SkyblockDeleteWarpEvent skyblockCreateWarpEvent = new SkyblockDeleteWarpEvent(this, name);
        Bukkit.getPluginManager().callEvent(skyblockCreateWarpEvent);
        if (skyblockCreateWarpEvent.isCancelled()) {
            return false;
        }
        return this.plugin.getInterneAPI().getSkyblockManager().delWarpsIsland(this, skyblockCreateWarpEvent.getWarpName()).join();
    }

    @Override
    public boolean isDisable() {
        return this.plugin.getInterneAPI().getSkyblockManager().isDisabledIsland(this).join();
    }

    @Override
    public boolean setDisable(boolean disable) {
        SkyblockDeleteEvent skyblockRemoveEvent = new SkyblockDeleteEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(skyblockRemoveEvent);
        if (skyblockRemoveEvent.isCancelled()) {
            return false;
        }
        return this.plugin.getInterneAPI().getSkyblockManager().disableIsland(this, disable).join();
    }

    @Override
    public boolean isPrivateIsland() {
        return this.plugin.getInterneAPI().getSkyblockManager().isPrivateIsland(this).join();
    }

    @Override
    public boolean setPrivateIsland(boolean privateIsland) {
        SkyblockChangeAccessEvent skyblockChangeAccessEvent = new SkyblockChangeAccessEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(skyblockChangeAccessEvent);
        if (skyblockChangeAccessEvent.isCancelled()) {
            return false;
        }
        return this.plugin.getInterneAPI().getSkyblockManager().setPrivateIsland(this, privateIsland).join();
    }

    @Override
    public CopyOnWriteArrayList<Players> getMembers() {
        return this.plugin.getInterneAPI().getSkyblockManager().getMembersInIsland(this).join();
    }

    /**
     * Gets the list of members on the island.
     *
     * @return A list of members.
     */
    @Override
    public CopyOnWriteArrayList<Players> getMembersCached() {
        return PlayersInIslandCache.getPlayersCached(this.islandId);
    }

    @Override
    public Players getMember(UUID mojangId) {
        return this.plugin.getInterneAPI().getSkyblockManager().getMemberInIsland(this, mojangId).join();
    }

    @Override
    public @Nullable Players getMember(String playerName) {
        return this.plugin.getInterneAPI().getSkyblockManager().getMemberInIsland(this, playerName).join();
    }

    @Override
    public boolean removeMember(Players oldMember) {
        return this.plugin.getInterneAPI().getSkyblockManager().deleteMember(this, oldMember).join();
    }

    @Override
    public boolean updateMember(Players member) {
        return this.plugin.getInterneAPI().getSkyblockManager().updateMember(this, member).join();
    }

    @Override
    public boolean updatePermission(PermissionsType permissionsType, RoleType roleType, long permissions) {
        boolean isUpdated = this.plugin.getInterneAPI().getSkyblockManager().updatePermissionIsland(this, permissionsType, roleType, permissions).join();
        if (isUpdated) {
            Bukkit.getAsyncScheduler().runNow(plugin, task -> Bukkit.getPluginManager().callEvent(new SkyblockChangePermissionEvent(this, permissionsType, roleType, permissions)));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public int getMaxMembers() {
        int value = this.plugin.getInterneAPI().getSkyblockManager().getMaxMemberInIsland(this).join();
        if (value == -1) {
            return this.maxMemberInIsland;
        } else {
            return value;
        }
    }

    @Override
    public boolean setMaxMembers(int newMax) {
        return this.plugin.getInterneAPI().getSkyblockManager().setMaxMemberInIsland(this, newMax).join();
    }

    @Override
    public boolean updateGamerule(long gameRuleIsland) {
        boolean isUpdated = this.plugin.getInterneAPI().getSkyblockManager().updateGamerule(this, gameRuleIsland).join();
        if (isUpdated) {
            Bukkit.getAsyncScheduler().runNow(plugin, task -> Bukkit.getPluginManager().callEvent(new SkyblockChangeGameRuleEvent(this, gameRuleIsland)));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long getGameRulePermission() {
        return this.plugin.getInterneAPI().getSkyblockManager().getGameRulePermission(this).join();
    }

    @Override
    public long getPermission(PermissionsType permissionsType, RoleType roleType) {
        return this.plugin.getInterneAPI().getSkyblockManager().getPermissionIsland(this.islandId, permissionsType, roleType).join().permission();
    }
}
