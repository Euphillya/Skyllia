package fr.euphyllia.skyllia.api.utils.scheduler;

import fr.euphyllia.skyllia.api.utils.scheduler.executors.ExecutorsScheduler;
import fr.euphyllia.skyllia.api.utils.scheduler.folia.FoliaScheduler;
import fr.euphyllia.skyllia.api.utils.scheduler.legacy.LegacyScheduler;
import fr.euphyllia.skyllia.api.utils.scheduler.model.Scheduler;
import org.bukkit.plugin.Plugin;

public class SchedulerTask {

    private final Plugin plugin;
    private final ExecutorsScheduler executorsScheduler;
    private final LegacyScheduler legacyScheduler;
    private final FoliaScheduler foliaScheduler;

    public SchedulerTask(Plugin javaPlugin) {
        this.plugin = javaPlugin;
        this.executorsScheduler = new ExecutorsScheduler(this.plugin);
        this.legacyScheduler = new LegacyScheduler(this.plugin);
        this.foliaScheduler = new FoliaScheduler(this.plugin);
    }

    public Scheduler getScheduler(SchedulerSoft schedulerSoft) {
        return switch (schedulerSoft) {
            case NATIVE -> this.executorsScheduler;
            case LEGACY -> this.legacyScheduler;
            case FOLIA -> this.foliaScheduler;
        };
    }

    public enum SchedulerSoft {
        NATIVE, LEGACY, FOLIA
    }
}
