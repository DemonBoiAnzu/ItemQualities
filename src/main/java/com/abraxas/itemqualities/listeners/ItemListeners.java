package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.Config;
import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.api.DurabilityManager;
import com.abraxas.itemqualities.api.Keys;
import com.abraxas.itemqualities.api.quality.ItemQuality;
import com.abraxas.itemqualities.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import static com.abraxas.itemqualities.utils.Utils.log;

public class ItemListeners implements Listener {
    // TODO: Make no drops/double drops function for weapons/tools
    ItemQualities main = ItemQualities.getInstance();

    Config config = main.getConfiguration();

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item))
                    QualitiesManager.addQualityToItem(item,QualitiesManager.getRandomQuality());
            }
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 35; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item))
                    QualitiesManager.addQualityToItem(item,QualitiesManager.getRandomQuality());
            }
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item)){
            QualitiesManager.addQualityToItem(item,QualitiesManager.getRandomQuality());
            event.getItem().setItemStack(item);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item)) {
            QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
            event.setCurrentItem(item);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.isRepair()) event.getInventory().setResult(new ItemStack(Material.AIR));
        if (event.getInventory().getResult() == null || event.getInventory().getResult().getType().equals(Material.AIR))
            return;
        ItemStack item = event.getInventory().getResult().clone();

        if (QualitiesManager.itemCanHaveQuality(item)) {
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        event.getInventory().setResult(item);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        event.getInventory().getViewers().forEach(humanEntity -> {
            Player player = (Player) humanEntity;
            ItemStack item = event.getInventory().getResult();
            if (item == null) return;
            var itemMeta = item.getItemMeta();
            itemMeta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(itemMeta);
            event.setCurrentItem(item);
            var action = event.getAction();
            if (action.equals(InventoryAction.HOTBAR_SWAP) ||
                    action.equals(InventoryAction.SWAP_WITH_CURSOR) ||
                    action.equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
                if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item))
                    QualitiesManager.addQualityToItem(item,QualitiesManager.getRandomQuality());
                event.setCancelled(true);
                return;
            }
            if (event.isShiftClick()) {
                final ItemStack[] preInv = player.getInventory().getContents();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        final ItemStack[] postInv = player.getInventory().getContents();

                        for (int i = 0; i < preInv.length; i++) {
                            if (preInv[i] != postInv[i]) {
                                if (QualitiesManager.itemCanHaveQuality(postInv[i]) && !QualitiesManager.itemHasQuality(postInv[i])) {
                                    QualitiesManager.addQualityToItem(postInv[i],QualitiesManager.getRandomQuality());
                                    var itemMeta = postInv[i].getItemMeta();
                                    itemMeta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                                    postInv[i].setItemMeta(itemMeta);
                                    player.getInventory().setItem(i, postInv[i]);
                                }
                            }
                        }
                    }
                }.runTaskLater(main, 5);
                return;
            }
            if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item)){
                QualitiesManager.addQualityToItem(item,QualitiesManager.getRandomQuality());
                event.setCurrentItem(item);
            }
        });
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        event.setCancelled(true);
        ItemQuality itemsQuality = QualitiesManager.getQuality(item);
        int damage = event.getDamage();
        if (itemsQuality != null) {
            if (itemsQuality.noDurabilityLossChance > 0) {
                if (Utils.chanceOf(itemsQuality.noDurabilityLossChance))
                    return;
            }
            if (Utils.chanceOf(itemsQuality.extraDurabilityLossChance)) damage += Math.abs(itemsQuality.extraDurabilityLoss);
        }
        DurabilityManager.damageItem(player, item, damage);
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) return;
        if (!QualitiesManager.itemHasQuality(item)) return;

        ItemQuality itemsQuality = QualitiesManager.getQuality(item);
        if (itemsQuality == null) return;
        event.setDropItems(itemsQuality.noDropChance <= 0 || !Utils.chanceOf(itemsQuality.noDropChance));
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        var proj = event.getEntity();

        if (!(proj.getShooter() instanceof LivingEntity entity)) return;
        var equipment = entity.getEquipment();

        if (equipment == null) return;
        var item = equipment.getItemInMainHand();
        var itemMeta = item.getItemMeta();

        if (itemMeta == null || !itemMeta.hasAttributeModifiers()) return;

        if (itemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE) == null) return;
        var atkDmgMod = itemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE).stream().findFirst();
        if (atkDmgMod.isPresent()) {
            var value = atkDmgMod.get().getAmount();
            proj.getPersistentDataContainer().set(Keys.ITEM_PROJECTILE_DAMAGE_KEY, PersistentDataType.DOUBLE, value);
            log("Setting damage from ItemQuality to projectile: %s".formatted(value));
        }
    }

    @EventHandler
    public void onEntityHitByProjectile(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile proj)) return;
        log("Initial damage: %s".formatted(event.getDamage()));
        var value = proj.getPersistentDataContainer().getOrDefault(Keys.ITEM_PROJECTILE_DAMAGE_KEY, PersistentDataType.DOUBLE, 0d);
        event.setDamage(event.getDamage() + value);
        log("New damage: %s".formatted(event.getDamage()));
    }
}
