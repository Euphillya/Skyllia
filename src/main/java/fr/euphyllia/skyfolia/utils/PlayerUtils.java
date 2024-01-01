package fr.euphyllia.skyfolia.utils;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import fr.euphyllia.skyfolia.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerUtils {

    public static void teleportPlayerSpawn(Main main, Player player) {
        player.getScheduler().run(main, scheduledTask -> {
            World world = Bukkit.getWorlds().get(0);
            try {
                EssentialsSpawn essentialsSpawn = (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
                Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                if (essentialsSpawn != null && essentialsSpawn.isEnabled() && essentials != null && essentials.isEnabled()) {
                    player.teleportAsync(essentialsSpawn.getSpawn(essentials.getUser(player.getUniqueId()).getGroup()), PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.teleportAsync(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            } catch (Exception e) {
                player.teleportAsync(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }, null);
    }

    public static void refreshPlayerChunk(Player player, int chunkX, int chunkZ) {
        org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer craftPlayer = ((org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer) player);
        final net.minecraft.world.level.chunk.LevelChunk levelChunk = craftPlayer.getHandle().level().getChunk(chunkX, chunkZ);
        final net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket refresh = new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(levelChunk, levelChunk.getLevel().getLightEngine(), null, null, true);
        craftPlayer.getHandle().connection.send(refresh);
    }


    /**
     * Create a personal border just for the player.
     *
     * @param player        Player
     * @param centerBorder  The center of the border
     * @param colorBorder   Border color: Blue/Red/Green
     * @param borderSize    Border size
     * @param warningBlocks Sets the warning distance that causes the screen to be tinted red when the player is within the specified number of blocks from the border.
     * @param warningTime   Sets the warning time that causes the screen to be tinted red when a contracting border will reach the player within the specified time.
     */
    public static void setOwnWorldBorder(Main main, Player player, @NotNull Location centerBorder, @NotNull String colorBorder, double borderSize, int warningBlocks, int warningTime) {
        player.getScheduler().run(main, scheduledTask -> {
            final net.minecraft.world.level.border.WorldBorder worldBorderPlayer = new net.minecraft.world.level.border.WorldBorder();
            worldBorderPlayer.world = ((org.bukkit.craftbukkit.v1_20_R2.CraftWorld) centerBorder.getWorld()).getHandle();
            worldBorderPlayer.setCenter(centerBorder.getBlockX(), centerBorder.getBlockZ());
            worldBorderPlayer.setSize(borderSize);
            worldBorderPlayer.setWarningBlocks(warningBlocks);
            worldBorderPlayer.setWarningTime(warningTime);
            switch (colorBorder) {
                case "RED" -> worldBorderPlayer.lerpSizeBetween(borderSize, borderSize - 1.0, 20000000L);
                case "GREEN" -> worldBorderPlayer.lerpSizeBetween(borderSize - 0.1, borderSize, 20000000L);
            }
            final net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket updateWorldBorderPacket = new net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket(worldBorderPlayer);
            org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer craftPlayer = ((org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer) player);
            craftPlayer.getHandle().connection.send(updateWorldBorderPacket);
        }, null);
    }
}
