package dev.spaxter.lynxlib.task;

import dev.spaxter.lynxlib.common.MagicValues;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

/**
 * Basic task scheduler for server-side tasks.
 */
public class TaskScheduler {

    private static final PriorityQueue<DelayedTask> taskQueue = new PriorityQueue<>();
    private static MinecraftServer server;

    public static void scheduleTask(MinecraftServer server, float delay, Runnable task) {
        TaskScheduler.scheduleTask(server, delay, task, UUID.randomUUID());
    }

    /**
     * Schedules a task to run after a delay. If {@code delay == 0}, {@code task} will be executed immediately.
     *
     * @param server The server to set the task for
     * @param delay  The delay in seconds
     * @param task   The task to run
     */
    public static void scheduleTask(MinecraftServer server, float delay, Runnable task, UUID taskId) {
        long currentTick = server.getTickCount();
        long executionTick = Math.round(currentTick + delay * MagicValues.TICKS_PER_SECOND);

        if (delay == 0) {
            task.run();
        } else {
            DelayedTask delayedTask = new DelayedTask(executionTick, task, taskId);
            taskQueue.add(delayedTask);
        }
    }

    /**
     * Un-schedule a task.
     *
     * @param taskId Task UUID
     */
    public static void unScheduleTask(UUID taskId) {
        List<DelayedTask> task = taskQueue.stream().filter(t -> t.getId().equals(taskId)).collect(Collectors.toList());
        task.forEach(taskQueue::remove);
    }

    /**
     * Schedule a task that repeats every {@code delay} seconds.
     *
     * @param server The server to set the task for
     * @param delay  Delay in seconds
     * @param task   The task to run
     * @param taskId UUID of the task
     */
    public static void repeatTask(final MinecraftServer server, final float delay, final Runnable task,
        final UUID taskId) {
        Runnable repeatedTask = new Runnable() {
            @Override
            public void run() {
                task.run();
                TaskScheduler.scheduleTask(server, delay, this, taskId);
            }
        };

        // Schedule the first instance of the repeated task
        TaskScheduler.scheduleTask(server, delay, repeatedTask, taskId);
    }

    /**
     * Server tick event.
     *
     * @param event Event
     */
    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        synchronized (taskQueue) {
            if (event.phase == TickEvent.Phase.END && !taskQueue.isEmpty()) {
                long currentTick = server.getTickCount();

                // Process tasks whose scheduled execution time has passed
                while (!taskQueue.isEmpty() && taskQueue.peek().executionTick <= currentTick) {
                    DelayedTask taskToRun = taskQueue.poll();
                    taskToRun.task.run();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStart(FMLServerStartingEvent event) {
        server = event.getServer();
    }

    private static class DelayedTask implements Comparable<DelayedTask> {
        private final long executionTick;
        private final Runnable task;
        private final UUID id;

        public DelayedTask(long executionTick, Runnable task, UUID id) {
            this.executionTick = executionTick;
            this.task = task;
            this.id = id;
        }

        public UUID getId() {
            return this.id;
        }

        @Override
        public int compareTo(DelayedTask otherTask) {
            return Long.compare(this.executionTick, otherTask.executionTick);
        }
    }
}
