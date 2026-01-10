package fr.euphyllia.skyllia.api.permissions.modules;

import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public interface PermissionModule extends Listener {

    void registerPermissions(PermissionRegistry registry, Plugin owner);
}
