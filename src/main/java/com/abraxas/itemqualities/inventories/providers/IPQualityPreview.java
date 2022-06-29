package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.abraxas.itemqualities.ItemQualities.getInstance;
import static com.abraxas.itemqualities.QualitiesManager.addQualityToItem;
import static com.abraxas.itemqualities.QualitiesManager.getQualityById;
import static com.abraxas.itemqualities.api.Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING;
import static com.abraxas.itemqualities.inventories.Inventories.QUALITY_MANAGER_INVENTORY;
import static com.abraxas.itemqualities.inventories.utils.InvUtils.*;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;
import static org.bukkit.Material.*;
import static org.bukkit.persistence.PersistentDataType.STRING;

public class IPQualityPreview implements InventoryProvider {
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
        var previewSword = addQualityToItem(new ItemStack(NETHERITE_SWORD), quality);
        var previewPick = addQualityToItem(new ItemStack(NETHERITE_PICKAXE), quality);
        var previewAxe = addQualityToItem(new ItemStack(NETHERITE_AXE), quality);
        var previewShovel = addQualityToItem(new ItemStack(NETHERITE_SHOVEL), quality);
        var previewHoe = addQualityToItem(new ItemStack(NETHERITE_HOE), quality);
        var previewHelmet = addQualityToItem(new ItemStack(NETHERITE_HELMET), quality);
        var previewChestplate = addQualityToItem(new ItemStack(NETHERITE_CHESTPLATE), quality);
        var previewLeggings = addQualityToItem(new ItemStack(NETHERITE_LEGGINGS), quality);
        var previewBoots = addQualityToItem(new ItemStack(NETHERITE_BOOTS), quality);
        var previewShield = addQualityToItem(new ItemStack(SHIELD), quality);
        var previewBow = addQualityToItem(new ItemStack(BOW), quality);
        var previewTrident = addQualityToItem(new ItemStack(TRIDENT), quality);

        contents.fill(ClickableItem.of(blankItemSecondary, PREVENT_PICKUP));

        contents.set(0, 0, ClickableItem.of(previewHelmet, PREVENT_PICKUP));
        contents.set(0, 1, ClickableItem.of(previewChestplate, PREVENT_PICKUP));
        contents.set(0, 2, ClickableItem.of(previewLeggings, PREVENT_PICKUP));
        contents.set(0, 3, ClickableItem.of(previewBoots, PREVENT_PICKUP));

        contents.set(0, 4, ClickableItem.of(previewSword, PREVENT_PICKUP));
        contents.set(0, 5, ClickableItem.of(previewPick, PREVENT_PICKUP));
        contents.set(0, 6, ClickableItem.of(previewAxe, PREVENT_PICKUP));
        contents.set(0, 7, ClickableItem.of(previewShovel, PREVENT_PICKUP));
        contents.set(0, 8, ClickableItem.of(previewHoe, PREVENT_PICKUP));

        contents.set(1, 3, ClickableItem.of(previewShield, PREVENT_PICKUP));
        contents.set(1, 4, ClickableItem.of(previewBow, PREVENT_PICKUP));
        contents.set(1, 5, ClickableItem.of(previewTrident, PREVENT_PICKUP));

        contents.fillRow(2, ClickableItem.of(blankItem, PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(arrowLeftBtn, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().remove(PLAYER_QUALITY_EDITING_OR_PREVIEWING);
            QUALITY_MANAGER_INVENTORY.open(player, 0);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
