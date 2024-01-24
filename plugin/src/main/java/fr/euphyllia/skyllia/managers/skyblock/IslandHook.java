package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.event.SkyblockChangeAccessEvent;
import fr.euphyllia.skyllia.api.event.SkyblockCreateWarpEvent;
import fr.euphyllia.skyllia.api.event.SkyblockDeleteEvent;
import fr.euphyllia.skyllia.api.event.SkyblockDeleteWarpEvent;
import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.IslandType;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
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
    private final UUID ownerId;
    private final IslandType islandType;
    private double size;

    /**
     * @param main       Plugin skyllia
     * @param islandType Type Island (config.toml)
     * @param islandId   Island ID
     * @param ownerId    Owner Island
     * @param position   Position X/Z region File
     * @param size       Rayon Island
     * @param date       Create Date
     */
    public IslandHook(Main main, IslandType islandType, UUID islandId, UUID ownerId, Position position, double size, Timestamp date) throws MaxIslandSizeExceedException {
        this.plugin = main;
        this.islandType = islandType;
        this.islandId = islandId;
        this.ownerId = ownerId;
        this.createDate = date;
        this.position = position;
        if (size >= (511 * ConfigToml.regionDistance) || size <= 1) {
            throw new MaxIslandSizeExceedException("The size of the island exceeds the permitted limit! Must be between 2 and %s.".formatted((511 * ConfigToml.regionDistance)));
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
    public void setSize(double rayon) throws MaxIslandSizeExceedException {
        if (this.size >= (511 * ConfigToml.regionDistance) || this.size <= 1) {
            throw new MaxIslandSizeExceedException("The size of the island exceeds the permitted limit! Must be between 2 and %s.".formatted(511 * ConfigToml.regionDistance) ); // Fix https://github.com/Euphillya/skyllia/issues/9
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
        this.plugin.getInterneAPI().getSkyblockManager().disableIsland(this, disable).join();

        return disable;
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
    public UUID getOwnerId() {
        return this.ownerId;
    }

    @Override
    public void setOwnerId(UUID ownerId) {
        // Todo ? a faire
        throw new UnsupportedOperationException("pas encore implémenter");
    }

    @Override
    public boolean updatePermissionIsland(PermissionsType permissionsType, RoleType roleType, long permissions) {
        return this.plugin.getInterneAPI().getSkyblockManager().updatePermissionIsland(this, permissionsType, roleType, permissions).join();
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public IslandType getIslandType() {
        return this.islandType;
    }

    @Override
    public void setIslandType(IslandType islandType) {
        throw new UnsupportedOperationException("pas encore implémenter");
    }
}
