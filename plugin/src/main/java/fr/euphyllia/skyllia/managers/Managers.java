package fr.euphyllia.skyllia.managers;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionIndexStore;
import fr.euphyllia.skyllia.api.permissions.PermissionIndexStoreFactory;
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

        var loader = api.getDatabaseLoader();
        if (loader == null) {
            throw new IllegalStateException("Database not initialized (DatabaseLoader is null)");
        }

        PermissionIndexStore indexStore = PermissionIndexStoreFactory.create(loader);

        this.permissionRegistry = new PermissionRegistry(indexStore);
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
