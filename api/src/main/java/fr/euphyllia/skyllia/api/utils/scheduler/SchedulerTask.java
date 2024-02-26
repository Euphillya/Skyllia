package fr.euphyllia.skyllia.api.utils.scheduler;

import fr.euphyllia.skyllia.api.SkylliaAPI;
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
        if (schedulerSoft == SchedulerSoft.NATIVE) {
            return this.executorsScheduler;
        } else if (schedulerSoft == SchedulerSoft.MINECRAFT) {
            if (SkylliaAPI.isFolia()) {
                return this.foliaScheduler;
            }
            return this.legacyScheduler;
        }
        throw new UnsupportedOperationException();
    }

    public enum SchedulerSoft {
        NATIVE, MINECRAFT
    }
}