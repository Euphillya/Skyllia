package fr.euphyllia.skyllia.papi;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.papi.handlers.*;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PlaceholderAPI expansion for Skyllia.
 *
 * <p>Acts as a pure router: it resolves the island for the requesting player,
 * strips the placeholder prefix, and delegates to the appropriate
 * {@link PlaceholderHandler}. No business logic lives here.
 *
 * <p>To add a new placeholder group, implement {@link PlaceholderHandler}
 * and register it in {@link #buildHandlerRegistry()}.
 */
public class SkylliaExpansion extends PlaceholderExpansion {

    private final Skyllia plugin;

    private final Map<String, PlaceholderHandler> handlers;

    public SkylliaExpansion(@NotNull Skyllia skyllia) {
        this.plugin = skyllia;
        this.handlers = buildHandlerRegistry();
    }

    /**
     * Registers all placeholder handlers.
     * Add new entries here when creating a new handler.
     */
    private static Map<String, PlaceholderHandler> buildHandlerRegistry() {
        List<PlaceholderHandler> allHandlers = List.of(
                new IslandHandler(),
                new FlagsHandler(),
                new PermissionsHandler(),
                new BannedHandler(),
                new MembersHandler(),
                new WarpHandler(),
                // Legacy alias: gamerule_* → same handler as flags_*
                new FlagsHandler() {
                    @Override
                    public @NotNull String prefix() {
                        return "gamerule";
                    }
                }
        );

        Map<String, PlaceholderHandler> registry = new HashMap<>();
        for (PlaceholderHandler handler : allHandlers) {
            registry.put(handler.prefix() + "_", handler);
        }
        return Map.copyOf(registry);
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.plugin.getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return this.plugin.getPluginMeta().getAuthors().getFirst();
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Nullable
    @Override
    public String onRequest(@NotNull OfflinePlayer player, @NotNull String placeholder) {
        UUID playerId = player.getUniqueId();

        Island island = SkylliaAPI.getIslandByPlayerId(playerId);
        if (island == null) return null;

        String lowerPlaceholder = placeholder.toLowerCase();

        for (Map.Entry<String, PlaceholderHandler> entry : handlers.entrySet()) {
            String prefix = entry.getKey();   // e.g. "island_"
            if (lowerPlaceholder.startsWith(prefix)) {
                String key = lowerPlaceholder.substring(prefix.length());
                return entry.getValue().handle(player, island, key);
            }
        }

        return null;
    }
}
