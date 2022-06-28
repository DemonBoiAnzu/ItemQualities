package com.abraxas.itemqualities.api;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class DurabilityManager {
    public static int getItemCustomMaxDurability(ItemStack itemStack) {
        var meta = itemStack.getItemMeta();
        if (meta == null) return 0;

        return meta.getPersistentDataContainer().getOrDefault(Keys.MAX_ITEM_DURABILITY, PersistentDataType.INTEGER, 0);
    }

    public static int getItemCustomDamage(ItemStack itemStack) {
        var meta = itemStack.getItemMeta();
        if (meta == null) return 0;

        return meta.getPersistentDataContainer().getOrDefault(Keys.ITEM_DURABILITY, PersistentDataType.INTEGER, 0);
    }

    public static int getItemMaxDurability(ItemStack itemStack) {
        if (getItemCustomMaxDurability(itemStack) > 0) return getItemCustomMaxDurability(itemStack);
        return itemStack.getType().getMaxDurability();
    }

    public static int getItemDamage(ItemStack itemStack) {
        if (getItemCustomDamage(itemStack) > 0) return getItemCustomDamage(itemStack);
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;
        if (!(itemMeta instanceof Damageable damageable)) return 0;
        return damageable.getDamage();
    }

    public static void damageItem(Player player, ItemStack itemStack, int damage) {
        ItemMeta meta = itemStack.getItemMeta();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        if (meta == null) return;
        if (!(meta instanceof Damageable damageable)) return;
        int curDur = getItemDamage(itemStack);
        int maxDur = getItemMaxDurability(itemStack);
        int customMaxDur = getItemCustomMaxDurability(itemStack);
        int customDur = getItemCustomDamage(itemStack);

        if (customMaxDur <= 0) {
            curDur += damage;
            if (curDur >= maxDur) {
                breakItem(player, itemStack);
                return;
            }
            damageable.setDamage(curDur);
            itemStack.setItemMeta(damageable);
            return;
        }

        customDur += damage;

        var newDur = ((float) customDur / customMaxDur) * maxDur;

        damageable.setDamage((int) newDur);

        damageable.getPersistentDataContainer().set(Keys.ITEM_DURABILITY, PersistentDataType.INTEGER, customDur);

        itemStack.setItemMeta(damageable);
        if (customDur >= customMaxDur)
            breakItem(player, itemStack);
    }

    public static void breakItem(Player player, ItemStack itemStack) {
        PlayerItemBreakEvent playerItemBreakEvent = new PlayerItemBreakEvent(player, itemStack);
        Bukkit.getPluginManager().callEvent(playerItemBreakEvent);
        itemStack.setAmount(0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1, 1);
    }

    public static void repairItem(ItemStack itemStack) {
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        if (!(itemMeta instanceof Damageable damageable)) return;
        int customMaxDur = getItemCustomMaxDurability(itemStack);
        int customDur = getItemCustomDamage(itemStack);
        if (customMaxDur <= 0) repairItem(itemStack, damageable.getDamage());
        else repairItem(itemStack, customDur);
    }

    public static void repairItem(ItemStack itemStack, int amount) {
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        if (!(itemMeta instanceof Damageable damageable)) return;
        int curDur = getItemDamage(itemStack);
        int customMaxDur = getItemCustomMaxDurability(itemStack);
        int customDur = getItemCustomDamage(itemStack);
        if (customMaxDur >= 0) {
            damageable.setDamage(curDur - amount);
            itemStack.setItemMeta(damageable);
            return;
        }
        damageable.getPersistentDataContainer().set(Keys.ITEM_DURABILITY, PersistentDataType.INTEGER, customDur - amount);
        itemStack.setItemMeta(damageable);
    }
}
