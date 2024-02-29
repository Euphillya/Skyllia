package fr.euphyllia.skyllia.cache.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

    public record CreateCacheCommandsTabs(CommandSender commandSender, String key){};
}


