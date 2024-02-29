package fr.euphyllia.skyllia.cache.commands;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CacheCommands {

    public static final com.google.common.cache.LoadingCache<CreateCacheCommandsTabs, Boolean> createTabCompleteCache = com.google.common.cache.CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build(
                    new com.google.common.cache.CacheLoader<>() {
                        @Override
                        public @NotNull Boolean load(@NotNull CreateCacheCommandsTabs createCacheCommandsTabs) {
                            return createCacheCommandsTabs.commandSender.hasPermission("skyllia.island.command.create.%s".formatted(createCacheCommandsTabs.key));
                        }
                    }
            );

    public static final com.google.common.cache.LoadingCache<UUID, List<String>> warpTabCompleteCache = com.google.common.cache.CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(
                    new com.google.common.cache.CacheLoader<>() {
                        @Override
                        public @NotNull List<String> load(@NotNull UUID playerId) {
                            List<String> warpList = new ArrayList<>();
                            Island island = SkylliaAPI.getIslandByPlayerId(playerId).join();
                            if (island == null) return warpList;
                            List<WarpIsland> warps = island.getWarps();
                            if (warps == null) return warpList;
                            for (WarpIsland warpIsland : warps) {
                                warpList.add(warpIsland.warpName());
                            }
                            return warpList;
                        }
                    }
            );

    public record CreateCacheCommandsTabs(CommandSender commandSender, String key) {
    }

    ;
}


