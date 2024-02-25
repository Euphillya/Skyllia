package fr.euphyllia.skyllia.api.utils.scheduler.legacy;

import fr.euphyllia.skyllia.api.utils.scheduler.model.Scheduler;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerCallBack;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerTaskInter;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LegacyScheduler implements Scheduler {

    private final ConcurrentHashMap<Integer, SchedulerTaskInter> mapSchedulerTask = new ConcurrentHashMap<>();
    private Plugin plugin;

    public LegacyScheduler(Plugin pluginBukkit) {
        this.plugin = pluginBukkit;
    }

    @Override
    public void runAtFixedRate(@NotNull SchedulerType schedulerType, long initialDelayTicks, long periodTicks, SchedulerCallBack callBack) {
        this.runAtFixedRate(schedulerType, null, null, initialDelayTicks, periodTicks, callBack);
    }

    @Override
    public void runAtFixedRate(@NotNull SchedulerType schedulerType, @Nullable Object chunkOrLoc, long initialDelayTicks, long periodTicks, SchedulerCallBack callBack) {
        this.runAtFixedRate(schedulerType, chunkOrLoc, null, initialDelayTicks, periodTicks, callBack);
    }

    @Override
    public void runAtFixedRate(@NotNull SchedulerType schedulerType, @Nullable Object chunkOrLocOrEntity, @Nullable Runnable retired, long initialDelayTicks, long periodTicks, SchedulerCallBack callBack) {
        if (schedulerType.equals(SchedulerType.ASYNC)) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, task -> {
                SchedulerTaskInter schedulerTask = new LegacySchedulerTask(task);
                mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                callBack.run(schedulerTask);
            }, initialDelayTicks, periodTicks);
        } else {
            Bukkit.getScheduler().runTaskTimer(this.plugin, task -> {
                SchedulerTaskInter schedulerTask = new LegacySchedulerTask(task);
                mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                callBack.run(schedulerTask);
            }, initialDelayTicks, periodTicks);
        }
    }

    @Override
    public void runDelayed(@NotNull SchedulerType schedulerType, long delayTicks, SchedulerCallBack callBack) {
        this.runDelayed(schedulerType, null, null, delayTicks, callBack);
    }

    @Override
    public void runDelayed(@NotNull SchedulerType schedulerType, @Nullable Object chunkOrLoc, long delayTicks, SchedulerCallBack callBack) {
        this.runDelayed(schedulerType, chunkOrLoc, null, delayTicks, callBack);
    }

    @Override
    public void runDelayed(@NotNull SchedulerType schedulerType, @Nullable Object chunkOrLocOrEntity, @Nullable Runnable retired, long delayTicks, SchedulerCallBack callBack) {
        if (schedulerType.equals(SchedulerType.ASYNC)) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, task -> {
                SchedulerTaskInter schedulerTask = new LegacySchedulerTask(task);
                mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                callBack.run(schedulerTask);
            }, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(this.plugin, task -> {
                SchedulerTaskInter schedulerTask = new LegacySchedulerTask(task);
                mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                callBack.run(schedulerTask);
            }, delayTicks);
        }
    }

    @Override
    public void execute(@NotNull SchedulerType schedulerType, SchedulerCallBack callBack) {
        this.execute(schedulerType, null, null, callBack);
    }

    @Override
    public void execute(@NotNull SchedulerType schedulerType, @Nullable Object chunkOrLoc, SchedulerCallBack callBack) {
        this.execute(schedulerType, chunkOrLoc, null, callBack);
    }

    @Override
    public void execute(@NotNull SchedulerType schedulerType, @Nullable Object chunkOrLocOrEntity, @Nullable Runnable retired, SchedulerCallBack callBack) {
        if (schedulerType.equals(SchedulerType.ASYNC)) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                callBack.run(null);
            });
        } else {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                callBack.run(null);
            });
        }
    }

    @Override
    public void cancelAllTask() {
        for (Map.Entry<Integer, SchedulerTaskInter> entry : mapSchedulerTask.entrySet()) {
            SchedulerTaskInter schedulerTaskInter = entry.getValue();
            schedulerTaskInter.cancel();
        }
    }

    @Override
    public void cancelTask(int taskId) {
        SchedulerTaskInter schedulerTask = this.mapSchedulerTask.get(taskId);
        if (schedulerTask == null || schedulerTask.isCancelled()) return;
        schedulerTask.cancel();
    }
}
