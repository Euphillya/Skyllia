package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.players.PlayerTeleportSpawnEvent;
import fr.euphyllia.skyllia.api.hooks.SpawnHook;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerUtils {

    public static void teleportPlayerSpawn(Player player) {
        if (!ConfigLoader.general.isSpawnEnabled()) return;
        player.getScheduler().execute(SkylliaAPI.getPlugin(), () -> {
            if (!player.isOnline()) return;

            SpawnHook spawnHook = Skyllia.getInstance().getInterneAPI().getSpawnHook();
            if (spawnHook != null && spawnHook.isAvailable()) {
                Location hookLocation = spawnHook.getSpawnLocation(player);
                if (hookLocation != null) {
                    PlayerTeleportSpawnEvent playerTeleportSpawnEvent = new PlayerTeleportSpawnEvent(player, hookLocation);
                    Bukkit.getPluginManager().callEvent(playerTeleportSpawnEvent);
                    if (playerTeleportSpawnEvent.isCancelled()) {
                        return;
                    }
                    player.teleportAsync(playerTeleportSpawnEvent.getFinalLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    return;
                }
            }

            Location location = ConfigLoader.general.getSpawnLocation();
            if (location == null || location.getWorld() == null)
                location = Bukkit.getWorlds().getFirst().getSpawnLocation();
            PlayerTeleportSpawnEvent playerTeleportSpawnEvent = new PlayerTeleportSpawnEvent(player, location);
            Bukkit.getPluginManager().callEvent(playerTeleportSpawnEvent);
            if (playerTeleportSpawnEvent.isCancelled()) {
                return;
            }
            player.teleportAsync(playerTeleportSpawnEvent.getFinalLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }, null, 1L);

    }

    // TODO: i18n support and optimise the code
    public static boolean hasPermission(Player player, String key) {
        var result = player.hasPermission(key);

        if (!ConfigLoader.general.isDebugPermission())
            return result;

        var text = Component.text()
                .append(Component.text("player "))
                .append(player.name());

        if (result) {
            text.append(Component.text(" HAVE ").color(NamedTextColor.GREEN));
        } else {
            text.append(Component.text(" DOESN'T HAVE ").color(NamedTextColor.RED));
        }

        text.append(Component.text(key)).append(Component.text(" permission"));

        Bukkit.getConsoleSender().sendMessage(text.build());

        return result;
    }
}
