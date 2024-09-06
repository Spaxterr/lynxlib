package dev.spaxter.lynxlib;

import dev.spaxter.lynxlib.task.TaskScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * LynxLib main class.
 */
public class LynxLib {
    public static Logger logger = LogManager.getLogger("lynxlib");

    public static void setLogger(String modId) {
        logger = LogManager.getLogger(modId);
    }

    public static void registerEvents(IEventBus eventBus) {
        eventBus.register(TaskScheduler.class);
    }
}
