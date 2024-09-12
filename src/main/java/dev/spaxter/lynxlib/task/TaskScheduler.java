package dev.spaxter.lynxlib.task;

import dev.spaxter.lynxlib.common.MagicValues;
import java.util.PriorityQueue;
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

    /**
     * Schedules a task to run after a delay. If {@code delay == 0}, {@code task} will be executed immediately.
     *
     * @param server The server to set the task for
     * @param delay  The delay in seconds
     * @param task   The task to run
     */
    public static void scheduleTask(MinecraftServer server, long delay, Runnable task) {
        long currentTick = server.getTickCount();
        long executionTick = currentTick + delay * MagicValues.TICKS_PER_SECOND;

        if (delay == 0) {
            task.run();
        } else {
            DelayedTask delayedTask = new DelayedTask(executionTick, task);
            taskQueue.add(delayedTask);
        }
    }

    /**
     * Server tick event.
     *
     * @param event Event
     */
    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !taskQueue.isEmpty()) {
            long currentTick = server.getTickCount();

            // Process tasks whose scheduled execution time has passed
            while (!taskQueue.isEmpty() && taskQueue.peek().executionTick <= currentTick) {
                DelayedTask taskToRun = taskQueue.poll();
                taskToRun.task.run();
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

        public DelayedTask(long executionTick, Runnable task) {
            this.executionTick = executionTick;
            this.task = task;
        }

        @Override
        public int compareTo(DelayedTask otherTask) {
            return Long.compare(this.executionTick, otherTask.executionTick);
        }
    }
}
