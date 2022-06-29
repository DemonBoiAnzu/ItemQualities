package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.ItemQualities;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import static com.abraxas.itemqualities.QualitiesManager.*;
import static com.abraxas.itemqualities.api.DurabilityManager.damageItem;
import static com.abraxas.itemqualities.api.DurabilityManager.repairItem;
import static com.abraxas.itemqualities.api.Keys.ITEM_CRAFTED;
import static com.abraxas.itemqualities.api.Keys.ITEM_PROJECTILE_DAMAGE;
import static com.abraxas.itemqualities.utils.Utils.*;
import static java.lang.Math.abs;
import static org.bukkit.GameMode.SURVIVAL;
import static org.bukkit.Material.*;
import static org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE;
import static org.bukkit.persistence.PersistentDataType.DOUBLE;
import static org.bukkit.persistence.PersistentDataType.INTEGER;

public class ItemListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void repairItemMend(PlayerItemMendEvent event) {
        if (event.isCancelled()) return;
        repairItem(event.getItem(), event.getRepairAmount());
    }

    @EventHandler
    public void enchantItem(EnchantItemEvent event) {
        var item = event.getItem();
        new BukkitRunnable() {
            @Override
            public void run() {
                refreshItem(item);
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
                    if (itemMeta.getPersistentDataContainer().has(ITEM_CRAFTED, INTEGER) &&
                            !itemCanHaveQuality(item)) {
                        itemMeta.getPersistentDataContainer().remove(ITEM_CRAFTED);
                        item.setItemMeta(itemMeta);
                    }
                    var canAdd = getConfig().applyQualityOnCraft ||
                            !itemMeta.getPersistentDataContainer().has(ITEM_CRAFTED, INTEGER);
                    if (itemHasQuality(item) && canAdd) refreshItem(item);
                    if (itemCanHaveQuality(item) && !itemHasQuality(item) && canAdd)
                        addQualityToItem(item, getRandomQuality());
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
                    if (itemMeta.getPersistentDataContainer().has(ITEM_CRAFTED, INTEGER) &&
                            !itemCanHaveQuality(item)) {
                        itemMeta.getPersistentDataContainer().remove(ITEM_CRAFTED);
                        item.setItemMeta(itemMeta);
                    }
                    var canAdd = getConfig().applyQualityOnCraft ||
                            !itemMeta.getPersistentDataContainer().has(ITEM_CRAFTED, INTEGER);
                    if (itemCanHaveQuality(item) && !itemHasQuality(item) && canAdd)
                        addQualityToItem(item, getRandomQuality());
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
                itemMeta.getPersistentDataContainer().has(ITEM_CRAFTED, INTEGER)) return;
        if (itemCanHaveQuality(item) && !itemHasQuality(item)) {
            addQualityToItem(item, getRandomQuality());
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
                itemMeta.getPersistentDataContainer().has(ITEM_CRAFTED, INTEGER)) return;
        if (itemHasQuality(item)) {
            refreshItem(item);
            event.setCurrentItem(item);
        }
        if (itemCanHaveQuality(item) && !itemHasQuality(item)) {
            addQualityToItem(item, getRandomQuality());
            event.setCurrentItem(item);
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        var player = event.getPlayer();
        var item = event.getItem();
        event.setCancelled(true);
        var itemsQuality = getQuality(item);
        int damage = event.getDamage();
        if (itemsQuality != null) {
            if (itemsQuality.noDurabilityLossChance > 0) {
                if (chanceOf(itemsQuality.noDurabilityLossChance))
                    return;
            }
            if (chanceOf(itemsQuality.extraDurabilityLossChance))
                damage += abs(itemsQuality.extraDurabilityLoss);
        }
        damageItem(player, item, damage);
        if (itemHasQuality(item))
            refreshItem(item);
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        var block = event.getBlock();
        var player = event.getPlayer();
        if (!player.getGameMode().equals(SURVIVAL)) return;
        var item = player.getInventory().getItemInMainHand();
        if (item.getType().equals(AIR)) return;
        if (!itemHasQuality(item)) return;
        if (block.getState() instanceof InventoryHolder) return;

        var itemsQuality = getQuality(item);
        if (itemsQuality == null) return;
        if (itemsQuality.noDropChance > 0 && chanceOf(itemsQuality.noDropChance)) {
            event.setDropItems(false);
            return;
        }

        if (!isOre(block) &&
                !block.getType().equals(GLOWSTONE) &&
                !block.getType().equals(PRISMARINE) &&
                !block.getType().equals(CLAY)) return;

        if (itemsQuality.doubleDropsChance > 0 && chanceOf(itemsQuality.doubleDropsChance)) {
            event.setDropItems(false);
            var drops = block.getDrops(item, player);
            drops.forEach(d -> {
                if (d.getType().getMaxStackSize() > 1) {
                    d.setAmount(d.getAmount() * 2);
                    player.getWorld().dropItemNaturally(block.getLocation(), d);
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
        if (attackersItem.getType().equals(AIR)) return;
        if (!itemHasQuality(attackersItem)) return;
        if (event.getEntity() instanceof Player) return;

        var itemsQuality = getQuality(attackersItem);
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

        if (itemMeta.getAttributeModifiers(GENERIC_ATTACK_DAMAGE) == null) return;
        var atkDmgMod = itemMeta.getAttributeModifiers(GENERIC_ATTACK_DAMAGE).stream().findFirst();
        atkDmgMod.ifPresent(attributeModifier -> proj.getPersistentDataContainer().set(ITEM_PROJECTILE_DAMAGE, DOUBLE, attributeModifier.getAmount()));
    }

    @EventHandler
    public void onEntityHitByProjectile(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile proj)) return;
        var value = proj.getPersistentDataContainer().getOrDefault(ITEM_PROJECTILE_DAMAGE, DOUBLE, 0d);
        event.setDamage(event.getDamage() + value);
    }
}
