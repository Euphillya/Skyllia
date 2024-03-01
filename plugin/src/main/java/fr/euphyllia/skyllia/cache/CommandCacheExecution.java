package fr.euphyllia.skyllia.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommandCacheExecution {

    private static final ConcurrentHashMap<UUID, List<String>> commandAlreadyExecution = new ConcurrentHashMap<>();

    public static boolean isAlreadyExecute(UUID uuid, String command) {
        List<String> listCmd = commandAlreadyExecution.getOrDefault(uuid, null);
        if (listCmd == null) return false;
        return listCmd.contains(command);
    }

    public static void addCommandExecute(UUID uuid, String command) {
        List<String> listCmd = commandAlreadyExecution.getOrDefault(uuid, null);
        if (listCmd == null) {
            listCmd = new ArrayList<>();
        }
        listCmd.add(command);
        commandAlreadyExecution.put(uuid, listCmd);
    }

    public static void removeCommandExec(UUID uuid, String command) {
        List<String> listCmd = commandAlreadyExecution.getOrDefault(uuid, null);
        if (listCmd == null) return;
        listCmd.remove(command);
        commandAlreadyExecution.put(uuid, listCmd);
    }
}
