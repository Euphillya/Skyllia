package fr.euphyllia.skyllia.api.hooks;

import org.bukkit.entity.Player;

/**
 * Hook for delegating Bukkit permission-node checks to an external permissions
 * provider (such as LuckPerms) through its own cached permission data.
 * <p>
 * The default Bukkit path ({@link Player#hasPermission(String)}) may trigger a full
 * context resolution on every call when a provider like LuckPerms is installed,
 * which can be costly on hot code paths. Implementations of this hook are expected
 * to read directly from the provider's cached data instead, avoiding that overhead
 * while letting the provider remain responsible for cache invalidation when
 * permissions change.
 * <p>
 * Implementations must be safe to call from any thread (including Folia region
 * threads). When no supported provider is present, callers should fall back to
 * {@link Player#hasPermission(String)}.
 */
public interface PermissionHook extends PluginHook {

    /**
     * Checks whether the given player holds the specified permission node,
     * reading from the backing provider's cached permission data.
     * <p>
     * This is intended as a faster, thread-safe alternative to
     * {@link Player#hasPermission(String)} for nodes that are queried frequently.
     * Implementations should resolve the node against the provider's cache rather
     * than re-running context calculation on each call.
     *
     * @param player the player to check.
     * @param node   the permission node to resolve (for example, {@code "skyllia.player.interact.bypass"}).
     * @return {@code true} if the player holds the node, {@code false} otherwise.
     */
    boolean hasPermission(Player player, String node);
}