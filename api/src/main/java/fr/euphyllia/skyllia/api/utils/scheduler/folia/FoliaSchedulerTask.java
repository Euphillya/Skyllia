package fr.euphyllia.skyllia.api.utils.scheduler.folia;

import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerTaskInter;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FoliaSchedulerTask implements SchedulerTaskInter {

    private ScheduledTask schedulerTask;

    public FoliaSchedulerTask(ScheduledTask schedulerTask) {
        this.schedulerTask = schedulerTask;
    }


    @Override
    public @NotNull Plugin getPlugin() {
        return this.schedulerTask.getOwningPlugin();
    }

    @Override
    public boolean isCancelled() {
        return this.schedulerTask.isCancelled();
    }

    @Override
    public void cancel() {
        this.schedulerTask.cancel();
    }

    @Override
    public int getTaskId() {
        return this.schedulerTask.hashCode();
    }
}
