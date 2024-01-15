package fr.euphyllia.skyfolia.utils;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.api.exceptions.UnsupportedMinecraftVersionException;
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

    public static void updateChunk(Main main, Player player, int chunkX, int chunkZ) throws UnsupportedMinecraftVersionException {
        final String versionMC = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        switch (versionMC) {
            case "v1_19_R3" ->
                    fr.euphyllia.skyfolia.utils.nms.v1_19_R3.PlayerNMS.refreshPlayerChunk(main, player, chunkX, chunkZ);
            case "v1_20_R1" ->
                    fr.euphyllia.skyfolia.utils.nms.v1_20_R1.PlayerNMS.refreshPlayerChunk(main, player, chunkX, chunkZ);
            case "v1_20_R2" ->
                    fr.euphyllia.skyfolia.utils.nms.v1_20_R2.PlayerNMS.refreshPlayerChunk(main, player, chunkX, chunkZ);
            default ->
                    throw new UnsupportedMinecraftVersionException("Version %s not supported !".formatted(versionMC));
        }
    }

}
