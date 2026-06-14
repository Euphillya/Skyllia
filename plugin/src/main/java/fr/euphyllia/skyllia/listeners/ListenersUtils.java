package fr.euphyllia.skyllia.listeners;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

public class ListenersUtils {

    private ListenersUtils() {

    }

    public static @Nullable Island checkChunkIsIsland(int chunkX, int chunkZ, Cancellable cancellable) {
        Position pos = RegionHelper.getRegionFromChunk(chunkX, chunkZ);
        Island island = SkylliaAPI.getIslandByPosition(pos);

        if (island == null) {
            cancellable.setCancelled(true);
        }
        return island;
    }


    public static void callPlayerPrepareChangeWorldSkyblockEvent(Player player, WorldConfig worldConfig, PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType, @Nullable Cancellable cancellable) {
        if (cancellable != null) cancellable.setCancelled(true);
        new PlayerPrepareChangeWorldSkyblockEvent(player, worldConfig, portalType).callEvent();
    }


    public static boolean isBlockOutsideIsland(Island island, World world, int blockX, int blockY, int blocZ, @Nullable Cancellable cancellable) {
        boolean outside = !island.isInside(world, blockX, blockY, blocZ);

        if (outside && cancellable != null) {
            cancellable.setCancelled(true);
        }

        return outside;
    }

    public static boolean isBlockOutsideIsland(Island island, Location location, @Nullable Cancellable cancellable) {
        boolean outside = !island.isInside(location);

        if (outside && cancellable != null) {
            cancellable.setCancelled(true);
        }

        return outside;
    }

    public static @Nullable Island islandAtBlock(World world, int bx, int bz) {
        if (!SkylliaAPI.isWorldSkyblock(world.getName())) return null;
        return SkylliaAPI.getIslandByChunk(bx >> 4, bz >> 4);
    }

}
