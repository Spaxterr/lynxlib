package dev.spaxter.lynxlib.common;

import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
}
