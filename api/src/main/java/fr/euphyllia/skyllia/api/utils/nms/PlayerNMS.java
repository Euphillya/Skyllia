package fr.euphyllia.skyllia.api.utils.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerNMS {
    /**
     * Create a personal border just for the player.
     *
     * @param player        Player
     * @param centerBorder  The center of the border
     * @param borderSize    Border size
     * @param warningBlocks Sets the warning distance that causes the screen to be tinted red when the player is within the specified number of blocks from the border.
     * @param warningTime   Sets the warning time that causes the screen to be tinted red when a contracting border will reach the player within the specified time.
     */
    public abstract void setOwnWorldBorder(JavaPlugin main, Player player, @NotNull Location centerBorder, double borderSize, int warningBlocks, int warningTime);
}
