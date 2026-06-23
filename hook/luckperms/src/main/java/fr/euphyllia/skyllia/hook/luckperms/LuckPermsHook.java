package fr.euphyllia.skyllia.hook.luckperms;

import fr.euphyllia.skyllia.api.hooks.PermissionHook;
import fr.euphyllia.skyllia.api.hooks.PluginHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LuckPermsHook implements PermissionHook {

    private static final boolean CLASS_AVAILABLE =
            PluginHook.hasClass("net.luckperms.api.LuckPerms");

    private LuckPermsDelegate delegate;

    public LuckPermsHook() {
    }

    @Override
    public String name() {
        return "LuckPerms";
    }

    @Override
    public boolean isAvailable() {
        return CLASS_AVAILABLE
                && Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
    }

    @Override
    public void register(Plugin skylliaPlugin) {
        this.delegate = new LuckPermsDelegate(skylliaPlugin);
    }

    @Override
    public boolean hasPermission(Player player, String node) {
        if (delegate == null || !delegate.isEnabled()) return player.hasPermission(node); // FallBack
        return delegate.hasPermission(player, node);
    }
}