package net.draycia.kits;

import org.bukkit.inventory.ItemStack;

public class KitItem {

    private int slot;
    private ItemStack itemStack;

    public KitItem(int slot, ItemStack itemStack) {
        this.slot = slot;
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getSlot() {
        return slot;
    }

}
