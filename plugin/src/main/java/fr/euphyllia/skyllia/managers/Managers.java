package fr.euphyllia.skyllia.managers;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.permissions.*;
import fr.euphyllia.skyllia.api.permissions.modules.FlagModuleManager;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModuleManager;
import fr.euphyllia.skyllia.managers.world.WorldsManager;
import org.bukkit.Bukkit;

public class Managers {

    private final WorldsManager worldsManager;
    private final InterneAPI api;

    private final PermissionRegistry permissionRegistry;
    private final PermissionModuleManager permissionModuleManager;
    private final PermissionsManagers permissionsManagers;

    private final IslandFlagRegistry flagRegistry;
    private final FlagModuleManager flagModuleManager;


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

        this.flagRegistry = new IslandFlagRegistry(indexStore);
        this.flagModuleManager = new FlagModuleManager(api.getPlugin(), this.flagRegistry);
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

    public IslandFlagRegistry getFlagRegistry() {
        return flagRegistry;
    }

    public FlagModuleManager getFlagModuleManager() {
        return flagModuleManager;
    }
}
