package dev.spaxter.lynxlib.menu;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Custom slot class for "locked" menu slots. These slots can not be moved or edited by the player.
 */
public class LockedSlot extends SlotItemHandler {
    public LockedSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(@Nonnull PlayerEntity player) {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack onTake(@Nonnull PlayerEntity player, @Nonnull ItemStack itemStack) {
        return ItemStack.EMPTY;
    }
}
