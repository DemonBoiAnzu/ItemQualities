package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.abraxas.itemqualities.ItemQualities.getInstance;
import static com.abraxas.itemqualities.QualitiesManager.getQualityById;
import static com.abraxas.itemqualities.QualitiesManager.saveQualityToFile;
import static com.abraxas.itemqualities.api.Keys.*;
import static com.abraxas.itemqualities.api.Registries.qualitiesRegistry;
import static com.abraxas.itemqualities.inventories.Inventories.QUALITY_EDIT_MODIFIER;
import static com.abraxas.itemqualities.inventories.utils.InvUtils.*;
import static com.abraxas.itemqualities.utils.Utils.*;
import static org.bukkit.Material.PAPER;
import static org.bukkit.persistence.PersistentDataType.INTEGER;
import static org.bukkit.persistence.PersistentDataType.STRING;

public class IPQualityEditModSelSlot implements InventoryProvider {
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

        var editing = Attribute.valueOf(player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_MODIFIER_EDITING, STRING, ""));
        var mod = quality.modifiers.get(editing);

        List<EquipmentSlot> slotList = new ArrayList<>() {{
            addAll(List.of(EquipmentSlot.values()));
        }};
        if (player.getPersistentDataContainer().has(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT, INTEGER) && mod.slot != null)
            slotList.remove(mod.slot);
        else if (player.getPersistentDataContainer().has(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED, INTEGER) && mod.ignoredSlots != null)
            slotList.removeAll(mod.ignoredSlots);
        else if (player.getPersistentDataContainer().has(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC, INTEGER) && mod.slotSpecificAmounts != null)
            slotList.removeAll(mod.slotSpecificAmounts.keySet());

        int col = 0;
        int row = 0;
        for (EquipmentSlot slot : slotList) {
            var slotItem = new ItemStack(PAPER);
            var slotItemMeta = slotItem.getItemMeta();
            slotItemMeta.setDisplayName(colorize("&r%s".formatted(formalizedString(slot.toString().replace("_", " ")))));
            slotItemMeta.setLore(new ArrayList<>() {{
                add(colorize("&7Click to add Slot."));
            }});
            slotItem.setItemMeta(slotItemMeta);
            contents.set(row, col, ClickableItem.of(slotItem, e -> {
                e.setCancelled(true);
                if (player.getPersistentDataContainer().has(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT, INTEGER)) {
                    mod.slot = slot;
                    QUALITY_EDIT_MODIFIER.open(player);
                } else if (player.getPersistentDataContainer().has(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED, INTEGER)) {
                    if (mod.ignoredSlots == null) mod.ignoredSlots = new ArrayList<>();
                    mod.ignoredSlots.add(slot);
                    QUALITY_EDIT_MODIFIER.open(player);
                } else if (player.getPersistentDataContainer().has(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC, INTEGER)) {
                    if (mod.slotSpecificAmounts == null) mod.slotSpecificAmounts = new HashMap<>();
                    mod.slotSpecificAmounts.put(slot, 0d);
                    QUALITY_EDIT_MODIFIER.open(player);
                }
                qualitiesRegistry.updateValue(quality.key, quality);
                saveQualityToFile(quality);
                player.getPersistentDataContainer().remove(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT);
                player.getPersistentDataContainer().remove(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC);
                player.getPersistentDataContainer().remove(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED);
            }));

            col++;
            if (col >= 9) {
                col = 0;
                row++;
            }
        }

        contents.fillRow(2, ClickableItem.of(blankItem, PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(arrowLeftBtn, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().remove(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT);
            player.getPersistentDataContainer().remove(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC);
            player.getPersistentDataContainer().remove(PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED);
            QUALITY_EDIT_MODIFIER.open(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
