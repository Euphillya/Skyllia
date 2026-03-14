package fr.euphyllia.skyllia.cache.island;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.commands.common.subcommands.CreateSubCommand;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class IslandCreationQueue {

    private static final Queue<IslandCreationRequest> creationQueue = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public static synchronized void queuePlayer(Player player, String[] args) {
        UUID uuid = player.getUniqueId();

        int index = 1;
        for (IslandCreationRequest req : creationQueue) {
            if (req.uuid().equals(player.getUniqueId())) {
                ConfigLoader.language.sendMessage(player, "island.create.already-in-queue", Map.of("%position%", String.valueOf(index)));
                return;
            }
            index++;
        }


        creationQueue.add(new IslandCreationRequest(uuid, args));
        ConfigLoader.language.sendMessage(player, "island.create.queued", Map.of("%position%", String.valueOf(creationQueue.size())));

        processNext();
    }

    private static synchronized void processNext() {
        if (isProcessing.get() || creationQueue.isEmpty()) return;

        removeOfflinePlayers();

        if (creationQueue.isEmpty()) return;

        isProcessing.set(true);
        IslandCreationRequest request = creationQueue.poll();
        if (request == null) {
            isProcessing.set(false);
            return;
        }

        Player player = Bukkit.getPlayer(request.uuid());
        broadcastPositions();

        if (player == null || !player.isOnline()) {
            isProcessing.set(false);
            processNext();
            return;
        }

        new CreateSubCommand().runCreateIsland(Skyllia.getInstance(), player, request.args())
                .whenComplete((result, throwable) -> {
                    isProcessing.set(false);
                    IslandCreationQueue.processNext();
                });
    }

    public static synchronized boolean isQueued(UUID uuid) {
        return creationQueue.stream().anyMatch(req -> req.uuid().equals(uuid));
    }

    private static void removeOfflinePlayers() {
        creationQueue.removeIf(req -> Bukkit.getPlayer(req.uuid()) == null);
    }

    private static void broadcastPositions() {
        int pos = 1;
        for (IslandCreationRequest req : creationQueue) {
            Player target = Bukkit.getPlayer(req.uuid());
            if (target != null) {
                ConfigLoader.language.sendMessage(target, "island.create.position-update", Map.of("%position%", String.valueOf(pos)));
            }
            pos++;
        }
    }

    private record IslandCreationRequest(UUID uuid, String[] args) {
    }
}
