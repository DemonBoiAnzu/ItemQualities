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
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return 0;

        return meta.getPersistentDataContainer().getOrDefault(Keys.MAX_ITEM_DURABILITY_KEY, PersistentDataType.INTEGER, 0);
    }

    public static int getItemCustomDamage(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return 0;

        return meta.getPersistentDataContainer().getOrDefault(Keys.ITEM_DURABILITY_KEY, PersistentDataType.INTEGER, 0);
    }

    public static int getItemMaxDurability(ItemStack itemStack) {
        return itemStack.getType().getMaxDurability();
    }

    public static int getItemDamage(ItemStack itemStack) {
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        if (damageable == null) return 0;
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
            itemStack.setItemMeta((ItemMeta) damageable);
            return;
        }

        customDur += damage;

        var newDur = ((float) customDur / customMaxDur) * maxDur;

        damageable.setDamage((int) newDur);

        ((ItemMeta) damageable).getPersistentDataContainer().set(Keys.ITEM_DURABILITY_KEY, PersistentDataType.INTEGER, customDur);

        itemStack.setItemMeta((ItemMeta) damageable);
        if (customDur >= customMaxDur)
            breakItem(player, itemStack);
    }

    public static void breakItem(Player player, ItemStack itemStack) {
        PlayerItemBreakEvent playerItemBreakEvent = new PlayerItemBreakEvent(player, itemStack);
        Bukkit.getPluginManager().callEvent(playerItemBreakEvent);
        itemStack.setAmount(0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1, 1);
    }
}
