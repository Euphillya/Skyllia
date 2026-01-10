package fr.euphyllia.skyllia.hook.essentialsx;


import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import fr.euphyllia.skyllia.api.hooks.SpawnHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class EssentialsSpawnHook implements SpawnHook {
    @Override
    public boolean isAvailable() {
        return hasClass("com.earth2me.essentials.spawn.EssentialsSpawn") && hasClass("com.earth2me.essentials.Essentials");
    }

    @Override
    public @Nullable Location getSpawnLocation(Player player) {
        EssentialsSpawn essentialsSpawn = (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentialsSpawn == null || essentials == null) {
            return null;
        }
        if (essentialsSpawn.isEnabled() && essentials.isEnabled()) {
            return essentialsSpawn.getSpawn(essentials.getUser(player.getUniqueId()).getGroup());
        }
        return null;
    }
}
