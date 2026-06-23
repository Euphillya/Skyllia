package fr.euphyllia.skyllia.hook.essentialsx;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class EssentialsDelegate {

    private final EssentialsSpawn essentialsSpawn;
    private final Essentials essentials;

    public EssentialsDelegate() {
        this.essentialsSpawn =
                (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
        this.essentials =
                (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    public boolean isEnabled() {
        return essentialsSpawn != null
                && essentials != null
                && essentialsSpawn.isEnabled()
                && essentials.isEnabled();
    }

    @Nullable
    public Location getSpawnLocation(Player player) {
        return essentialsSpawn.getSpawn(
                essentials.getUser(player.getUniqueId()).getGroup()
        );
    }
}
