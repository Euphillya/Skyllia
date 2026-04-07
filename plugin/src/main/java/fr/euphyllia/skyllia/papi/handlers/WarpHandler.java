package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.papi.SkylliaPAPIUtils;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Handles {@code %skyllia_warp_*%} placeholders.
 * Exposes the island's warp list as indexed entries,
 * suitable for rendering dynamic GUIs in DeluxeMenus.
 *
 * <table>
 *   <caption>Available placeholders</caption>
 *   <tr><th>Placeholder</th><th>Returns</th></tr>
 *   <tr><td>warp_count</td><td>Total number of warps</td></tr>
 *   <tr><td>warp_has_&lt;N&gt;</td><td>{@code true} if index N exists, otherwise {@code false}</td></tr>
 *   <tr><td>warp_name_&lt;N&gt;</td><td>Name of the warp at index N</td></tr>
 *   <tr><td>warp_world_&lt;N&gt;</td><td>World name of the warp at index N</td></tr>
 *   <tr><td>warp_x_&lt;N&gt;</td><td>X coordinate (block) of the warp at index N</td></tr>
 *   <tr><td>warp_y_&lt;N&gt;</td><td>Y coordinate (block) of the warp at index N</td></tr>
 *   <tr><td>warp_z_&lt;N&gt;</td><td>Z coordinate (block) of the warp at index N</td></tr>
 * </table>
 *
 * <p>Indices are zero-based. Out-of-range requests return an empty string.
 * If {@link Island#getWarps()} returns {@code null}, all placeholders behave
 * as if the list is empty.
 */
public class WarpHandler implements PlaceholderHandler {

    private static @NotNull List<WarpIsland> warps(@NotNull Island island) {
        List<WarpIsland> warps = island.getWarps();
        return warps != null ? warps : Collections.emptyList();
    }

    private static @NotNull String worldName(@NotNull WarpIsland warp) {
        Location loc = warp.location();
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName();
    }

    @Override
    public @NotNull String prefix() {
        return "warp";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @NotNull Island island,
                                   @NotNull String key) {
        List<WarpIsland> warps = warps(island);

        if (key.equals("count")) {
            return String.valueOf(warps.size());
        }

        if (key.startsWith("has_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("has_".length()));
            if (index < 0) return null;
            return String.valueOf(index < warps.size());
        }

        if (key.startsWith("name_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("name_".length()));
            if (index < 0 || index >= warps.size()) return "";
            return warps.get(index).warpName();
        }

        if (key.startsWith("world_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("world_".length()));
            if (index < 0 || index >= warps.size()) return "";
            return worldName(warps.get(index));
        }

        if (key.startsWith("x_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("x_".length()));
            if (index < 0 || index >= warps.size()) return "";
            Location loc = warps.get(index).location();
            return loc != null ? String.valueOf(loc.getBlockX()) : "";
        }

        if (key.startsWith("y_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("y_".length()));
            if (index < 0 || index >= warps.size()) return "";
            Location loc = warps.get(index).location();
            return loc != null ? String.valueOf(loc.getBlockY()) : "";
        }

        if (key.startsWith("z_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("z_".length()));
            if (index < 0 || index >= warps.size()) return "";
            Location loc = warps.get(index).location();
            return loc != null ? String.valueOf(loc.getBlockZ()) : "";
        }

        return null;
    }
}