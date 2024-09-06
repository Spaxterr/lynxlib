package dev.spaxter.lynxlib.task;

import dev.spaxter.lynxlib.common.MagicValues;
import java.util.PriorityQueue;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Basic task scheduler for server-side tasks.
 */
public class TaskScheduler {

    private static final PriorityQueue<DelayedTask> taskQueue = new PriorityQueue<>();

    /**
     * Schedules a task to run after a delay.
     *
     * @param delay The delay in seconds
     * @param task  The task to run
     */
    public static void scheduleTask(MinecraftServer server, int delay, Runnable task) {
        long currentTick = server.getTickCount();
        long executionTick = currentTick + (long) delay * MagicValues.TICKS_PER_SECOND;

        DelayedTask delayedTask = new DelayedTask(executionTick, task);
        taskQueue.add(delayedTask);
    }

    /**
     * Server tick event.
     *
     * @param event Event
     */
    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !taskQueue.isEmpty()) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            long currentTick = server.getTickCount();

            // Process tasks whose scheduled execution time has passed
            while (!taskQueue.isEmpty() && taskQueue.peek().executionTick <= currentTick) {
                DelayedTask taskToRun = taskQueue.poll();
                taskToRun.task.run();
            }
        }
    }

    private static class DelayedTask {
        private final long executionTick;
        private final Runnable task;

        public DelayedTask(long executionTick, Runnable task) {
            this.executionTick = executionTick;
            this.task = task;
        }
    }
}
