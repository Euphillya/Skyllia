package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.service.TrustService;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.papi.SkylliaPAPIUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles {@code %skyllia_island_*%} placeholders.
 *
 * <table>
 *   <caption>Available placeholders</caption>
 *   <tr><th>Placeholder</th><th>Returns</th></tr>
 * <p>
 *   <tr><td colspan="2"><b>— Identity —</b></td></tr>
 *   <tr><td>island_id</td><td>Island UUID</td></tr>
 *   <tr><td>island_owner_name</td><td>Owner's last known name</td></tr>
 *   <tr><td>island_owner_uuid</td><td>Owner's UUID</td></tr>
 *   <tr><td>island_create_date</td><td>Creation date (Timestamp.toString)</td></tr>
 * <p>
 *   <tr><td colspan="2"><b>— Size —</b></td></tr>
 *   <tr><td>island_size</td><td>Island radius in blocks (double)</td></tr>
 *   <tr><td>island_size_int</td><td>Island radius rounded to int</td></tr>
 *   <tr><td>island_members_size</td><td>Current member count</td></tr>
 *   <tr><td>island_members_max_size</td><td>Maximum member slots</td></tr>
 * <p>
 *   <tr><td colspan="2"><b>— Role —</b></td></tr>
 *   <tr><td>island_role / island_rank</td><td>Requesting player's role name (e.g. OWNER)</td></tr>
 *   <tr><td>island_role_value</td><td>Numeric value of the requesting player's role</td></tr>
 * <p>
 *   <tr><td colspan="2"><b>— State —</b></td></tr>
 *   <tr><td>island_access</td><td>{@code true} if private, {@code false} if public</td></tr>
 *   <tr><td>island_disabled</td><td>{@code true} if the island is disabled</td></tr>
 * </table>
 *
 * <p>Warp-related placeholders are handled by {@link WarpHandler}.
 *
 * <p>Also handles the following global / online-player placeholders:
 * <table>
 *   <tr><th>Placeholder</th><th>Returns</th></tr>
 *   <tr><td>island_total</td><td>Total number of islands in the Skyblock world</td></tr>
 *   <tr><td>island_members_online</td><td>Online members + trusted of the player's island</td></tr>
 *   <tr><td>island_visitors_online</td><td>Online visitors (not member/trusted) on the player's island</td></tr>
 * </table>
 */
public class IslandHandler implements PlaceholderHandler {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private static @NotNull String ownerName(@NotNull Island island) {
        Players owner = island.getOwner();
        return owner != null ? owner.getLastKnowName() : "";
    }

    private static @NotNull String ownerUuid(@NotNull Island island) {
        Players owner = island.getOwner();
        return owner != null ? owner.getMojangId().toString() : "";
    }

    private static @NotNull String createDate(@NotNull Island island) {
        return island.getCreateDate() != null ? island.getCreateDate().toString() : "";
    }

    private static int countMembersOnline(@NotNull Island island, @NotNull Set<UUID> onlineIds) {
        int count = 0;
        for (Players member : island.getMembers()) {
            if (onlineIds.contains(member.getMojangId())) count++;
        }
        TrustService trustService = SkylliaAPI.getTrustService();
        if (trustService != null) {
            Set<UUID> trusted = trustService.getTrusted(island.getId());
            if (trusted != null) {
                for (UUID id : trusted) {
                    if (onlineIds.contains(id)) count++;
                }
            }
        }
        return count;
    }

    private static int countVisitorsOnline(@NotNull Island island, @NotNull Set<UUID> onlineIds) {
        UUID islandId = island.getId();
        TrustService trustService = SkylliaAPI.getTrustService();
        int count = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            // Calculate the block X/Z directly from Pos without retrieving a Chunk object
            Location loc = online.getLocation();
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;

            Island standing = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
            if (standing == null || !standing.getId().equals(islandId)) continue;

            UUID playerId = online.getUniqueId();
            if (island.getMember(playerId) != null) continue;
            if (trustService != null && trustService.isTrusted(islandId, playerId)) continue;
            count++;
        }
        return count;
    }

    private static Set<UUID> getOnlinePlayersId() {
        Set<UUID> ids = new java.util.HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            ids.add(p.getUniqueId());
        }
        return ids;
    }

    @Override
    public boolean requiresIsland() {
        return false;
    }

    @Override
    public @NotNull String prefix() {
        return "island";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @Nullable Island island,
                                   @NotNull String key) {
        switch (key) {
            case "total" -> {
                List<Island> all = SkylliaAPI.getAllIslandsValid();
                return String.valueOf(all == null ? 0 : all.size());
            }
            case "members_online" -> {
                Set<UUID> onlineIds = getOnlinePlayersId();
                return island == null ? "0" : String.valueOf(countMembersOnline(island, onlineIds));
            }
            case "visitors_online" -> {
                Set<UUID> onlineIds = getOnlinePlayersId();
                return island == null ? "0" : String.valueOf(countVisitorsOnline(island, onlineIds));
            }
        }

        if (island == null) {
            return "";
        }
        return switch (key) {

            case "id" -> island.getId().toString();
            case "owner_name" -> ownerName(island);
            case "owner_uuid" -> ownerUuid(island);
            case "create_date" -> createDate(island);

            case "size" -> String.valueOf(island.getSize());
            case "size_int" -> String.valueOf((int) island.getSize());
            case "members_size" -> String.valueOf(island.getMembers().size());
            case "members_max_size" -> String.valueOf(island.getMaxMembers());

            case "role", "rank" -> SkylliaPAPIUtils.resolveRole(island, player.getUniqueId()).name();
            case "role_value" -> String.valueOf(SkylliaPAPIUtils.resolveRole(island, player.getUniqueId()).getValue());

            case "access" -> String.valueOf(island.isPrivateIsland());
            case "disabled" -> String.valueOf(island.isDisable());
            case "name" -> {
                String raw = island.getName();
                yield raw != null ? render(raw) : player.getName() + "'s Island";
            }
            case "name_raw" -> {
                String raw = island.getName();
                yield raw != null ? raw : player.getName() + "'s Island";
            }
            case "description" -> {
                String raw = island.getDescription();
                yield raw != null ? render(raw) : "";
            }
            case "description_raw" -> {
                String raw = island.getDescription();
                yield raw != null ? raw : "";
            }
            default -> null;
        };
    }

    private String render(String miniMessageString) {
        Component component = miniMessage.deserialize(miniMessageString);
        return LEGACY.serialize(component);
    }
}
