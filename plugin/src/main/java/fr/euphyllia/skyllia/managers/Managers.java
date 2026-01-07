package fr.euphyllia.skyllia.managers;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.PermissionsManagers;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModuleManager;
import fr.euphyllia.skyllia.managers.world.WorldsManager;
import org.bukkit.Bukkit;

public class Managers {

    private final WorldsManager worldsManager;
    private final InterneAPI api;

    private final PermissionRegistry permissionRegistry;
    private final PermissionModuleManager permissionModuleManager;
    private final PermissionsManagers permissionsManagers;

    public Managers(InterneAPI interneAPI) {
        this.api = interneAPI;
        this.worldsManager = new WorldsManager(this.api);

        this.permissionRegistry = new PermissionRegistry();
        this.permissionModuleManager = new PermissionModuleManager(api.getPlugin(), this.permissionRegistry);
        this.permissionsManagers = new PermissionsManagers();
    }

    public void init() {
        Bukkit.getGlobalRegionScheduler().execute(api.getPlugin(), this.worldsManager::initWorld);
    }

    public PermissionRegistry getPermissionRegistry() {
        return permissionRegistry;
    }

    public PermissionModuleManager getPermissionModuleManager() {
        return permissionModuleManager;
    }

    public PermissionsManagers getPermissionsManagers() {
        return permissionsManagers;
    }
}
