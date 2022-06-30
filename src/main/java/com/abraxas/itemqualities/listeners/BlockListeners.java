package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.api.ItemQualityComparator;
import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.api.quality.ItemQuality;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.abraxas.itemqualities.QualitiesManager.*;
import static com.abraxas.itemqualities.api.DurabilityManager.*;
import static com.abraxas.itemqualities.api.Keys.*;
import static com.abraxas.itemqualities.utils.Permissions.USE_REFORGE_PERMISSION;
import static com.abraxas.itemqualities.utils.Utils.*;
import static org.bukkit.ChatColor.stripColor;
import static org.bukkit.GameMode.SURVIVAL;
import static org.bukkit.Material.*;
import static org.bukkit.Sound.BLOCK_ANVIL_DESTROY;
import static org.bukkit.Sound.BLOCK_ANVIL_USE;
import static org.bukkit.SoundCategory.BLOCKS;
import static org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES;
import static org.bukkit.persistence.PersistentDataType.INTEGER;
import static org.bukkit.persistence.PersistentDataType.STRING;

public class BlockListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void repairItemAnvil(PrepareAnvilEvent event) {
        var slot0 = event.getInventory().getItem(0);
        var slot1 = event.getResult();
        if (slot1 == null || slot0 == null) return;
        if (slot0.getType() == slot1.getType()) {
            var slot0Dam = getItemDamage(slot0);
            var slot1Dam = getItemDamage(slot1);

            if (slot1Dam < slot0Dam) {
                var difference = slot0Dam - slot1Dam;
                repairItem(slot1, difference);
            }
        }
    }

    @EventHandler
    public void normalPrepareAnvil(PrepareAnvilEvent event) {
        var slot1 = event.getResult();
        if (slot1 == null) return;
        if (!itemHasQuality(slot1)) return;
        var itemsQuality = getQuality(slot1);

        var renameText = event.getInventory().getRenameText();
        if (itemHasQuality(slot1))
            renameText = renameText.replaceAll(stripColor(colorize(itemsQuality.display)) + " ", "");
        var slot1Meta = slot1.getItemMeta();
        slot1Meta.getPersistentDataContainer().set(ITEM_CUSTOM_NAME, STRING, renameText);
        slot1.setItemMeta(slot1Meta);
        removeQualityFromItem(slot1);
        addQualityToItem(slot1, (itemsQuality != null) ? itemsQuality : getRandomQuality());
        event.setResult(slot1);
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        event.getInventory().getViewers().forEach(humanEntity -> {
            if (event.getInventory().getResult() == null) return;
            var ogItemEnchants = event.getResult().getEnchantments();
            var item = new ItemStack(event.getInventory().getResult().getType());
            ogItemEnchants.forEach(item::addEnchantment);
            var itemMeta = item.getItemMeta();
            if (itemMeta == null) return;
            if (!getConfig().rerollQualityOnSmith) {
                event.setResult(addQualityToItem(item, getQuality(event.getInventory().getItem(0))));
                return;
            }
            if (itemCanHaveQuality(item)) {
                itemMeta.addItemFlags(HIDE_ATTRIBUTES);
                itemMeta.getPersistentDataContainer().set(ITEM_CRAFTED, INTEGER, 1);
                item.setItemMeta(itemMeta);
                event.setResult(item);
            }
        });
    }

    @EventHandler
    public void onSmithItem(SmithItemEvent event) {
        var item = event.getCurrentItem();
        if (item == null) return;
        var meta = item.getItemMeta();
        if (meta == null) return;

        meta.removeItemFlags(HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        if (getConfig().rerollQualityOnSmith)
            event.setCurrentItem(addQualityToItem(item, getRandomQuality()));
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getResult() == null || event.getInventory().getResult().getType().equals(AIR))
            return;
        var item = event.getInventory().getResult().clone();
        if (itemCanHaveQuality(item)) {
            var meta = item.getItemMeta();
            meta.addItemFlags(HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ITEM_CRAFTED, INTEGER, 1);
            item.setItemMeta(meta);
        }
        event.getInventory().setResult(item);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        var player = (Player) event.getWhoClicked();
        var item = event.getCurrentItem();
        var action = event.getAction();
        if (item == null) return;
        var meta = item.getItemMeta();
        if (meta == null) return;

        meta.removeItemFlags(HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        if (!getConfig().applyQualityOnCraft &&
                meta.getPersistentDataContainer().has(ITEM_CRAFTED, INTEGER))
            return;

        switch (action) {
            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
            case SWAP_WITH_CURSOR:
                event.setCurrentItem(addQualityToItem(item, getRandomQuality()));
                break;
            default:
                if (event.isShiftClick()) {
                    var preInv = player.getInventory().getContents();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            var postInv = player.getInventory().getContents();

                            for (int i = 0; i < preInv.length; i++) {
                                if (preInv[i] != postInv[i]) {
                                    addQualityToItem(postInv[i], getRandomQuality());
                                    var itemMeta = postInv[i].getItemMeta();
                                    itemMeta.removeItemFlags(HIDE_ATTRIBUTES);
                                    postInv[i].setItemMeta(itemMeta);
                                    player.getInventory().setItem(i, postInv[i]);
                                }
                            }
                        }
                    }.runTaskLater(main, 5);
                } else event.setCurrentItem(addQualityToItem(item, getRandomQuality()));
                break;
        }
    }

    @EventHandler
    public void reforgeItem(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var item = player.getInventory().getItemInMainHand();
        var itemMeta = item.getItemMeta();
        var block = event.getClickedBlock();
        var action = event.getAction();
        var hand = event.getHand();
        if (action != Action.RIGHT_CLICK_BLOCK ||
                hand != EquipmentSlot.HAND ||
                block == null ||
                !player.isSneaking()) return;
        if (!block.getType().equals(ANVIL) &&
                !block.getType().equals(CHIPPED_ANVIL) &&
                !block.getType().equals(DAMAGED_ANVIL)) return;
        if (!getConfig().reforgeStationEnabled) return;

        if (itemMeta.getPersistentDataContainer().has(ITEM_QUALITY_REMOVED, INTEGER)) {
            itemMeta.getPersistentDataContainer().remove(ITEM_QUALITY_REMOVED);
            item.setItemMeta(itemMeta);
        }

        List<ItemQuality> registeredQualities = new ArrayList<>() {{
            addAll(Registries.qualitiesRegistry.getRegistry().values());
        }};
        if (registeredQualities.size() < 1) {
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.no_qualities_registered"));
            return;
        }
        registeredQualities.sort(new ItemQualityComparator());
        Collections.reverse(registeredQualities);

        int highestReforgeTier = highestTier;
        int lowestReforgeTier = lowestTier;
        if (getConfig().reforgeTierDependsOnAnvilDamage) {
            switch (block.getType()) {
                case ANVIL -> {
                    highestReforgeTier = highestTier;
                    lowestReforgeTier = midTier;
                }
                case CHIPPED_ANVIL -> {
                    highestReforgeTier = highTier;
                    lowestReforgeTier = lowTier;
                }
                case DAMAGED_ANVIL -> {
                    highestReforgeTier = midTier;
                    lowestReforgeTier = lowestTier;
                }
            }
        }
        int finalLowestReforgeTier = lowestReforgeTier;
        int finalHighestReforgeTier = highestReforgeTier;
        List<ItemQuality> qualities = new ArrayList<>(registeredQualities.stream().filter(q -> q.tier >= finalLowestReforgeTier &&
                q.tier < finalHighestReforgeTier).toList());

        qualities.sort(new ItemQualityComparator());
        Collections.shuffle(qualities);

        if (!canUseReforge(player)) {
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.no_permission").formatted(USE_REFORGE_PERMISSION));
            return;
        }

        var cost = getReforgeEXPCost(item.getType());
        if (cost <= 0) {
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.item_cant_be_reforged"));
            return;
        }

        if (player.getGameMode().equals(SURVIVAL) && player.getLevel() < cost) {
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.cant_afford").formatted(cost));
            return;
        }
        qualities.remove(getQuality(item));
        qualities.removeAll(registeredQualities.stream().filter(q -> q.addToItemChance <= 0).toList());
        ItemQuality newQuality = null;
        for (ItemQuality qual : qualities) {
            int chanceBonus = (block.getType().equals(ANVIL)) ? 20 :
                    (block.getType().equals(CHIPPED_ANVIL)) ? 10 :
                            (block.getType().equals(DAMAGED_ANVIL)) ? 8 : 0;
            if (qual.tier < midTier) chanceBonus = -5;
            if (chanceOf(qual.addToItemChance + chanceBonus)) newQuality = qual;
        }
        if (newQuality == null && qualities.size() > 0)
            newQuality = qualities.get(getRandom().nextInt(qualities.size()));
        if (newQuality == null) {
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.unable_to_reforge").formatted(finalLowestReforgeTier, finalHighestReforgeTier));
            return;
        }
        refreshItem(item, newQuality);
        var customItemName = item.getItemMeta().getPersistentDataContainer().getOrDefault(ITEM_CUSTOM_NAME, STRING, "");
        String itemName = (!customItemName.isEmpty()) ? customItemName : new TranslatableComponent("item.minecraft.%s".formatted(item.getType().toString().toLowerCase())).toPlainText();
        if (player.getGameMode().equals(SURVIVAL)) player.setLevel(player.getLevel() - cost);
        if (player.getGameMode().equals(SURVIVAL))
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.success_survival").formatted(formalizedString(itemName),
                    newQuality.display, cost));
        else
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.success_creative").formatted(formalizedString(itemName),
                    newQuality.display));

        if (chanceOf(6) && player.getGameMode().equals(SURVIVAL) && getConfig().damageAnvilOnReforge) {
            Directional dirBlockData = (Directional) block.getBlockData();
            var initFacing = dirBlockData.getFacing();

            player.playSound(block.getLocation(), BLOCK_ANVIL_DESTROY, BLOCKS, 1f, 1f);
            if (block.getType().equals(ANVIL)) block.setType(CHIPPED_ANVIL);
            else if (block.getType().equals(CHIPPED_ANVIL)) block.setType(DAMAGED_ANVIL);
            else if (block.getType().equals(DAMAGED_ANVIL)) block.setType(AIR);

            if (block.getType() != AIR) {
                dirBlockData = (Directional) block.getBlockData();
                dirBlockData.setFacing(initFacing);
                block.setBlockData(dirBlockData);
            }
        } else player.getWorld().playSound(block.getLocation(), BLOCK_ANVIL_USE, BLOCKS, 1f, 1f);
        damageItem(player, item, 0);
        event.setCancelled(true);
    }
}
