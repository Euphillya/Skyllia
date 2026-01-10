package fr.euphyllia.skyllia.papi;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public class SkylliaExpansion extends PlaceholderExpansion {


    private final Skyllia plugin;

    public SkylliaExpansion(Skyllia skyllia) {
        this.plugin = skyllia;
    }

    private static @NotNull RoleType resolveRole(Island island, UUID playerId) {
        var member = island.getMember(playerId);
        if (member == null) {
            return RoleType.VISITOR;
        }
        RoleType role = member.getRoleType();
        return role != null ? role : RoleType.VISITOR;
    }

    private static @Nullable RoleType parseRole(String input) {
        if (input == null || input.isEmpty()) return null;
        try {
            return RoleType.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static @Nullable NamespacedKey parseKeyLenient(String input) {
        if (input == null || input.isEmpty()) return null;

        NamespacedKey key = NamespacedKey.fromString(input);
        if (key != null) return key;

        return NamespacedKey.fromString("skyllia:" + input);
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
    public String onRequest(final OfflinePlayer player, @NotNull final String placeholder) {
        UUID offlinePlayerUUID = player.getUniqueId();

        Island island = SkylliaAPI.getIslandByPlayerId(offlinePlayerUUID);
        if (island == null) {
            return null;
        }

        String placeholderLower = placeholder.toLowerCase();
        if (placeholderLower.startsWith("island_")) {
            return processIsland(island, offlinePlayerUUID, placeholderLower);
        }

        if (placeholderLower.startsWith("permissions_")) {
            return processPermissions(island, offlinePlayerUUID, placeholderLower);
        }

        return null;
    }

    private String processIsland(Island island, UUID playerId, String placeholder) {
        RoleType role = resolveRole(island, playerId);

        return switch (placeholder) {
            case "island_size" -> String.valueOf(island.getSize());
            case "island_members_max_size" -> String.valueOf(island.getMaxMembers());
            case "island_members_size" -> String.valueOf(island.getMembers().size());
            case "island_role", "island_rank" -> role.name();
            case "island_role_value" -> String.valueOf(role.getValue());
            default -> null;
        };
    }

    private String processPermissions(Island island, UUID playerId, String placeholder) {
        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();
        String rest = placeholder.substring("permissions_".length());

        RoleType role;
        String keyPart;

        if (rest.startsWith("role_")) {
            String tmp = rest.substring("role_".length());
            int idx = tmp.indexOf('_');
            if (idx <= 0) return null;

            String roleStr = tmp.substring(0, idx);
            keyPart = tmp.substring(idx + 1);

            role = parseRole(roleStr);
            if (role == null) return null;
        } else {
            role = resolveRole(island, playerId);
            keyPart = rest;
        }
        NamespacedKey key = parseKeyLenient(keyPart);
        if (key == null) return null;

        PermissionId pid = registry.getIfPresent(key);
        if (pid == null) return null;

        boolean allowed = island.getCompiledPermissions().has(registry, role, pid);
        return String.valueOf(allowed);
    }
}
