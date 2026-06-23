package fr.euphyllia.skyllia.hook.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LuckPermsDelegate {

    private final Map<UUID, Map<String, Boolean>> cache = new ConcurrentHashMap<>();
    private final LuckPerms luckPerms;

    LuckPermsDelegate(Plugin plugin) {
        RegisteredServiceProvider<LuckPerms> reg =
                Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        this.luckPerms = reg != null ? reg.getProvider() : null;

        if (luckPerms != null) {
            luckPerms.getEventBus().subscribe(plugin, NodeAddEvent.class, this::onNodeAdd);
            luckPerms.getEventBus().subscribe(plugin, NodeRemoveEvent.class, this::onNodeRemove);
            luckPerms.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, this::onUserDataRecalculate);
        }
    }

    boolean isEnabled() {
        return luckPerms != null;
    }

    boolean hasPermission(Player player, String node) {
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

    private void onNodeAdd(NodeAddEvent e) {
        handle(e.getTarget(), e.getNode());
    }

    private void onNodeRemove(NodeRemoveEvent e) {
        handle(e.getTarget(), e.getNode());
    }

    private void onUserDataRecalculate(UserDataRecalculateEvent e) {
        cache.remove(e.getUser().getUniqueId());
    }

    private void handle(PermissionHolder target, Node node) {
        if (target instanceof User user) {
            if (node instanceof PermissionNode || node instanceof InheritanceNode) {
                cache.remove(user.getUniqueId());
            }
        } else if (target instanceof Group group && node instanceof PermissionNode) {
            invalidateUsersInGroup(group);
        }
    }

    private void invalidateUsersInGroup(Group group) {
        String groupName = group.getName();
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            if (user != null && user.getInheritedGroups(user.getQueryOptions()).stream()
                    .anyMatch(g -> g.getName().equals(groupName))) {
                cache.remove(player.getUniqueId());
            }
        }
    }
}