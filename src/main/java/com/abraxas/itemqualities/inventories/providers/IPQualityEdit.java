package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;

import static com.abraxas.itemqualities.ItemQualities.getInstance;
import static com.abraxas.itemqualities.QualitiesManager.deleteQuality;
import static com.abraxas.itemqualities.QualitiesManager.getQualityById;
import static com.abraxas.itemqualities.api.Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING;
import static com.abraxas.itemqualities.api.Keys.PLAYER_TYPING_VALUE_KEY;
import static com.abraxas.itemqualities.inventories.Inventories.QUALITY_MANAGER_INVENTORY;
import static com.abraxas.itemqualities.inventories.Inventories.QUALITY_MODIFIERS_LIST;
import static com.abraxas.itemqualities.inventories.utils.InvUtils.*;
import static com.abraxas.itemqualities.utils.QualityChatValues.*;
import static com.abraxas.itemqualities.utils.Utils.colorize;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;
import static org.bukkit.DyeColor.LIGHT_GRAY;
import static org.bukkit.DyeColor.RED;
import static org.bukkit.Material.*;
import static org.bukkit.block.banner.PatternType.*;
import static org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES;
import static org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS;
import static org.bukkit.persistence.PersistentDataType.STRING;

public class IPQualityEdit implements InventoryProvider {
    ItemQualities main = getInstance();

