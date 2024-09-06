package dev.spaxter.lynxlib.menu;

import dev.spaxter.lynxlib.common.ColoredTextComponent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;

/**
 * Base class for container provider used for menus.
 */
public class MenuProvider implements INamedContainerProvider {
    private String title;

    public void setTitle(String title) {
        this.title = title;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new ColoredTextComponent(this.title);
    }

    @Nullable
    @Override
    public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player) {
        return null;
    }
}
