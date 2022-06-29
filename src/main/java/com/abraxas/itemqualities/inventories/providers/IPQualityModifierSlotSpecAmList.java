package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.utils.Utils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;

import static com.abraxas.itemqualities.ItemQualities.getInstance;
import static com.abraxas.itemqualities.api.Keys.*;
import static com.abraxas.itemqualities.inventories.Inventories.*;
import static com.abraxas.itemqualities.inventories.utils.InvUtils.*;
import static com.abraxas.itemqualities.utils.QualityChatValues.UPDATE_QUALITY_MODIFIER_SLOT_AMOUNT;
import static com.abraxas.itemqualities.utils.Utils.colorize;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;
import static org.bukkit.DyeColor.GREEN;
import static org.bukkit.DyeColor.LIME;
import static org.bukkit.Material.GREEN_BANNER;
import static org.bukkit.Material.PAPER;
import static org.bukkit.block.banner.PatternType.*;
import static org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS;
import static org.bukkit.persistence.PersistentDataType.STRING;

public class IPQualityModifierSlotSpecAmList implements InventoryProvider {
    ItemQualities main = getInstance();

    @Override
    public void init(Player player, InventoryContents contents) {
        var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
        var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
        var quality = QualitiesManager.getQualityById(qualityNamespace);
        if (quality == null) {
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.error"));
            player.closeInventory();
            return;
        }
        var editing = Attribute.valueOf(player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_MODIFIER_EDITING, STRING, ""));
        var mod = quality.modifiers.get(editing);

        contents.fill(ClickableItem.of(blankItemSecondary, PREVENT_PICKUP));

        int col = 0;
        int row = 0;
        for (int i = 0; i < ((mod.slotSpecificAmounts != null) ? mod.slotSpecificAmounts.size() : 0); i++) {
            var slot = mod.slotSpecificAmounts.entrySet().stream().toList().get(i);

            var slotItem = new ItemStack(PAPER);
            var slotItemMeta = slotItem.getItemMeta();
            slotItemMeta.setDisplayName(colorize("&7Slot Specific Amount"));
            slotItemMeta.setLore(new ArrayList<>() {{
                add(colorize("&7Slot: &e%s".formatted(slot.getKey().toString())));
                add(colorize("&7Amount: &e%s".formatted(slot.getValue())));
                add("");
                add(colorize("&7Left-Click to Edit Amount"));
                add(colorize("&7Right-Click to Remove"));
            }});
            slotItem.setItemMeta(slotItemMeta);

            contents.set(row, col, ClickableItem.of(slotItem, e -> {
                e.setCancelled(true);
                if (e.isLeftClick()) {
                    player.getPersistentDataContainer().set(PLAYER_QUALITY_MODIFIER_EDITING_SLOT, STRING, slot.getKey().toString());
                    player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, UPDATE_QUALITY_MODIFIER_SLOT_AMOUNT);
                    sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Slot amount for %s".formatted(slot.getKey())));
                    player.closeInventory();
                } else if (e.isRightClick()) {
                    mod.slotSpecificAmounts.remove(slot.getKey());
                    QUALITY_MODIFIER_SLOT_SPEC_AM_LIST.open(player);
                }
            }));

            col++;
            if (col >= 9) {
                col = 0;
                row++;
            }
        }

        var newQualityItem = new ItemStack(GREEN_BANNER);
        var newQualityItemMeta = (BannerMeta) newQualityItem.getItemMeta();
        newQualityItemMeta.setDisplayName(colorize("&aAdd New"));
        newQualityItemMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Add a new Slot."));
        }});
        newQualityItemMeta.addPattern(new Pattern(LIME, STRAIGHT_CROSS));
        newQualityItemMeta.addPattern(new Pattern(GREEN, BORDER));
        newQualityItemMeta.addPattern(new Pattern(GREEN, STRIPE_TOP));
        newQualityItemMeta.addPattern(new Pattern(GREEN, STRIPE_BOTTOM));
        newQualityItemMeta.addItemFlags(HIDE_POTION_EFFECTS); // Wtf mojang
        newQualityItem.setItemMeta(newQualityItemMeta);
        contents.set(row, col, ClickableItem.of(newQualityItem, e -> {
            e.setCancelled(true);
            QUALITY_MODIFIER_SELECT_SLOT.open(player);
        }));

        contents.fillRow(2, ClickableItem.of(blankItem, PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(arrowLeftBtn, e -> {
            e.setCancelled(true);
            QUALITY_EDIT_MODIFIER.open(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
