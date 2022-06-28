package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.api.Keys;
import com.abraxas.itemqualities.inventories.Inventories;
import com.abraxas.itemqualities.inventories.utils.InvUtils;
import com.abraxas.itemqualities.utils.Utils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class QualityPreview implements InventoryProvider {
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
        var previewSword = QualitiesManager.addQualityToItem(new ItemStack(Material.NETHERITE_SWORD), quality);
        var previewPick = QualitiesManager.addQualityToItem(new ItemStack(Material.NETHERITE_PICKAXE), quality);
        var previewAxe = QualitiesManager.addQualityToItem(new ItemStack(Material.NETHERITE_AXE), quality);
        var previewShovel = QualitiesManager.addQualityToItem(new ItemStack(Material.NETHERITE_SHOVEL), quality);
        var previewHoe = QualitiesManager.addQualityToItem(new ItemStack(Material.NETHERITE_HOE), quality);
        var previewHelmet = QualitiesManager.addQualityToItem(new ItemStack(Material.NETHERITE_HELMET), quality);
        var previewChestplate = QualitiesManager.addQualityToItem(new ItemStack(Material.NETHERITE_CHESTPLATE), quality);
        var previewLeggings = QualitiesManager.addQualityToItem(new ItemStack(Material.NETHERITE_LEGGINGS), quality);
        var previewBoots = QualitiesManager.addQualityToItem(new ItemStack(Material.NETHERITE_BOOTS), quality);
        var previewShield = QualitiesManager.addQualityToItem(new ItemStack(Material.SHIELD), quality);
        var previewBow = QualitiesManager.addQualityToItem(new ItemStack(Material.BOW), quality);
        var previewTrident = QualitiesManager.addQualityToItem(new ItemStack(Material.TRIDENT), quality);

        contents.fill(ClickableItem.of(InvUtils.blankItemSecondary, InvUtils.PREVENT_PICKUP));

        contents.set(0, 0, ClickableItem.of(previewHelmet, InvUtils.PREVENT_PICKUP));
        contents.set(0, 1, ClickableItem.of(previewChestplate, InvUtils.PREVENT_PICKUP));
        contents.set(0, 2, ClickableItem.of(previewLeggings, InvUtils.PREVENT_PICKUP));
        contents.set(0, 3, ClickableItem.of(previewBoots, InvUtils.PREVENT_PICKUP));

        contents.set(0, 4, ClickableItem.of(previewSword, InvUtils.PREVENT_PICKUP));
        contents.set(0, 5, ClickableItem.of(previewPick, InvUtils.PREVENT_PICKUP));
        contents.set(0, 6, ClickableItem.of(previewAxe, InvUtils.PREVENT_PICKUP));
        contents.set(0, 7, ClickableItem.of(previewShovel, InvUtils.PREVENT_PICKUP));
        contents.set(0, 8, ClickableItem.of(previewHoe, InvUtils.PREVENT_PICKUP));

        contents.set(1, 3, ClickableItem.of(previewShield, InvUtils.PREVENT_PICKUP));
        contents.set(1, 4, ClickableItem.of(previewBow, InvUtils.PREVENT_PICKUP));
        contents.set(1, 5, ClickableItem.of(previewTrident, InvUtils.PREVENT_PICKUP));

        contents.fillRow(2, ClickableItem.of(InvUtils.blankItem, InvUtils.PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(InvUtils.arrowLeftBtn, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING);
            Inventories.QUALITY_MANAGER_INVENTORY.open(player, 0);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
