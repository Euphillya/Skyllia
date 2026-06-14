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
public interface PermissionHook {

    /**
     * Tests whether a class is present on the classpath.
     * <p>
     * Used by implementations to detect, at load time, whether their backing
     * provider's API is available before attempting to bind to it.
     *
     * @param className the fully qualified name of the class to look up.
     * @return {@code true} if the class can be loaded, {@code false} otherwise.
     */
    static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks whether this hook is usable.
     * <p>
     * A hook is available only when its backing provider is both present on the
     * classpath and successfully bound. Callers must check this before invoking
     * {@link #hasPermission(Player, String)} and fall back to the Bukkit
     * permission API when it returns {@code false}.
     *
     * @return {@code true} if the hook can resolve permissions, {@code false} otherwise.
     */
    boolean isAvailable();

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