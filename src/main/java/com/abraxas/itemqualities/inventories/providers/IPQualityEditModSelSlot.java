package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.api.Keys;
import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.inventories.Inventories;
import com.abraxas.itemqualities.inventories.utils.InvUtils;
import com.abraxas.itemqualities.utils.Utils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IPQualityEditModSelSlot implements InventoryProvider {
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

        contents.fill(ClickableItem.of(InvUtils.blankItemSecondary, InvUtils.PREVENT_PICKUP));

        var editing = Attribute.valueOf(player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_MODIFIER_EDITING, PersistentDataType.STRING, ""));
        var mod = quality.modifiers.get(editing);

        List<EquipmentSlot> slotList = new ArrayList<>() {{
            addAll(List.of(EquipmentSlot.values()));
        }};
        if (player.getPersistentDataContainer().has(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT, PersistentDataType.INTEGER) && mod.slot != null)
            slotList.remove(mod.slot);
        else if (player.getPersistentDataContainer().has(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED, PersistentDataType.INTEGER) && mod.ignoredSlots != null)
            slotList.removeAll(mod.ignoredSlots);
        else if (player.getPersistentDataContainer().has(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC, PersistentDataType.INTEGER) && mod.slotSpecificAmounts != null)
            slotList.removeAll(mod.slotSpecificAmounts.keySet());

        int col = 0;
        int row = 0;
        for (EquipmentSlot slot : slotList) {
            var slotItem = new ItemStack(Material.PAPER);
            var slotItemMeta = slotItem.getItemMeta();
            slotItemMeta.setDisplayName(Utils.colorize("&r%s".formatted(Utils.formalizedString(slot.toString().replace("_", " ")))));
            slotItemMeta.setLore(new ArrayList<>() {{
                add(Utils.colorize("&7Click to add Slot."));
            }});
            slotItem.setItemMeta(slotItemMeta);
            contents.set(row, col, ClickableItem.of(slotItem, e -> {
                e.setCancelled(true);
                if (player.getPersistentDataContainer().has(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT, PersistentDataType.INTEGER)) {
                    mod.slot = slot;
                    Inventories.QUALITY_EDIT_MODIFIER.open(player);
                } else if (player.getPersistentDataContainer().has(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED, PersistentDataType.INTEGER)) {
                    if (mod.ignoredSlots == null) mod.ignoredSlots = new ArrayList<>();
                    mod.ignoredSlots.add(slot);
                    Inventories.QUALITY_EDIT_MODIFIER.open(player);
                } else if (player.getPersistentDataContainer().has(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC, PersistentDataType.INTEGER)) {
                    if (mod.slotSpecificAmounts == null) mod.slotSpecificAmounts = new HashMap<>();
                    mod.slotSpecificAmounts.put(slot, 0d);
                    Inventories.QUALITY_EDIT_MODIFIER.open(player);
                }
                Registries.qualitiesRegistry.updateValue(quality.key, quality);
                QualitiesManager.saveQualityToFile(quality);
                player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT);
                player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC);
                player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED);
            }));

            col++;
            if (col >= 9) {
                col = 0;
                row++;
            }
        }

        contents.fillRow(2, ClickableItem.of(InvUtils.blankItem, InvUtils.PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(InvUtils.arrowLeftBtn, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT);
            player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC);
            player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED);
            Inventories.QUALITY_EDIT_MODIFIER.open(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
