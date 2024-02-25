package fr.euphyllia.skyllia.managers;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.utils.scheduler.SchedulerTask;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerType;
import fr.euphyllia.skyllia.managers.world.WorldsManager;

public class Managers {

    private final WorldsManager worldsManager;
    private final InterneAPI api;

    public Managers(InterneAPI interneAPI) {
        this.api = interneAPI;
        this.worldsManager = new WorldsManager(this.api);
    }

    public void init() {
        SkylliaAPI.getSchedulerTask().getScheduler(SchedulerTask.SchedulerSoft.MINECRAFT)
                .execute(SchedulerType.GLOBAL, schedulerTask -> {
                    this.worldsManager.initWorld();
                });
    }
}
