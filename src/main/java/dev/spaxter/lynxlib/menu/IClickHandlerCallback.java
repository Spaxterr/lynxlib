package dev.spaxter.lynxlib.menu;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;

public interface IClickHandlerCallback {
    public void operation(Slot clickedSlot, PlayerEntity player);
}
