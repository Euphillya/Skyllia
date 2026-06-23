package fr.euphyllia.skyllia.hook.cmi;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.utils.SpawnUtil;
import net.Zrips.CMILib.Container.CMILocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class CMIDelegate {
    private final CMI cmi;

    public CMIDelegate() {
        this.cmi = (CMI) Bukkit.getPluginManager().getPlugin("CMI");
    }

    public boolean isEnabled() {
        return cmi != null && cmi.isEnabled();
    }

    @Nullable
    public Location getSpawnLocation(Player player) {
        CMILocation loc = SpawnUtil.getSpawnPoint(player);
        if (loc == null) return null;
        return loc.getBukkitLoc();
    }
}
