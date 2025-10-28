package fr.euphyllia.skyllia.cache.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandCacheExecution {

    private static final Cache<UUID, Set<String>> COMMAND_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(5000)
            .recordStats()
            .build();

    public static boolean isAlreadyExecute(UUID uuid, String command) {
        Set<String> commands = COMMAND_CACHE.getIfPresent(uuid);
        return (commands != null && commands.contains(command));
    }

    public static void addCommandExecute(UUID uuid, String command) {
        COMMAND_CACHE.asMap().compute(uuid, (key, oldSet) -> {
            if (oldSet == null) {
                oldSet = new HashSet<>();
            }
            oldSet.add(command);
            return oldSet;
        });
    }

    public static void removeCommandExec(UUID uuid, String command) {
        COMMAND_CACHE.asMap().computeIfPresent(uuid, (key, oldSet) -> {
            oldSet.remove(command);
            return oldSet;
        });
    }

    public static void invalidateAll() {
        COMMAND_CACHE.invalidateAll();
    }
}