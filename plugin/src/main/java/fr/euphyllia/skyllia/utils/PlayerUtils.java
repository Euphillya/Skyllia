package fr.euphyllia.skyllia.utils;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.event.players.PlayerTeleportSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

public class PlayerUtils {

    public static void teleportPlayerSpawn(Main main, Player player) {
        player.getScheduler().run(main, scheduledTask -> {
            Location spawnLocation = getSpawnLocationEssentials(player); // Todo Sera supprimer à l'avenir, je veux utiliser le point de dépendance possible !
            if (spawnLocation == null) {
                spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
            }
            PlayerTeleportSpawnEvent playerTeleportSpawnEvent = new PlayerTeleportSpawnEvent(player, spawnLocation);
            Bukkit.getPluginManager().callEvent(playerTeleportSpawnEvent);
            if (playerTeleportSpawnEvent.isCancelled()) {
                return;
            }
            player.teleportAsync(playerTeleportSpawnEvent.getFinalLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }, null);
    }

    @Deprecated(forRemoval = true, since = "1.0-RC4")
    private static @Nullable  Location getSpawnLocationEssentials(Player player) {
        EssentialsSpawn essentialsSpawn = (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentialsSpawn != null && essentialsSpawn.isEnabled() && essentials != null && essentials.isEnabled()) {
            return essentialsSpawn.getSpawn(essentials.getUser(player.getUniqueId()).getGroup());
        } else {
            return null;
        }
    }
}
