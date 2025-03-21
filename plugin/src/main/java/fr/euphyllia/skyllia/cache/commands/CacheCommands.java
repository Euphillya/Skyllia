package fr.euphyllia.skyllia.cache.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CacheCommands {

    public static final Cache<CreateCacheCommandsTabs, Boolean> createTabCompleteCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(createCacheCommandsTabs -> {
                CommandSender sender = createCacheCommandsTabs.commandSender;
                return PermissionImp.hasPermission(sender, "skyllia.island.command.create.%s".formatted(createCacheCommandsTabs.key));
            });

    public static final Cache<UUID, List<String>> warpTabCompleteCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(playerId -> {
                List<String> warpList = new ArrayList<>();
                Island island = SkylliaAPI.getIslandByPlayerId(playerId).join();
                if (island == null) return warpList;
                List<WarpIsland> warps = island.getWarps();
                if (warps == null) return warpList;
                for (WarpIsland warpIsland : warps) {
                    warpList.add(warpIsland.warpName());
                }
                return warpList;
            });

    public static void invalidateAll() {
        createTabCompleteCache.invalidateAll();
        warpTabCompleteCache.invalidateAll();
    }

    public record CreateCacheCommandsTabs(CommandSender commandSender, String key) {
    }
}


