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
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;

public class IPQualityEditModifer implements InventoryProvider {
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

        if (mod.slotSpecificAmounts != null && mod.slotSpecificAmounts.size() < 1) mod.slotSpecificAmounts = null;
        if (mod.ignoredSlots != null && mod.ignoredSlots.size() < 1) mod.ignoredSlots = null;

        contents.fill(ClickableItem.of(InvUtils.blankItemSecondary, InvUtils.PREVENT_PICKUP));

        var editAmountItem = new ItemStack(Material.PAPER);
        var editAmountMeta = editAmountItem.getItemMeta();
        editAmountMeta.setDisplayName(Utils.colorize("&7Edit Amount"));
        editAmountMeta.setLore(new ArrayList<>() {{
            add(Utils.colorize("&7Current:"));
            if (mod.slotSpecificAmounts == null)
                add(Utils.colorize("&7Amount: &e%s".formatted(mod.amount)));
            else {
                mod.slotSpecificAmounts.forEach((slot, amount) -> {
                    add(Utils.colorize("&7%s%s".formatted(slot.toString(), ": &e%s".formatted(amount))));
                });
            }
            add("");
            add(Utils.colorize("&7Left-Click to Edit Normal Amount%s".formatted((mod.slotSpecificAmounts == null) ? "" : " &4(Disabled because SlotSpecificAmounts)")));
            add(Utils.colorize("&7Right-Click to Edit Slot Specific Amount"));
        }});
        editAmountItem.setItemMeta(editAmountMeta);
        contents.set(0, 3, ClickableItem.of(editAmountItem, e -> {
            e.setCancelled(true);
            if (e.isLeftClick()) {
                if (mod.slotSpecificAmounts != null) return;
                player.getPersistentDataContainer().set(Keys.PLAYER_TYPING_VALUE_KEY, PersistentDataType.STRING, QualityChatValues.UPDATE_QUALITY_MODIFIER_NORMAL_AMOUNT);
                sendMessageWithPrefix(player, ItemQualities.getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("Modifier Amount"));
                player.closeInventory();
            } else if (e.isRightClick()) {
                player.getPersistentDataContainer().set(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC, PersistentDataType.INTEGER, 0);
                Inventories.QUALITY_MODIFIER_SLOT_SPEC_AM_LIST.open(player);
            }
        }));

        var editSlotItem = new ItemStack(Material.PAPER);
        var editSlotMeta = editSlotItem.getItemMeta();
        editSlotMeta.setDisplayName(Utils.colorize("&7Edit Slot"));
        editSlotMeta.setLore(new ArrayList<>() {{
            add(Utils.colorize("&7Current:"));
            add(Utils.colorize("&7Slot: &e%s".formatted((mod.slotSpecificAmounts != null) ? "Using SlotSpecificAmounts!" : (mod.slot != null) ? mod.slot : "None")));
            if (mod.ignoredSlots != null) {
                var ignoredSlotStrings = new ArrayList<String>();
                mod.ignoredSlots.forEach(is -> ignoredSlotStrings.add(is.toString()));
                add(Utils.colorize("&7Ignored Slots: &e%s".formatted(String.join(", ", ignoredSlotStrings))));
            }
            add("");
            add(Utils.colorize("&7Left-Click to Edit Normal Slot%s".formatted((mod.slotSpecificAmounts == null) ? "" : " &4(Disabled because SlotSpecificAmounts)")));
            add(Utils.colorize("&7Right-Click to Edit Ignored Slots"));
        }});
        editSlotItem.setItemMeta(editSlotMeta);
        contents.set(0, 5, ClickableItem.of(editSlotItem, e -> {
            e.setCancelled(true);
            if (e.isLeftClick()) {
                if (mod.slotSpecificAmounts != null) return;
                player.getPersistentDataContainer().set(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT, PersistentDataType.INTEGER, 0);
                Inventories.QUALITY_MODIFIER_SELECT_SLOT.open(player);
            } else if (e.isRightClick()) {
                player.getPersistentDataContainer().set(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED, PersistentDataType.INTEGER, 0);
                Inventories.QUALITY_MODIFIER_SELECT_SLOT.open(player);
            }
        }));

        contents.fillRow(2, ClickableItem.of(InvUtils.blankItem, InvUtils.PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(InvUtils.arrowLeftBtn, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_EDITING);
            Inventories.QUALITY_MODIFIERS_LIST.open(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
