package fr.euphyllia.skyllia.managers;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.managers.world.WorldsManager;

public class Managers {

    private final WorldsManager worldsManager;
    private final InterneAPI api;

    public Managers(InterneAPI interneAPI) {
        this.api = interneAPI;
        this.worldsManager = new WorldsManager(this.api);
    }

    public void init() {
        SkylliaAPI.getScheduler()
                .runTask(SchedulerType.SYNC, schedulerTask -> {
                    this.worldsManager.initWorld();
                });
    }
}
