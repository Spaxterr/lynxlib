package dev.spaxter.lynxlib.menu;

import dev.spaxter.lynxlib.chat.Messenger;
import dev.spaxter.lynxlib.common.ItemUtil;
import dev.spaxter.lynxlib.common.MagicValues;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

/**
 * Base class for inventory menus.
 */
public abstract class MenuBase extends Container {

    protected final ItemStackHandler itemStackHandler;
    private final Map<Slot, IClickHandlerCallback> clickHandlers;
    public final PlayerInventory playerInventory;
    public final int rows;
    public final int columns;
    public boolean valid = true;

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public MenuBase(final ContainerType<?> containerType, PlayerInventory playerInventory, final int id,
        final int columns, final int rows) {
        super(containerType, id);
        this.clickHandlers = new HashMap<>();

        this.playerInventory = playerInventory;
        this.rows = rows;
        this.columns = columns;
        int slotCount = columns * rows;

        this.itemStackHandler = new ItemStackHandler(slotCount);

        this.initSlots();
        this.initPlayerInventorySlots();
    }

    /**
     * Set a click callback function for a slot.
     *
     * @param row      The row of the slot
     * @param col      The column of the slot
     * @param function The function to run on click
     */
    public void setClickHandler(int row, int col, IClickHandlerCallback function) {
        final int slot = (row - 1) * this.columns + (col - 1);
        this.setClickHandler(slot, function);
    }

    /**
     * Set a click callback function for a slot.
     *
     * @param slot     The slot to set the click handler for.
     * @param function The function to run on click.
     */
    public void setClickHandler(int slot, IClickHandlerCallback function) {
        clickHandlers.put(this.slots.get(slot), function);
        Messenger.debug(this.playerInventory.player, "Set click handler at slot &e" + slot, this.getClass());
    }

    /**
     * Set an item at the given slot index.
     *
     * @param item The item to set
     * @param slot The slot index
     * @throws IllegalArgumentException  If the given row or column is an invalid value
     * @throws IndexOutOfBoundsException If the calculated slot index is out of bounds for the inventory
     */
    public void setItemAt(final ItemStack item, final int slot) {
        if (item == null) {
            return;
        }
        this.itemStackHandler.setStackInSlot(slot, item);
    }

    /**
     * Set an item at the given row and column.
     *
     * @param item The item to set
     * @param row  Row number
     * @param col  Column number
     * @throws IllegalArgumentException  If the given row or column is an invalid value
     * @throws IndexOutOfBoundsException If the calculated slot index is out of bounds for the inventory
     */
    public void setItemAt(final ItemStack item, final int row, final int col) {
        final int slot = (row - 1) * this.columns + (col - 1);
        validateCoordinates(row, col, slot);
        this.setItemAt(item, slot);
    }

    /**
     * Set the background of the inventory.
     *
     * @param item The item to use as the background
     */
    public void setBackground(final Item item) {
        ItemStack itemStack = new ItemStack(item);
        ItemUtil.setItemName(itemStack, "");
        this.fillWith(itemStack);
    }

    /**
     * Fill all empty slots in the inventory with a given item.
     *
     * @param item The item to fill the menu with
     */
    public void fillWith(final ItemStack item) {
        for (short i = 0; i < this.itemStackHandler.getSlots(); i++) {
            Item itemInSlot = this.itemStackHandler.getStackInSlot(i).getItem();
            if (itemInSlot == Items.AIR) {
                this.itemStackHandler.setStackInSlot(i, item);
            }
        }
    }

    /**
     * Change the menu's valid state.
     *
     * @param valid If the container should be set to valid or not
     */
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    /**
     * Close this menu.
     */
    public void close() {
        this.clickHandlers.clear();
        this.removed(this.playerInventory.player);
        this.setValid(false);
        this.playerInventory.player.closeContainer();
    }

    /**
     * Create the slots for the inventory menu.
     */
    private void initSlots() {
        int index = 0;
        for (int row = 0; row < this.rows; row++) { // 3 rows
            for (int col = 0; col < this.columns; col++) { // 9 columns
                int x = MagicValues.INVENTORY_SLOT_HORIZONTAL_OFFSET + col * MagicValues.INVENTORY_SLOT_HEIGHT;
                int y = MagicValues.INVENTORY_SLOT_HEIGHT + row * MagicValues.INVENTORY_SLOT_HEIGHT;
                this.addSlot(new LockedSlot(this.itemStackHandler, index++, x, y));
            }
        }
        Messenger.debug(this.playerInventory.player, "Initialized menu slots", this.getClass());
    }

