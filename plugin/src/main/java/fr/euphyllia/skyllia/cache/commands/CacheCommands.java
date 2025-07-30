package fr.euphyllia.skyllia.cache.commands;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CacheCommands {

    private static final Map<UUID, List<String>> warpTabCache = new ConcurrentHashMap<>();

    static {
        Bukkit.getAsyncScheduler().runAtFixedRate(Skyllia.getInstance(), scheduledTask -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                refreshFor(player.getUniqueId());
            }
        }, 1, 60, TimeUnit.SECONDS);
    }

    public static List<String> getWarps(UUID playerId) {
        return warpTabCache.getOrDefault(playerId, Collections.emptyList());
    }

    public static void refreshFor(UUID playerId) {
        SkylliaAPI.getIslandByPlayerId(playerId).thenAccept(island -> {
            if (island == null) {
                warpTabCache.remove(playerId);
                return;
            }
            List<WarpIsland> warps = island.getWarps();
            if (warps == null) {
                warpTabCache.remove(playerId);
                return;
            }
            List<String> names = warps.stream().map(WarpIsland::warpName).toList();
            warpTabCache.put(playerId, names);
        });
    }

    public static void invalidateAll() {
        warpTabCache.clear();
    }

    public record CreateCacheCommandsTabs(CommandSender commandSender, String key) {
    }
}


