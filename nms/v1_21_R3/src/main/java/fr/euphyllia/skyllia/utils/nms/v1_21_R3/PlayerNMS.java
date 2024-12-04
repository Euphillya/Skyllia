package fr.euphyllia.skyllia.utils.nms.v1_21_R3;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlayerNMS extends fr.euphyllia.skyllia.api.utils.nms.PlayerNMS {

    public void setOwnWorldBorder(JavaPlugin main, Player player, @NotNull Location centerBorder, double borderSize, int warningBlocks, int warningTime) {
        player.getScheduler().execute(main, () -> {
            final net.minecraft.world.level.border.WorldBorder worldBorderPlayer = new net.minecraft.world.level.border.WorldBorder();
            worldBorderPlayer.world = ((org.bukkit.craftbukkit.CraftWorld) centerBorder.getWorld()).getHandle();
            worldBorderPlayer.setCenter(centerBorder.getBlockX(), centerBorder.getBlockZ());
            worldBorderPlayer.setSize(borderSize);
            worldBorderPlayer.setWarningBlocks(warningBlocks);
            worldBorderPlayer.setWarningTime(warningTime);
            final net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket updateWorldBorderPacket = new net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket(worldBorderPlayer);
            org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer = ((org.bukkit.craftbukkit.entity.CraftPlayer) player);
            craftPlayer.getHandle().connection.send(updateWorldBorderPacket);
        }, null, 1L);
    }


}
