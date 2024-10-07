package dev.spaxter.lynxlib.chat;

import dev.spaxter.lynxlib.common.ColoredTextComponent;
import io.netty.handler.logging.LogLevel;
import javax.annotation.Nullable;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextComponent;

/**
 * Util class for messaging players.
 */
public class Messenger {

    public static void setLogLevel(LogLevel level) {
        logLevel = level;
    }

    public static void setPrefix(String prefix) {
        INFO_PREFIX = prefix;
    }

    private static LogLevel logLevel = LogLevel.INFO;
    private static final String DEBUG_PREFIX = "&8[&bDebug&8]&r";
    private static final String ERROR_PREFIX = "&8[&4Error&8]&r";
    private static String INFO_PREFIX = "&7[&bInfo&7]&r";
    private static final String CONTEXT_PREFIX = "&8[&7%context%&8]&r";

    public static String translateColorCodes(String message) {
        return message.replaceAll("&([0-9a-fk-or])", "§$1");
    }

    private static TextComponent formatMessage(String message) {
        return new ColoredTextComponent(message);
    }

    /**
     * Send a debug message to a player.
     *
     * @param player  The player to message
     * @param message The message to send
     * @param context The class the message was sent from
     */
    public static void debug(final @Nullable PlayerEntity player, final String message, Class<?> context) {
        if (logLevel.compareTo(LogLevel.DEBUG) <= 0) {
            if (player != null) {
                String prefix = DEBUG_PREFIX + " " + CONTEXT_PREFIX.replace("%context%", context.getSimpleName());
                TextComponent chatMessage = formatMessage(prefix + " &7" + message);
                player.sendMessage(chatMessage, player.getUUID());
            }
        }
    }

    /**
     * Send an error message to a player.
     *
     * @param player  The player to message
     * @param error   The exception object
     * @param context The class the message was sent from
     */
    public static void error(final @Nullable PlayerEntity player, final Exception error, Class<?> context) {
        if (player != null) {
            String prefix =
                INFO_PREFIX + " " + ERROR_PREFIX + " " + CONTEXT_PREFIX.replace("%context%", context.getSimpleName());
            TextComponent chatMessage = formatMessage(prefix + " &c" + error.getLocalizedMessage());
            player.sendMessage(chatMessage, player.getUUID());
        }
    }

    /**
     * Send an informational message to a player.
     *
     * @param player  The player to message
     * @param message The info message
     */
    public static void info(final @Nullable PlayerEntity player, final String message) {
        if (player != null) {
            TextComponent chatMessage = formatMessage(INFO_PREFIX + " &f" + message);
            player.sendMessage(chatMessage, player.getUUID());
        }
    }

    /**
     * Send an informational message to a CommandSource instance.
     *
     * @param source  The command source
     * @param message The info message
     */
    public static void info(final @Nullable CommandSource source, final String message) {
       if (source != null) {
            TextComponent chatMessage = formatMessage(INFO_PREFIX + " &f" + message);
            source.sendSuccess(chatMessage, false);
        }
    }
}
