package com.abraxas.itemqualities.api;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

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
        //if (getItemCustomDamage(itemStack) > 0) return getItemCustomDamage(itemStack);
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
        int curDamage = getItemDamage(itemStack);
        int maxDur = getItemMaxDurability(itemStack);

        // Since this key is now unused, remove it from any item that has it
        //  this is so we're not clogging the data container with unused keys, eventually this code
        //  and the key itself will be removed (1.0.8 possibly)
        // TODO: Remove this and key next update
        if (damageable.getPersistentDataContainer().has(Keys.ITEM_DURABILITY, PersistentDataType.INTEGER))
            damageable.getPersistentDataContainer().remove(Keys.ITEM_DURABILITY);

        if (maxDur == itemStack.getType().getMaxDurability()) {
            curDamage += damage;
            if (curDamage >= maxDur) {
                breakItem(player, itemStack);
                return;
            }
            if (curDamage < 0) curDamage = 0;
            damageable.setDamage(curDamage);
            itemStack.setItemMeta(damageable);
            return;
        }

        curDamage += damage;
        var newDur = ((float) curDamage / maxDur) * itemStack.getType().getMaxDurability();
        if (newDur < 0) newDur = 0;
        damageable.setDamage((int) newDur);
        itemStack.setItemMeta(damageable);
        if (curDamage >= maxDur)
            breakItem(player, itemStack);
    }

    public static void breakItem(Player player, ItemStack itemStack) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (itemStack.getType().equals(Material.AIR)) {
                    cancel();
                    return;
                }
                PlayerItemBreakEvent playerItemBreakEvent = new PlayerItemBreakEvent(player, itemStack);
                Bukkit.getPluginManager().callEvent(playerItemBreakEvent);
                itemStack.setAmount(0);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1, 1);
            }
        }.runTaskLater(ItemQualities.getInstance(), 3);
    }

    public static void repairItem(ItemStack itemStack) {
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        if (!(itemMeta instanceof Damageable damageable)) return;
        repairItem(itemStack, damageable.getDamage());
    }

    public static void repairItem(ItemStack itemStack, int amount) {
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        if (!(itemMeta instanceof Damageable damageable)) return;
        var curDamage = getItemDamage(itemStack);
        damageable.setDamage(curDamage - amount);
        itemStack.setItemMeta(damageable);
        var itemsQuality = QualitiesManager.getQuality(itemStack);
        QualitiesManager.removeQualityFromItem(itemStack);
        QualitiesManager.addQualityToItem(itemStack, itemsQuality);
    }
}
