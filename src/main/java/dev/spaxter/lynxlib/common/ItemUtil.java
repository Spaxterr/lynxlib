package dev.spaxter.lynxlib.common;

import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Utility class for items.
 */
public class ItemUtil {

    /**
     * Get an item from its resource name.
     *
     * @param name The item name, E.g. `minecraft:dirt`
     * @return The Item object, or null if no item could be found for the given name
     */
    @Nullable
    public static Item getItemByName(String name) {
        ResourceLocation resourceLocation = new ResourceLocation(name);
        try {
            return ForgeRegistries.ITEMS.getValue(resourceLocation);
        } catch (ResourceLocationException e) {
            return null;
        }
    }

    /**
     * Rename an item stack.
     *
     * @param itemStack The item stack to rename
     * @param name      The name to set
     */
    public static void setItemName(ItemStack itemStack, String name) {
        StringTextComponent nameComponent = new ColoredTextComponent(name);
        CompoundNBT nbt = itemStack.getOrCreateTag();
        CompoundNBT displayTag = nbt.getCompound("display");

        displayTag.putString("Name", ITextComponent.Serializer.toJson(nameComponent));
        nbt.put("display", displayTag);
    }

    /**
     * Set the lore for an item stack.
     *
     * @param itemStack The item stack to set the lore for
     * @param lore      The lore to set
     */
    public static void setItemLore(ItemStack itemStack, String lore) {
        String[] lines = lore.split("\n");
        ListNBT loreList = new ListNBT();

        for (String line : lines) {
            ITextComponent loreLine = new ColoredTextComponent(line);
            loreList.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(loreLine)));
        }

        CompoundNBT nbt = itemStack.getOrCreateTag();
        CompoundNBT displayTag = nbt.getCompound("display");

        displayTag.put("Lore", loreList);
        nbt.put("display", displayTag);
    }

    /**
     * Set the lore for an item stack.
     *
     * @param itemStack The item stack to set the lore for
     * @param lore      The lore to set
     */
    @Nullable
    public static String getItemLore(ItemStack itemStack) {
        CompoundNBT nbt = itemStack.getTag();
        if (nbt != null && nbt.contains("display", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT displayTag = nbt.getCompound("display");
            if (displayTag.contains("Lore", Constants.NBT.TAG_LIST)) {
                ListNBT loreList = displayTag.getList("Lore", Constants.NBT.TAG_STRING);
                StringBuilder lore = new StringBuilder();
                for (int i = 0; i < loreList.size(); i++) {
                    ITextComponent textComponent = ITextComponent.Serializer.fromJson(loreList.getString(i));
                    if (textComponent != null) {
                        lore.append(textComponent.getString());
                        if (i < loreList.size() - 1) {
                            lore.append("\n");
                        }
                    }
                }
                return lore.length() > 0 ? lore.toString() : null;
            }
        }
        return null;
    }

    /**
     * Add or set an NBT tag for an item.
     *
     * @param itemStack The item stack
     * @param key       Key of the NBT
     * @param value     Value of the NBT as an integer
     */
    public static void setNbtTag(ItemStack itemStack, String key, int value) {
        CompoundNBT nbt = itemStack.getOrCreateTag();
        nbt.putInt(key, value);
    }

    /**
     * Add or set an NBT tag for an item.
     *
     * @param itemStack The item stack
     * @param key       Key of the NBT
     * @param value     Value of the NBT as a string
     */
    public static void setNbtTag(ItemStack itemStack, String key, String value) {
        CompoundNBT nbt = itemStack.getOrCreateTag();
        nbt.putString(key, value);
    }

    /**
     * Set an item's NBT tag from a JSON string.
     *
     * @param itemStack The item stack
     * @param nbtString NBT tag as a JSON string
     */
    public static void setNbt(ItemStack itemStack, String nbtString) throws CommandSyntaxException  {
        CompoundNBT nbt = JsonToNBT.parseTag(nbtString);
        itemStack.setTag(nbt);
    }

    public static boolean canFitItems(PlayerInventory inventory, Iterable<ItemStack> items) {
        NonNullList<ItemStack> simulatedInventory = NonNullList.withSize(inventory.items.size(), ItemStack.EMPTY);

        for (int i = 0; i < inventory.items.size(); i++) {
            simulatedInventory.set(i, inventory.items.get(i).copy());
        }

        for (ItemStack item : items) {
            int remaining = item.getCount();

            for (ItemStack existing : simulatedInventory) {
                if (ItemStack.isSame(existing, item) && ItemStack.tagMatches(existing, item)) {
                    int spaceLeft = existing.getMaxStackSize() - existing.getCount();
                    if (spaceLeft > 0) {
                        int toAdd = Math.min(remaining, spaceLeft);
                        existing.grow(toAdd);
                        remaining -= toAdd;
                    }
                }
                if (remaining == 0)
                    break;
            }

            while (remaining > 0) {
                int freeSlot = findFreeSlot(simulatedInventory);
                if (freeSlot == -1) {
                    return false;
                }

                int toAdd = Math.min(remaining, item.getMaxStackSize());
                simulatedInventory.set(freeSlot, new ItemStack(item.getItem(), toAdd));
                remaining -= toAdd;
            }
        }

        return true;
    }

    private static int findFreeSlot(NonNullList<ItemStack> inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
