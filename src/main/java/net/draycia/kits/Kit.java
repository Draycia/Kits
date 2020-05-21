package net.draycia.kits;

import org.black_ixx.bossshop.core.BSShop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class Kit {

    private ArrayList<KitItem> items;
    private String name;
    private long cooldown;
    private BSShop shop = null;

    public Kit(ArrayList<KitItem> items, String name, long cooldown) {
        this.items = items;
        this.name = name;
        this.cooldown = cooldown;
    }

    public void addItem(ItemStack... itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            addItem(itemStack);
        }
    }

    public void addItem(ItemStack itemStack) {
        addItem(-1, itemStack);
    }

    public void addItem(int index, ItemStack itemStack) {
        items.add(new KitItem(index, itemStack));
    }

    public void giveToPlayer(Player player, boolean ignoreSlots) {
        if (!ignoreSlots) {
            for (KitItem item : items) {
                if (item.getSlot() < 0) {
                    player.getInventory().addItem(item.getItemStack());
                } else {
                    player.getInventory().setItem(item.getSlot(), item.getItemStack());
                }
            }
        } else {
            player.getInventory().addItem((ItemStack[]) items.stream().map(KitItem::getItemStack).toArray());
        }
    }

    public String getName() {
        return name;
    }

    public long getCooldown() {
        return cooldown;
    }

    public BSShop getShop() {
        return shop;
    }

    public void setShop(BSShop shop) {
        this.shop = shop;
    }
}
