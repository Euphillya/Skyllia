package fr.euphyllia.skyllia.hook.quickshop;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.event.SkyblockDeleteEvent;
import fr.euphyllia.skyllia.api.event.SkyblockRemoveMemberEvent;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.hook.quickshop.configuration.QSConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class QuickShopListener implements Listener {

    private static final Logger log = LoggerFactory.getLogger(QuickShopListener.class);

    private final PermissionId QUICKSHOP_CREATE_SHOP;

    public QuickShopListener() {
        QUICKSHOP_CREATE_SHOP = SkylliaAPI.getPermissionRegistry().register(new PermissionNode(
                new NamespacedKey(SkylliaAPI.getPlugin(), "quickshop.shop.create"),
                "island.permission.quickshop.shop.create.name",
                "island.permission.quickshop.shop.create.description"
        ));
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopCreate(final ShopCreateEvent event) {
        if (!event.phase().cancellable()) return;

        final Location shopLoc = event.location();
        if (!SkylliaAPI.isWorldSkyblock(shopLoc.getWorld())) return;

        final Island island = SkylliaAPI.getIslandByChunk(shopLoc.getChunk());
        if (island == null) {
            event.setCancelled(true, "You cannot create a shop outside of an island.");
            return;
        }

        event.user().getBukkitPlayer().ifPresent(player -> {
            if (QSConfigLoader.config.isOnlyOwnerCanCreateShop()) {
                final var owner = island.getOwner();
                if (owner == null || !owner.getMojangId().equals(player.getUniqueId())) {
                    event.setCancelled(true, SkylliaAPI.getLanguageProvider().translate(player, "hook.quickshop.create.owner-only"));
                }
            } else {
                boolean allowed = SkylliaAPI.getPermissionsManager().hasPermission(
                        player, island, QUICKSHOP_CREATE_SHOP, null
                );
                if (!allowed) {
                    event.setCancelled(true, SkylliaAPI.getLanguageProvider().translate(player, "hook.quickshop.create.permission-denied"));
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShopPurchase(final ShopPurchaseEvent event) {
        final Location shopLoc = event.getShop().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(shopLoc.getWorld())) return;

        final Island island = SkylliaAPI.getIslandByChunk(shopLoc.getChunk());
        if (island == null) return;

        if (island.isDisable()) {
            event.getPurchaser().getBukkitPlayer().ifPresent(player ->
                    event.setCancelled(true, SkylliaAPI.getLanguageProvider().translate(player, "hook.quickshop.purchase.island-disabled"))
            );
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDelete(final SkyblockDeleteEvent event) {
        deleteShopsInIsland(event.getIsland(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRemoveMemberEvent(final SkyblockRemoveMemberEvent event) {
        if (!QSConfigLoader.config.isDeleteShopOnMemberLeave()) return;

        if (event.getCause() == RemovalCause.ISLAND_DELETED) return;

        deleteShopsInIsland(event.getIsland(), event.getRemovedPlayer().getMojangId());
    }

    /**
     * Deletes all shops on the island
     *
     * @param island      The island to clean up.
     * @param ownerFilter If non-null, only shops owned by this UUID are deleted.
     */
    private void deleteShopsInIsland(Island island, UUID ownerFilter) {
        final QuickShopAPI api = QuickShopAPI.getInstance();
        if (api == null) return;

        for (final World world : Bukkit.getWorlds()) {
            if (!SkylliaAPI.isWorldSkyblock(world)) continue;

            final List<Shop> shops = api.getShopManager().getShopsInWorld(world);
            if (shops.isEmpty()) continue;

            for (final Shop shop : shops) {
                final Location loc = shop.getLocation();
                if (loc == null) continue;

                final Island shopIsland = SkylliaAPI.getIslandByChunk(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
                if (shopIsland == null || !shopIsland.getId().equals(island.getId())) continue;

                if (ownerFilter != null && !Objects.equals(shop.getOwner().getUniqueId(), ownerFilter)) continue;

                Bukkit.getRegionScheduler().execute(SkylliaAPI.getPlugin(), loc, () -> {
                    try {
                        api.getShopManager().deleteShop(shop);
                    } catch (Exception e) {
                        log.error("Failed to delete shop at {} during island cleanup", loc, e);
                    }
                });
            }
        }
    }
}