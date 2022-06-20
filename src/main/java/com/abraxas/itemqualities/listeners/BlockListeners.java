package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.api.DurabilityManager;
import com.abraxas.itemqualities.api.Keys;
import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.api.quality.ItemQuality;
import com.abraxas.itemqualities.utils.Permissions;
import com.abraxas.itemqualities.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static com.abraxas.itemqualities.utils.Utils.getConfig;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;

public class BlockListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void repairItemAnvil(PrepareAnvilEvent event) {
        var slot0 = event.getInventory().getItem(0);
        var slot1 = event.getResult();
        if (slot1 == null || slot0 == null) return;
        if (slot0.getType() == slot1.getType()) {
            var slot0Dam = DurabilityManager.getItemDamage(slot0);
            var slot1Dam = DurabilityManager.getItemDamage(slot1);

            if (slot1Dam < slot0Dam) {
                var difference = slot0Dam - slot1Dam;
                DurabilityManager.repairItem(slot1, difference);
            }
        }
    }

    @EventHandler
    public void normalPrepareAnvil(PrepareAnvilEvent event) {
        var slot1 = event.getResult();
        if (slot1 == null) return;
        var itemsQuality = QualitiesManager.getQuality(slot1);
        QualitiesManager.removeQualityFromItem(slot1);
        QualitiesManager.addQualityToItem(slot1, (itemsQuality != null) ? itemsQuality : QualitiesManager.getRandomQuality());
        event.setResult(slot1);
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        event.getInventory().getViewers().forEach(humanEntity -> {
            if (event.getInventory().getResult() == null) return;
            var item = new ItemStack(event.getInventory().getResult().getType());
            var itemMeta = item.getItemMeta();
            if (itemMeta == null) return;
            if (!getConfig().applyQualityOnCraft) {
                event.setResult(QualitiesManager.addQualityToItem(item, QualitiesManager.getQuality(event.getInventory().getItem(0))));
                return;
            }
            if (QualitiesManager.itemCanHaveQuality(item)) {
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemMeta.getPersistentDataContainer().set(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER, 1);
                item.setItemMeta(itemMeta);
                event.setResult(item);
            }
        });
    }

    @EventHandler
    public void onSmithItem(SmithItemEvent event) {
        event.getInventory().getViewers().forEach(humanEntity -> {
            var player = (Player) humanEntity;
            var item = event.getInventory().getResult();
            if (item == null) return;
            var itemMeta = item.getItemMeta();
            itemMeta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(itemMeta);
            event.setCurrentItem(item);
            if (!getConfig().applyQualityOnCraft &&
                    itemMeta.getPersistentDataContainer().has(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER))
                return;
            var action = event.getAction();
            if (action.equals(InventoryAction.HOTBAR_SWAP) ||
                    action.equals(InventoryAction.SWAP_WITH_CURSOR) ||
                    action.equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
                if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item))
                    QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
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
                                    QualitiesManager.addQualityToItem(postInv[i], QualitiesManager.getRandomQuality());
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
            if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item)) {
                QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
                event.setCurrentItem(item);
            }
        });
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getResult() == null || event.getInventory().getResult().getType().equals(Material.AIR))
            return;
        var item = event.getInventory().getResult().clone();
        if (QualitiesManager.itemCanHaveQuality(item)) {
            var meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER, 1);
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
            if (!getConfig().applyQualityOnCraft &&
                    itemMeta.getPersistentDataContainer().has(Keys.ITEM_CRAFTED_KEY, PersistentDataType.INTEGER))
                return;
            var action = event.getAction();
            if (action.equals(InventoryAction.HOTBAR_SWAP) ||
                    action.equals(InventoryAction.SWAP_WITH_CURSOR) ||
                    action.equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
                if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item))
                    QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
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
                                    QualitiesManager.addQualityToItem(postInv[i], QualitiesManager.getRandomQuality());
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
            if (QualitiesManager.itemCanHaveQuality(item) && !QualitiesManager.itemHasQuality(item)) {
                QualitiesManager.addQualityToItem(item, QualitiesManager.getRandomQuality());
                event.setCurrentItem(item);
            }
        });
    }

    @EventHandler
    public void reforgeItem(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var item = player.getInventory().getItemInMainHand();
        var block = event.getClickedBlock();
        var action = event.getAction();
        var hand = event.getHand();
        if (action != Action.RIGHT_CLICK_BLOCK ||
                hand != EquipmentSlot.HAND ||
                block == null ||
                !player.isSneaking()) return;
        if (!block.getType().equals(Material.ANVIL) &&
                !block.getType().equals(Material.CHIPPED_ANVIL) &&
                !block.getType().equals(Material.DAMAGED_ANVIL)) return;
        if (!getConfig().reforgeStationEnabled) return;

        List<ItemQuality> qualities = new ArrayList<>((block.getType().equals(Material.ANVIL)) ?
                Registries.qualitiesRegistry.getRegistry().values().stream().toList() :
                (block.getType().equals(Material.CHIPPED_ANVIL)) ?
                        Registries.qualitiesRegistry.getRegistry().values().stream().filter(q -> q.tier <= 5).toList() :
                        (block.getType().equals(Material.DAMAGED_ANVIL)) ?
                                Registries.qualitiesRegistry.getRegistry().values().stream().filter(q -> q.tier <= 4).toList() :
                                Registries.qualitiesRegistry.getRegistry().values().stream().toList());

        if (!Utils.canUseReforge(player)) {
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.no_permission").formatted(Permissions.USE_REFORGE_PERMISSION));
            return;
        }

        var cost = Utils.getReforgeEXPCost(item.getType());
        if (cost <= 0) {
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.item_cant_be_reforged"));
            return;
        }

        if (player.getGameMode().equals(GameMode.SURVIVAL) && player.getLevel() < cost) {
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.cant_afford").formatted(cost));
            return;
        }
        qualities.remove(QualitiesManager.getQuality(item));
        var newQuality = qualities.get(Utils.getRandom().nextInt(qualities.size()));
        QualitiesManager.removeQualityFromItem(item);
        QualitiesManager.addQualityToItem(item, newQuality);
        if (player.getGameMode().equals(GameMode.SURVIVAL)) player.setLevel(player.getLevel() - cost);
        if (player.getGameMode().equals(GameMode.SURVIVAL))
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.success_survival").formatted(Utils.formalizedString(item.getType().toString()),
                    newQuality.display, cost));
        else
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.success_creative").formatted(Utils.formalizedString(item.getType().toString()),
                    newQuality.display));

        if (Utils.chanceOf(6) && player.getGameMode().equals(GameMode.SURVIVAL) && getConfig().damageAnvilOnReforge) {
            Directional dirBlockData = (Directional) block.getBlockData();
            var initFacing = dirBlockData.getFacing();

            player.playSound(block.getLocation(), Sound.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1f, 1f);
            if (block.getType().equals(Material.ANVIL)) block.setType(Material.CHIPPED_ANVIL);
            else if (block.getType().equals(Material.CHIPPED_ANVIL)) block.setType(Material.DAMAGED_ANVIL);
            else if (block.getType().equals(Material.DAMAGED_ANVIL)) block.setType(Material.AIR);

            dirBlockData = (Directional) block.getBlockData(); // Update the data.
            dirBlockData.setFacing(initFacing);
            block.setBlockData(dirBlockData);
        } else player.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1f, 1f);

        event.setCancelled(true);
    }
}
