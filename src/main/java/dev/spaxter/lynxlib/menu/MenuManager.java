package dev.spaxter.lynxlib.menu;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.concurrent.TickDelayedTask;

/**
 * Manager class for opening custom inventory menus.
 */
public class MenuManager {
    /**
     * Open a menu for a player. This will open the menu after 1 tick of the server to ensure the menu is opened safely
     * and does not conflict with other containers.
     *
     * @param player       The player to open the menu for.
     * @param menuProvider The container provider for the menu.
     */
    public static void openMenu(PlayerEntity player, INamedContainerProvider menuProvider) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            serverPlayer.server.tell(new TickDelayedTask(1, () -> player.openMenu(menuProvider)));
        }
    }

    /**
     * Close a player's currently open menu.
     *
     * @param player The player to close the menu for.
     */
    public static void closeMenu(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            serverPlayer.closeContainer();
        }
    }
}
