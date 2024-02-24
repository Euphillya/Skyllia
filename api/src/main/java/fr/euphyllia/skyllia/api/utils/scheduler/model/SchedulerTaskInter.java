package fr.euphyllia.skyllia.api.utils.scheduler.model;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface SchedulerTaskInter {

    @NotNull Plugin getPlugin();

    boolean isCancelled();

    void cancel();

    int getTaskId();
}
