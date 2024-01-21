package fr.euphyllia.skyllia.utils;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import fr.euphyllia.skyllia.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

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

}
