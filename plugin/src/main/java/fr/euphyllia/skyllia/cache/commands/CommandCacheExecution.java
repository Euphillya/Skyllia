package fr.euphyllia.skyllia.cache.commands;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommandCacheExecution {

    private static final Map<UUID, Set<String>> COMMAND_CACHE = new ConcurrentHashMap<>();

    public static boolean isAlreadyExecute(UUID uuid, String command) {
        Set<String> commands = COMMAND_CACHE.getOrDefault(uuid, null);
        return (commands != null && commands.contains(command));
    }

    public static void addCommandExecute(UUID uuid, String command) {
        COMMAND_CACHE.compute(uuid, (key, oldSet) -> {
            if (oldSet == null) {
                oldSet = ConcurrentHashMap.newKeySet();
            }
            oldSet.add(command);
            return oldSet;
        });
    }

    public static void removeCommandExec(UUID uuid, String command) {
        COMMAND_CACHE.computeIfPresent(uuid, (key, oldSet) -> {
            oldSet.remove(command);
            return oldSet;
        });
    }
}