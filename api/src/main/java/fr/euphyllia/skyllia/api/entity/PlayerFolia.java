package fr.euphyllia.skyllia.api.entity;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerFolia {

    public static void setGameMode(Player player, GameMode gameMode) {
        player.getScheduler().execute(SkylliaAPI.getPlugin(), () -> player.setGameMode(gameMode), null, 0L);
    }
}
