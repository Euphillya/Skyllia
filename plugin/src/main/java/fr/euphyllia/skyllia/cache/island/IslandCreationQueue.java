package fr.euphyllia.skyllia.cache.island;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.commands.common.subcommands.CreateSubCommand;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IslandCreationQueue {

    private static final Queue<IslandCreationRequest> creationQueue = new ConcurrentLinkedQueue<>();
    private static final Set<UUID> queuedPlayers = ConcurrentHashMap.newKeySet();
    private static boolean isProcessing = false;

    public static synchronized void queuePlayer(Player player, String[] args) {
        UUID uuid = player.getUniqueId();

        if (queuedPlayers.contains(uuid)) {
            int index = 1;
            for (IslandCreationRequest req : creationQueue) {
                if (req.uuid().equals(uuid)) {
                    ConfigLoader.language.sendMessage(player, "island.create.already-in-queue", Map.of("%position%", String.valueOf(index)));
                    return;
                }
                index++;
            }
            return;
        }

        creationQueue.add(new IslandCreationRequest(uuid, args));
        queuedPlayers.add(uuid);
        ConfigLoader.language.sendMessage(player, "island.create.queued", Map.of("%position%", String.valueOf(creationQueue.size())));

        processNext();
    }

    private static synchronized void processNext() {
        if (isProcessing || creationQueue.isEmpty()) return;

        creationQueue.removeIf(req -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(req.uuid());
            boolean shouldRemove = !p.isOnline();
            if (shouldRemove) {
                queuedPlayers.remove(req.uuid());
            }
            return shouldRemove;
        });

        if (creationQueue.isEmpty()) {
            isProcessing = false;
            return;
        }

        isProcessing = true;
        IslandCreationRequest request = creationQueue.poll();
        if (request != null) {
            queuedPlayers.remove(request.uuid());
        }
        Player player = Bukkit.getPlayer(request.uuid());

        int pos = 1;
        for (IslandCreationRequest req : creationQueue) {
            Player target = Bukkit.getPlayer(req.uuid());
            if (target != null && target.isOnline()) {
                ConfigLoader.language.sendMessage(
                        target,
                        "island.create.position-update",
                        Map.of("%position%", String.valueOf(pos))
                );
            }
            pos++;
        }

        if (player == null || !player.isOnline()) {
            isProcessing = false;
            Bukkit.getAsyncScheduler().runNow(Skyllia.getInstance(), scheduledTask -> IslandCreationQueue.processNext());
            return;
        }

        Bukkit.getAsyncScheduler().runNow(Skyllia.getInstance(), (asyncCreateIsland) -> {
            new CreateSubCommand().runCreateIsland(Skyllia.getInstance(), player, request.args())
                    .whenComplete((result, throwable) -> {
                        isProcessing = false;
                        Bukkit.getAsyncScheduler().runNow(Skyllia.getInstance(), nextCreateIsland -> IslandCreationQueue.processNext());
                    });
        });
    }

    public static synchronized boolean isQueued(UUID uuid) {
        return queuedPlayers.contains(uuid);
    }

    private record IslandCreationRequest(UUID uuid, String[] args) {
    }
}
