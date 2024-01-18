package fr.euphyllia.skyllia.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommandCacheExecution {

    private static final ConcurrentHashMap<UUID, List<String>> commandAlreadyExecution = new ConcurrentHashMap<>();

    public static boolean isAlreadyExecute(UUID playerId, String command) {
        List<String> listCmd = commandAlreadyExecution.getOrDefault(playerId, null);
        if (listCmd == null) return false;
        return listCmd.contains(command);
    }

    public static void addCommandExecute(UUID playerId, String command) {
        List<String> listCmd = commandAlreadyExecution.getOrDefault(playerId, null);
        if (listCmd == null) {
            listCmd = new ArrayList<>();
        }
        listCmd.add(command);
        commandAlreadyExecution.put(playerId, listCmd);
    }

    public static void removeCommandExec(UUID playerId, String command) {
        List<String> listCmd = commandAlreadyExecution.getOrDefault(playerId, null);
        if (listCmd == null) return;
        listCmd.remove(command);
        commandAlreadyExecution.put(playerId, listCmd);
    }
}
