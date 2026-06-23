package fr.euphyllia.skyllia.hook.cmi;

import fr.euphyllia.skyllia.api.hooks.PluginHook;
import fr.euphyllia.skyllia.api.hooks.SpawnHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class CMISpawnHook implements SpawnHook {

    private static final boolean CLASS_AVAILABLE =
            PluginHook.hasClass("com.Zrips.CMI.CMI")
                    && PluginHook.hasClass("com.Zrips.CMI.utils.SpawnUtil");

    private CMIDelegate delegate;

    public CMISpawnHook() {
    }

    @Override
    public String name() {
        return "CMI";
    }

    @Override
    public boolean isAvailable() {
        return CLASS_AVAILABLE
                && Bukkit.getPluginManager().getPlugin("CMI") != null;
    }

    @Override
    public void register(Plugin skylliaPlugin) {
        this.delegate = new CMIDelegate();
    }

    @Override
    public @Nullable Location getSpawnLocation(Player player) {
        if (delegate == null || !delegate.isEnabled()) {
            return null;
        }
        return delegate.getSpawnLocation(player);
    }
}