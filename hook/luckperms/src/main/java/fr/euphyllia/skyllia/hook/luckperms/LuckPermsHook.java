package fr.euphyllia.skyllia.hook.luckperms;

import fr.euphyllia.skyllia.api.hooks.PermissionHook;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LuckPermsHook implements PermissionHook {

    private static final boolean AVAILABLE =
            PermissionHook.hasClass("net.luckperms.api.LuckPerms");
    private static final Logger log = LoggerFactory.getLogger(LuckPermsHook.class);

    private final Map<UUID, Map<String, Boolean>> cache = new ConcurrentHashMap<>();

    private final LuckPerms luckPerms;

    public LuckPermsHook(Plugin plugin) {
        LuckPerms lp = null;
        if (AVAILABLE) {
            RegisteredServiceProvider<LuckPerms> reg =
                    Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (reg != null) lp = reg.getProvider();
        }
        this.luckPerms = lp;

        if (lp != null) {
            lp.getEventBus().subscribe(plugin, NodeAddEvent.class, this::onNodeAdd);
            lp.getEventBus().subscribe(plugin, NodeRemoveEvent.class, this::onNodeRemove);
        }
    }

    @Override
    public boolean isAvailable() {
        return AVAILABLE && luckPerms != null;
    }

    @Override
    public boolean hasPermission(Player player, String node) {
        UUID uuid = player.getUniqueId();

        Map<String, Boolean> perUser = cache.get(uuid);
        if (perUser != null) {
            Boolean cached = perUser.get(node);
            if (cached != null) return cached;
        }

        boolean result = luckPerms.getPlayerAdapter(Player.class)
                .getPermissionData(player)
                .checkPermission(node)
                .asBoolean();

        cache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(node, result);
        return result;
    }

    private void onNodeAdd(final NodeAddEvent e) {
        handle(e.getTarget(), e.getNode());
    }

    private void onNodeRemove(final NodeRemoveEvent e) {
        handle(e.getTarget(), e.getNode());
    }

    private void handle(PermissionHolder target, Node node) {
        if (target instanceof User user && node instanceof PermissionNode) {
            cache.remove(user.getUniqueId());
        }
    }
}
