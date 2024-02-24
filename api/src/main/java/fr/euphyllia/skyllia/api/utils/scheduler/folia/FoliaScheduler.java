package fr.euphyllia.skyllia.api.utils.scheduler.folia;

import fr.euphyllia.skyllia.api.utils.scheduler.model.Scheduler;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerCallBack;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerTaskInter;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FoliaScheduler implements Scheduler {

    private Plugin plugin;
    private ConcurrentHashMap<Integer, SchedulerTaskInter> mapSchedulerTask = new ConcurrentHashMap<>();

    public FoliaScheduler(Plugin pluginBukkit) {
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
        if (initialDelayTicks <= 0) {
            initialDelayTicks = 1;
        }
        if (periodTicks <= 0) {
            periodTicks = 1;
        }
        switch (schedulerType) {
            case GLOBAL -> {
                Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, task -> {
                    SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                    mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                    callBack.run(schedulerTask);
                }, initialDelayTicks, periodTicks);
            }
            case REGION -> {
                if (chunkOrLocOrEntity instanceof Location loc) {
                    Bukkit.getRegionScheduler().runAtFixedRate(this.plugin, loc, task -> {
                        SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                        mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                        callBack.run(schedulerTask);
                    }, initialDelayTicks, periodTicks);
                } else if (chunkOrLocOrEntity instanceof Chunk chunk) {
                    Bukkit.getRegionScheduler().runAtFixedRate(this.plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), task -> {
                        SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                        mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                        callBack.run(schedulerTask);
                    }, initialDelayTicks, periodTicks);
                } else {
                    throw new RuntimeException("Object can only be Location or Chunk");
                }
            }
            case ENTITY -> {
                if (chunkOrLocOrEntity instanceof Entity entity) {
                    entity.getScheduler().runAtFixedRate(this.plugin, task -> {
                        SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                        mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                        callBack.run(schedulerTask);
                    }, retired, initialDelayTicks, periodTicks);
                }
            }
            case ASYNC -> {
                Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin, task -> {
                    SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                    mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                    callBack.run(schedulerTask);
                }, initialDelayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS);
            }
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
        if (delayTicks <= 0) {
            delayTicks = 1;
        }
        switch (schedulerType) {
            case GLOBAL -> {
                Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin, task -> {
                    SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                    mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                    callBack.run(schedulerTask);
                }, delayTicks);
            }
            case REGION -> {
                if (chunkOrLocOrEntity instanceof Location loc) {
                    Bukkit.getRegionScheduler().runDelayed(this.plugin, loc, task -> {
                        SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                        mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                        callBack.run(schedulerTask);
                    }, delayTicks);
                } else if (chunkOrLocOrEntity instanceof Chunk chunk) {
                    Bukkit.getRegionScheduler().runDelayed(this.plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), task -> {
                        SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                        mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                        callBack.run(schedulerTask);
                    }, delayTicks);
                } else {
                    throw new RuntimeException("Object can only be Location or Chunk");
                }
            }
            case ENTITY -> {
                if (chunkOrLocOrEntity instanceof Entity entity) {
                    entity.getScheduler().runDelayed(this.plugin, task -> {
                        SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                        mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                        callBack.run(schedulerTask);
                    }, retired, delayTicks);
                }
            }
            case ASYNC -> {
                Bukkit.getAsyncScheduler().runDelayed(this.plugin, task -> {
                    SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                    mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                    callBack.run(schedulerTask);
                }, delayTicks * 50, TimeUnit.MILLISECONDS);
            }
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
        switch (schedulerType) {
            case GLOBAL -> {
                Bukkit.getGlobalRegionScheduler().execute(this.plugin, () -> {
                    callBack.run(null);
                });
            }
            case REGION -> {
                if (chunkOrLocOrEntity instanceof Location loc) {
                    Bukkit.getRegionScheduler().execute(this.plugin, loc, () -> {
                        callBack.run(null);
                    });
                } else if (chunkOrLocOrEntity instanceof Chunk chunk) {
                    Bukkit.getRegionScheduler().execute(this.plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), () -> {
                        callBack.run(null);
                    });
                } else {
                    throw new RuntimeException("Object can only be Location or Chunk");
                }
            }
            case ENTITY -> {
                if (chunkOrLocOrEntity instanceof Entity entity) {
                    entity.getScheduler().run(this.plugin, task -> {
                        SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                        mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                        callBack.run(schedulerTask);
                    }, retired);
                }
            }
            case ASYNC -> {
                Bukkit.getAsyncScheduler().runNow(this.plugin, task -> {
                    SchedulerTaskInter schedulerTask = new FoliaSchedulerTask(task);
                    mapSchedulerTask.put(schedulerTask.getTaskId(), schedulerTask);
                    callBack.run(schedulerTask);
                });
            }
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
