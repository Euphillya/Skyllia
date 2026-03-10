package fr.euphyllia.skyllia.listeners.permissions.flags.fire;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.plugin.Plugin;

public class IslandAllowFireBurnPermissions implements FlagModule {

    private FlagId ISLAND_ALLOW_FIRE;

    @EventHandler(ignoreCancelled = true)
    public void onBurn(final BlockBurnEvent event) {
        final Location location = event.getBlock().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        if (!SkylliaAPI.getPermissionsManager().hasFlag(island, ISLAND_ALLOW_FIRE)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerFlags(IslandFlagRegistry registry, Plugin owner) {
        this.ISLAND_ALLOW_FIRE = registry.idOrRegister(new FlagNode(
                new NamespacedKey(owner, "island.allow.fire"),
                "Autoriser le feu",
                "Contrôle si le feu peut brûler des blocs"
        ));
    }
}
