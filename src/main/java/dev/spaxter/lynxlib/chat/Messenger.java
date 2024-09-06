package dev.spaxter.lynxlib.chat;

import dev.spaxter.lynxlib.common.ColoredTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextComponent;

/**
 * Util class for messaging players.
 */
public class Messenger {

    private static final String DEBUG_PREFIX = "&8[&bDebug&8]&r";
    private static final String ERROR_PREFIX = "&8[&4Error&8]&r";
    private static final String INFO_PREFIX = "&7[&bInfo&7]&r";
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
    public static void debug(final PlayerEntity player, final String message, Class<?> context) {
        String prefix = DEBUG_PREFIX + " " + CONTEXT_PREFIX.replace("%context%", context.getSimpleName());
        TextComponent chatMessage = formatMessage(prefix + " &7" + message);
        player.sendMessage(chatMessage, player.getUUID());
    }

    /**
     * Send an error message to a player.
     *
     * @param player  The player to message
     * @param error   The exception object
     * @param context The class the message was sent from
     */
    public static void error(final PlayerEntity player, final Exception error, Class<?> context) {
        String prefix = ERROR_PREFIX + " " + CONTEXT_PREFIX.replace("%context%", context.getSimpleName());
        TextComponent chatMessage = formatMessage(prefix + " &c" + error.getLocalizedMessage());
        player.sendMessage(chatMessage, player.getUUID());
    }

    /**
     * Send an informational message to a player.
     *
     * @param player  The player to message
     * @param message The info message
     */
    public static void info(final PlayerEntity player, final String message) {
        TextComponent chatMessage = formatMessage(INFO_PREFIX + " &f" + message);
        player.sendMessage(chatMessage, player.getUUID());
    }
}
