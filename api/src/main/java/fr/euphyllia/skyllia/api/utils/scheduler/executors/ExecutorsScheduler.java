package fr.euphyllia.skyllia.api.utils.scheduler.executors;

import fr.euphyllia.skyllia.api.utils.scheduler.model.Scheduler;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerCallBack;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerTaskInter;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorsScheduler implements Scheduler {

    private Plugin plugin;
    private ConcurrentHashMap<Integer, SchedulerTaskInter> mapSchedulerTask = new ConcurrentHashMap<>();

    public ExecutorsScheduler(Plugin pluginBukkit) {
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
        if (!schedulerType.equals(SchedulerType.ASYNC)) {
            throw new UnsupportedOperationException();
        }
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            ExecutorsSchedulerTask executorsScheduler = new ExecutorsSchedulerTask(plugin, executorService);
            mapSchedulerTask.put(executorsScheduler.getTaskId(), executorsScheduler);
            callBack.run(executorsScheduler);
        }, initialDelayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runDelayed(@NotNull SchedulerType schedulerType, long delayTicks, SchedulerCallBack callBack) {
        this.runDelayed(schedulerType, null, null, delayTicks, callBack);
    }

    @Override
    public void runDelayed(@NotNull SchedulerType schedulerType, @Nullable Object chunkOrLoc, long delayTicks, SchedulerCallBack callBack) {
        this.runDelayed(schedulerType, null, null, delayTicks, callBack);
    }

    @Override
    public void runDelayed(@NotNull SchedulerType schedulerType, @Nullable Object chunkOrLocOrEntity, @Nullable Runnable retired, long delayTicks, SchedulerCallBack callBack) {
        if (!schedulerType.equals(SchedulerType.ASYNC)) {
            throw new UnsupportedOperationException();
        }
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            ExecutorsSchedulerTask executorsScheduler = new ExecutorsSchedulerTask(plugin, executorService);
            mapSchedulerTask.put(executorsScheduler.getTaskId(), executorsScheduler);
            callBack.run(executorsScheduler);
        }, delayTicks * 50, TimeUnit.MILLISECONDS);
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
        if (!schedulerType.equals(SchedulerType.ASYNC)) {
            throw new UnsupportedOperationException();
        }
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.execute(() -> {
            ExecutorsSchedulerTask executorsScheduler = new ExecutorsSchedulerTask(plugin, executorService);
            mapSchedulerTask.put(executorsScheduler.getTaskId(), executorsScheduler);
            callBack.run(executorsScheduler);
        });
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