    @Override
    public void init(Player player, InventoryContents contents) {
        var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
        var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
        var quality = getQualityById(qualityNamespace);
        if (quality == null) {
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.error"));
            player.closeInventory();
            return;
        }

        contents.fill(ClickableItem.of(blankItemSecondary, PREVENT_PICKUP));

        // General Quality Attributes
        var editId = new ItemStack(PAPER);
        var editIdMeta = editId.getItemMeta();
        editIdMeta.setDisplayName(colorize("&aChange ID"));
        editIdMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.key.getKey())));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editId.setItemMeta(editIdMeta);
        contents.set(0, 0, ClickableItem.of(editId, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_ID);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("ID"));
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.id_tip"));
            player.closeInventory();
        }));

        var editDisplay = new ItemStack(NAME_TAG);
        var editDisplayMeta = editDisplay.getItemMeta();
        editDisplayMeta.setDisplayName(colorize("&aChange Display"));
        editDisplayMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.display)));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editDisplay.setItemMeta(editDisplayMeta);
        contents.set(0, 1, ClickableItem.of(editDisplay, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_DISPLAY);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Display"));
            player.closeInventory();
        }));

        var editTier = new ItemStack(IRON_NUGGET);
        var editTierMeta = editTier.getItemMeta();
        editTierMeta.setDisplayName(colorize("&aChange Tier"));
        editTierMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.tier)));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editTier.setItemMeta(editTierMeta);
        contents.set(0, 2, ClickableItem.of(editTier, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_TIER);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Tier"));
            player.closeInventory();
        }));

        var editAddChance = new ItemStack(IRON_NUGGET);
        var editAddChanceMeta = editAddChance.getItemMeta();
        editAddChanceMeta.setDisplayName(colorize("&aChange Add Chance"));
        editAddChanceMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.addToItemChance)));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editAddChance.setItemMeta(editAddChanceMeta);
        contents.set(0, 3, ClickableItem.of(editAddChance, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_ADD_CHANCE);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Add Chance"));
            player.closeInventory();
        }));


        // Quality Attributes
        var editNoDropsChance = new ItemStack(GOLD_NUGGET);
        var editNoDropsChanceMeta = editNoDropsChance.getItemMeta();
        editNoDropsChanceMeta.setDisplayName(colorize("&aChange No Drops Chance"));
        editNoDropsChanceMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.noDropChance)));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editNoDropsChance.setItemMeta(editNoDropsChanceMeta);
        contents.set(0, 4, ClickableItem.of(editNoDropsChance, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_NO_DROPS_CHANCE);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("No Drops Chance"));
            player.closeInventory();
        }));

        var editDoubleDropsChance = new ItemStack(DIAMOND);
        var editDoubleDropsChanceMeta = editDoubleDropsChance.getItemMeta();
        editDoubleDropsChanceMeta.setDisplayName(colorize("&aChange Double Drops Chance"));
        editDoubleDropsChanceMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.doubleDropsChance)));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editDoubleDropsChance.setItemMeta(editDoubleDropsChanceMeta);
        contents.set(0, 5, ClickableItem.of(editDoubleDropsChance, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_DOUBLE_DROPS_CHANCE);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Double Drops Chance"));
            player.closeInventory();
        }));

        var editMaxDurabilityMod = new ItemStack(SCUTE);
        var editMaxDurabilityModMeta = editMaxDurabilityMod.getItemMeta();
        editMaxDurabilityModMeta.setDisplayName(colorize("&aChange Max Durability Mod"));
        editMaxDurabilityModMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.itemMaxDurabilityMod)));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editMaxDurabilityMod.setItemMeta(editMaxDurabilityModMeta);
        contents.set(0, 6, ClickableItem.of(editMaxDurabilityMod, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_MAX_DURABILITY_MOD);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Max Durability Mod"));
            player.closeInventory();
        }));

        var editNoDurabilityLossChance = new ItemStack(OBSIDIAN);
        var editNoDurabilityLossChanceMeta = editNoDurabilityLossChance.getItemMeta();
        editNoDurabilityLossChanceMeta.setDisplayName(colorize("&aChange No Durability Loss Chance"));
        editNoDurabilityLossChanceMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.noDurabilityLossChance)));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editNoDurabilityLossChance.setItemMeta(editNoDurabilityLossChanceMeta);
        contents.set(0, 7, ClickableItem.of(editNoDurabilityLossChance, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_NO_DURABILITY_LOSS_CHANCE);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("No Durability Loss Chance"));
            player.closeInventory();
        }));

        var editExtraDurabilityLoss = new ItemStack(STICK);
        var editExtraDurabilityLossMeta = editExtraDurabilityLoss.getItemMeta();
        editExtraDurabilityLossMeta.setDisplayName(colorize("&aChange Extra Durability Loss"));
        editExtraDurabilityLossMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.extraDurabilityLoss)));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editExtraDurabilityLoss.setItemMeta(editExtraDurabilityLossMeta);
        contents.set(1, 5, ClickableItem.of(editExtraDurabilityLoss, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_EXTRA_DURABILITY_LOSS);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Extra Durability Loss"));
            player.closeInventory();
        }));

        var editExtraDurabilityLossChance = new ItemStack(STICK);
        var editExtraDurabilityLossChanceMeta = editExtraDurabilityLossChance.getItemMeta();
        editExtraDurabilityLossChanceMeta.setDisplayName(colorize("&aChange Extra Durability Loss Chance"));
        editExtraDurabilityLossChanceMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Current Value: &e%s".formatted(quality.extraDurabilityLossChance)));
            add("");
            add(colorize("&7Left-Click to edit."));
        }});
        editExtraDurabilityLossChance.setItemMeta(editExtraDurabilityLossChanceMeta);
        contents.set(1, 3, ClickableItem.of(editExtraDurabilityLossChance, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_EXTRA_DURABILITY_LOSS_CHANCE);
            sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Extra Durability Loss Chance"));
            player.closeInventory();
        }));

        var editModifiers = new ItemStack(IRON_AXE);
        var editModifiersMeta = editModifiers.getItemMeta();
        editModifiersMeta.addItemFlags(HIDE_ATTRIBUTES);
        editModifiersMeta.setDisplayName(colorize("&aEdit Modifiers"));
        editModifiersMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Left-Click to edit."));
        }});
        editModifiers.setItemMeta(editModifiersMeta);
        contents.set(0, 8, ClickableItem.of(editModifiers, e -> {
            e.setCancelled(true);
            QUALITY_MODIFIERS_LIST.open(player);
        }));


        contents.fillRow(2, ClickableItem.of(blankItem, PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(arrowLeftBtn, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
            player.getPersistentDataContainer().remove(PLAYER_QUALITY_EDITING_OR_PREVIEWING);
            QUALITY_MANAGER_INVENTORY.open(player, 0);
        }));

        var deleteBtn = new ItemStack(LIGHT_GRAY_BANNER);
        var deleteBtnMeta = (BannerMeta) deleteBtn.getItemMeta();
        deleteBtnMeta.addPattern(new Pattern(RED, STRIPE_DOWNRIGHT));
        deleteBtnMeta.addPattern(new Pattern(RED, STRIPE_DOWNLEFT));
        deleteBtnMeta.addPattern(new Pattern(LIGHT_GRAY, CURLY_BORDER));
        deleteBtnMeta.setDisplayName(colorize("&c&lDelete Quality"));
        deleteBtnMeta.setLore(new ArrayList<>() {{
            add(colorize("&4Warning: &cYou can NOT undo this."));
            add(colorize("&conly click this if you are SURE."));
        }});
        deleteBtnMeta.addItemFlags(HIDE_POTION_EFFECTS); // Wtf mojang
        deleteBtn.setItemMeta(deleteBtnMeta);
        contents.set(2, 8, ClickableItem.of(deleteBtn, e -> {
            e.setCancelled(true);
            deleteQuality(quality);
            player.getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
            player.getPersistentDataContainer().remove(PLAYER_QUALITY_EDITING_OR_PREVIEWING);
            QUALITY_MANAGER_INVENTORY.open(player, 0);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
