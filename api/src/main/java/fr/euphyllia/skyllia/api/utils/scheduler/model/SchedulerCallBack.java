package fr.euphyllia.skyllia.api.utils.scheduler.model;

import org.jetbrains.annotations.Nullable;

public interface SchedulerCallBack {

    void run(@Nullable SchedulerTaskInter schedulerTask);
}
