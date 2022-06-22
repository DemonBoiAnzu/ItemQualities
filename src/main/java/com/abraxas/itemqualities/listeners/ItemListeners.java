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
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import static com.abraxas.itemqualities.utils.Utils.chanceOf;
import static com.abraxas.itemqualities.utils.Utils.getConfig;

public class ItemListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void repairItemMend(PlayerItemMendEvent event) {
        if (event.isCancelled()) return;
        DurabilityManager.repairItem(event.getItem(), event.getRepairAmount());
    }

    @EventHandler
    public void enchantItem(EnchantItemEvent event) {
        var item = event.getItem();
        new BukkitRunnable() {
            @Override
            public void run() {
                QualitiesManager.refreshItem(item);
            }
        }.runTaskLater(main, 3);
    }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent event) {
        var inv = event.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            var item = inv.getItem(i);
            if (item != null) {
                var itemMeta = item.getItemMeta();
                if (itemMeta != null) {
                    if (itemMeta.getPersistentDataContainer().has(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER) &&
                            !QualitiesManager.itemCanHaveQuality(item)) {
                        itemMeta.getPersistentDataContainer().remove(Keys.ITEM_CRAFTED_KEY);
                        item.setItemMeta(itemMeta);
                    }
                    var canAdd = getConfig().applyQualityOnCraft ||
                            !itemMeta.getPersistentDataContainer().has(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER);
                    if (QualitiesManager.itemHasQuality(item) && canAdd) QualitiesManager.refreshItem(item);
                    if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item) && canAdd)
                        QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
                }
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
                var itemMeta = item.getItemMeta();
                if (itemMeta != null) {
                    if (itemMeta.getPersistentDataContainer().has(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER) &&
                            !QualitiesManager.itemCanHaveQuality(item)) {
                        itemMeta.getPersistentDataContainer().remove(Keys.ITEM_CRAFTED_KEY);
                        item.setItemMeta(itemMeta);
                    }
                    var canAdd = getConfig().applyQualityOnCraft ||
                            !itemMeta.getPersistentDataContainer().has(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER);
                    if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item) && canAdd)
                        QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
                }
            }
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        var item = event.getItem().getItemStack();
        var itemMeta = item.getItemMeta();
        if (!getConfig().applyQualityOnCraft &&
                itemMeta != null &&
                itemMeta.getPersistentDataContainer().has(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER)) return;
        if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item)) {
            QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
            event.getItem().setItemStack(item);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        var item = event.getCurrentItem();
        if (item == null) return;
        var itemMeta = item.getItemMeta();
        if (!getConfig().applyQualityOnCraft &&
                itemMeta != null &&
                itemMeta.getPersistentDataContainer().has(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER)) return;
        if (QualitiesManager.itemHasQuality(item)) {
            QualitiesManager.refreshItem(item);
            event.setCurrentItem(item);
        }
        if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item)) {
            QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
            event.setCurrentItem(item);
        }
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
        if (QualitiesManager.itemHasQuality(item))
            QualitiesManager.refreshItem(item);
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
                if (d.getType().getMaxStackSize() > 1) {
                    d.setAmount(d.getAmount() * 2);
                    player.getWorld().dropItemNaturally(event.getBlock().getLocation(), d);
                }
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
        if (event.getEntity() instanceof Player) return;

        var itemsQuality = QualitiesManager.getQuality(attackersItem);
        if (itemsQuality == null) return;
        if (itemsQuality.noDropChance > 0 && chanceOf(itemsQuality.noDropChance)) {
            event.getDrops().clear();
            return;
        }

        if (itemsQuality.doubleDropsChance > 0 && chanceOf(itemsQuality.doubleDropsChance)) {
            event.getDrops().forEach(d -> {
                if (d.getType().getMaxStackSize() > 1)
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
