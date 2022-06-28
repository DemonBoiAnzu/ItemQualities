package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.api.Keys;
import com.abraxas.itemqualities.inventories.Inventories;
import com.abraxas.itemqualities.inventories.utils.InvUtils;
import com.abraxas.itemqualities.utils.QualityChatValues;
import com.abraxas.itemqualities.utils.Utils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

import static com.abraxas.itemqualities.utils.Utils.colorize;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;

public class QualityModifierSlotSpecAmList implements InventoryProvider {
    ItemQualities main = ItemQualities.getInstance();

    @Override
    public void init(Player player, InventoryContents contents) {
        var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
        var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
        var quality = QualitiesManager.getQualityById(qualityNamespace);
        if (quality == null) {
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.error"));
            player.closeInventory();
            return;
        }
        var editing = Attribute.valueOf(player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_MODIFIER_EDITING, PersistentDataType.STRING, ""));
        var mod = quality.modifiers.get(editing);

        contents.fill(ClickableItem.of(InvUtils.blankItemSecondary, InvUtils.PREVENT_PICKUP));

        int col = 0;
        int row = 0;
        for (int i = 0; i < ((mod.slotSpecificAmounts != null) ? mod.slotSpecificAmounts.size() : 0); i++) {
            var slot = mod.slotSpecificAmounts.entrySet().stream().toList().get(i);

            var slotItem = new ItemStack(Material.PAPER);
            var slotItemMeta = slotItem.getItemMeta();
            slotItemMeta.setDisplayName(Utils.colorize("&7Slot Specific Amount"));
            slotItemMeta.setLore(new ArrayList<>() {{
                add(Utils.colorize("&7Slot: &e%s".formatted(slot.getKey().toString())));
                add(Utils.colorize("&7Amount: &e%s".formatted(slot.getValue())));
                add("");
                add(colorize("&7Left-Click to Edit Amount"));
                add(colorize("&7Right-Click to Remove"));
            }});
            slotItem.setItemMeta(slotItemMeta);

            contents.set(row, col, ClickableItem.of(slotItem, e -> {
                e.setCancelled(true);
                if (e.isLeftClick()) {
                    player.getPersistentDataContainer().set(Keys.PLAYER_QUALITY_MODIFIER_EDITING_SLOT, PersistentDataType.STRING, slot.getKey().toString());
                    player.getPersistentDataContainer().set(Keys.PLAYER_TYPING_VALUE_KEY, PersistentDataType.STRING, QualityChatValues.UPDATE_QUALITY_MODIFIER_SLOT_AMOUNT);
                    sendMessageWithPrefix(player, ItemQualities.getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Slot amount for %s".formatted(slot.getKey())));
                    player.closeInventory();
                } else if (e.isRightClick()) {
                    mod.slotSpecificAmounts.remove(slot.getKey());
                    Inventories.QUALITY_MODIFIER_SLOT_SPEC_AM_LIST.open(player);
                }
            }));

            col++;
            if (col >= 9) {
                col = 0;
                row++;
            }
        }

        var newQualityItem = new ItemStack(Material.GREEN_BANNER);
        var newQualityItemMeta = (BannerMeta) newQualityItem.getItemMeta();
        newQualityItemMeta.setDisplayName(colorize("&aAdd New"));
        newQualityItemMeta.setLore(new ArrayList<>() {{
            add(colorize("&7Add a new Slot."));
        }});
        newQualityItemMeta.addPattern(new Pattern(DyeColor.LIME, PatternType.STRAIGHT_CROSS));
        newQualityItemMeta.addPattern(new Pattern(DyeColor.GREEN, PatternType.BORDER));
        newQualityItemMeta.addPattern(new Pattern(DyeColor.GREEN, PatternType.STRIPE_TOP));
        newQualityItemMeta.addPattern(new Pattern(DyeColor.GREEN, PatternType.STRIPE_BOTTOM));
        newQualityItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); // Wtf mojang
        newQualityItem.setItemMeta(newQualityItemMeta);
        contents.set(row, col, ClickableItem.of(newQualityItem, e -> {
            e.setCancelled(true);
            Inventories.QUALITY_MODIFIER_SELECT_SLOT.open(player);
        }));

        contents.fillRow(2, ClickableItem.of(InvUtils.blankItem, InvUtils.PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(InvUtils.arrowLeftBtn, e -> {
            e.setCancelled(true);
            Inventories.QUALITY_EDIT_MODIFIER.open(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