    /**
     * Create the slots for the player's inventory to prevent internal Forge errors when clicking items.
     */
    private void initPlayerInventorySlots() {
        PlayerInvWrapper playerInvWrapper = new PlayerInvWrapper(this.playerInventory);

        // Add the player's main inventory slots (9-35 in the default player inventory)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = MagicValues.INVENTORY_SLOT_HORIZONTAL_OFFSET + col * MagicValues.INVENTORY_SLOT_HEIGHT;
                int y = MagicValues.PLAYER_INVENTORY_VERTICAL_OFFSET + row * MagicValues.INVENTORY_SLOT_HEIGHT;
                int index = col + row * 9 + 9;
                this.addSlot(new SlotItemHandler(playerInvWrapper, index, x, y));
            }
        }

        // Add the player's hotbar slots (0-8 in the default player inventory)
        for (int col = 0; col < 9; col++) {
            int x = MagicValues.INVENTORY_SLOT_HORIZONTAL_OFFSET + col * MagicValues.INVENTORY_SLOT_HEIGHT;
            int y = MagicValues.PLAYER_HOTBAR_VERTICAL_OFFSET;
            this.addSlot(new SlotItemHandler(playerInvWrapper, col, x, y));
        }
    }


    /**
     * Validate provided coordinates in the inventory.
     */
    private void validateCoordinates(final int row, final int col, final int slot) {
        if (row < 1 || col < 1) {
            throw new IllegalArgumentException(
                "Row and column indices must be greater than 0. Provided row: " + row
                    + ", column: " + col);
        }

        if (row > this.rows) {
            throw new IllegalArgumentException(
                "Row must be less than the configured row count for the inventory. Provided row: " + row
                    + ", configured row count: " + this.rows);
        }

        if (col > this.columns) {
            throw new IllegalArgumentException(
                "Column must be less than the configured row count for the inventory. Provided column: " + col
                    + ", configured column count: " + this.columns);
        }

        if (slot >= this.itemStackHandler.getSlots()) {
            throw new IndexOutOfBoundsException(
                "The calculated slot index " + slot + " is out of bounds for the inventory. Maximum allowed index: "
                    + (this.itemStackHandler.getSlots() - 1));
        }
    }

    private void refresh(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            serverPlayer.refreshContainer(this, this.getItems());
            this.broadcastChanges();
            this.clearPlayerCursor(player);
        }
        Messenger.debug(this.playerInventory.player, "Refreshed menu container", this.getClass());
    }

    private void clearPlayerCursor(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            SSetSlotPacket packet = new SSetSlotPacket(-1, -1, ItemStack.EMPTY);
            ((ServerPlayerEntity) player).connection.send(packet);
        }
        Messenger.debug(this.playerInventory.player, "Cleared player cursor", this.getClass());
    }

    /* Override methods to prevent player's taking items from the inventory */

    @Override
    @Nonnull
    public ItemStack clicked(int slot, int button, @Nonnull ClickType clickType, @Nonnull PlayerEntity player) {
        Messenger.debug(player, "You clicked slot &e" + slot, this.getClass());
        this.refresh(player);

        if (slot < 0 || clickType == ClickType.THROW) {
            return ItemStack.EMPTY;
        }

        Slot clicked = this.slots.get(slot);
        if (clickHandlers.containsKey(clicked)) {
            try {
                clickHandlers.get(clicked).operation(clicked, player);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(@Nullable ItemStack stack, @Nullable Slot slot) {
        return false;
    }

    @Override
    public boolean canDragTo(@Nullable Slot slot) {
        return false;
    }

    @Override
    public boolean stillValid(@Nullable PlayerEntity player) {
        return this.valid;
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull PlayerEntity player, int index) {
        this.refresh(player);
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean moveItemStackTo(@Nonnull ItemStack item, int x, int y, boolean bool) {
        return false;
    }

    @Override
    public void removed(@Nonnull PlayerEntity player) {
        this.clearPlayerCursor(player);
        super.removed(player);
        this.setValid(false);
        Messenger.debug(this.playerInventory.player, "Container removed", this.getClass());
    }

    @Override
    public void setItem(int slot, @Nonnull ItemStack item) {
    }
}
