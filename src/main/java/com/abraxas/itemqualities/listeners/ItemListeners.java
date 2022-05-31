package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.api.DurabilityManager;
import com.abraxas.itemqualities.api.Keys;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import static com.abraxas.itemqualities.utils.Utils.chanceOf;

public class ItemListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent event) {
        var inv = event.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            var item = inv.getItem(i);
            if (item != null) {
                if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item))
                    QualitiesManager.addQualityToItem(item,QualitiesManager.getRandomQuality());
            }
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        var player = (Player) event.getPlayer();
        var inventory = player.getInventory();
        for (int i = 0; i < 35; i++) {
            var item = inventory.getItem(i);
            if (item != null) {
                if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item))
                    QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
            }
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        var item = event.getItem().getItemStack();
        if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item)){
            QualitiesManager.addQualityToItem(item,QualitiesManager.getRandomQuality());
            event.getItem().setItemStack(item);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        var item = event.getCurrentItem();
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
        var item = event.getInventory().getResult().clone();

        if (QualitiesManager.itemCanHaveQuality(item)) {
            var meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        event.getInventory().setResult(item);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        event.getInventory().getViewers().forEach(humanEntity -> {
            var player = (Player) humanEntity;
            var item = event.getInventory().getResult();
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
                var preInv = player.getInventory().getContents();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        var postInv = player.getInventory().getContents();

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
        var player = event.getPlayer();
        var item = event.getItem();
        event.setCancelled(true);
        var itemsQuality = QualitiesManager.getQuality(item);
        int damage = event.getDamage();
        if (itemsQuality != null) {
            if (itemsQuality.noDurabilityLossChance > 0) {
                if (Utils.chanceOf(itemsQuality.noDurabilityLossChance))
                    return;
            }
            if (Utils.chanceOf(itemsQuality.extraDurabilityLossChance))
                damage += Math.abs(itemsQuality.extraDurabilityLoss);
        }
        DurabilityManager.damageItem(player, item, damage);
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        var player = event.getPlayer();
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;
        var item = player.getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) return;
        if (!QualitiesManager.itemHasQuality(item)) return;

        var itemsQuality = QualitiesManager.getQuality(item);
        if (itemsQuality == null) return;
        if (itemsQuality.noDropChance > 0 && chanceOf(itemsQuality.noDropChance)) {
            event.setDropItems(false);
            return;
        }

        if (itemsQuality.doubleDropsChance > 0 && chanceOf(itemsQuality.doubleDropsChance)) {
            event.setDropItems(false);
            var drops = event.getBlock().getDrops(item, player);
            drops.forEach(d -> {
                d.setAmount(d.getAmount() * 2);
                player.getWorld().dropItemNaturally(event.getBlock().getLocation(), d);
            });
        }
    }

    @EventHandler
    public void onKillEntity(EntityDeathEvent event) {
        if (!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent dmg)) return;
        if (!(dmg.getDamager() instanceof LivingEntity attacker)) return;
        var attackersEquipment = attacker.getEquipment();
        if (attackersEquipment == null) return;
        var attackersItem = attackersEquipment.getItemInMainHand();
        if (attackersItem.getType().equals(Material.AIR)) return;
        if (!QualitiesManager.itemHasQuality(attackersItem)) return;

        var itemsQuality = QualitiesManager.getQuality(attackersItem);
        if (itemsQuality == null) return;
        if (itemsQuality.noDropChance > 0 && chanceOf(itemsQuality.noDropChance)) {
            event.getDrops().clear();
            return;
        }

        if (itemsQuality.doubleDropsChance > 0 && chanceOf(itemsQuality.doubleDropsChance)) {
            event.getDrops().forEach(d -> {
                d.setAmount(d.getAmount() * 2);
            });
        }
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
        atkDmgMod.ifPresent(attributeModifier -> proj.getPersistentDataContainer().set(Keys.ITEM_PROJECTILE_DAMAGE_KEY, PersistentDataType.DOUBLE, attributeModifier.getAmount()));
    }

    @EventHandler
    public void onEntityHitByProjectile(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile proj)) return;
        var value = proj.getPersistentDataContainer().getOrDefault(Keys.ITEM_PROJECTILE_DAMAGE_KEY, PersistentDataType.DOUBLE, 0d);
        event.setDamage(event.getDamage() + value);
    }
}
