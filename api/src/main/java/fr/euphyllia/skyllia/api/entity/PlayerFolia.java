package fr.euphyllia.skyllia.api.entity;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Provides utility methods for managing player actions in Folia.
 */
public class PlayerFolia {

    /**
     * Sets the game mode for the specified player.
     *
     * @param player The player whose game mode is to be set.
     * @param gameMode The new game mode for the player.
     */
    public static void setGameMode(Player player, GameMode gameMode) {
        player.getScheduler().execute(SkylliaAPI.getPlugin(), () -> player.setGameMode(gameMode), null, 0L);
    }
}
