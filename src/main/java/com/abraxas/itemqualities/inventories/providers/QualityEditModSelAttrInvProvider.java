package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.api.Keys;
import com.abraxas.itemqualities.api.QualityAttributeModifier;
import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.inventories.Inventories;
import com.abraxas.itemqualities.inventories.utils.InvUtils;
import com.abraxas.itemqualities.utils.Utils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class QualityEditModSelAttrInvProvider implements InventoryProvider {
    ItemQualities main = ItemQualities.getInstance();

    @Override
    public void init(Player player, InventoryContents contents) {
        var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY, PersistentDataType.STRING, "").split(":");
        var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
        var quality = QualitiesManager.getQualityById(qualityNamespace);
        if (quality == null) {
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.error"));
            player.closeInventory();
            return;
        }

        contents.fill(ClickableItem.of(InvUtils.blankItemSecondary, InvUtils.PREVENT_PICKUP));

        List<Attribute> attributeList = new ArrayList<>() {{
            addAll(List.of(Attribute.values()));
        }};
        attributeList.removeAll(List.of(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS, Attribute.HORSE_JUMP_STRENGTH, Attribute.GENERIC_FLYING_SPEED, Attribute.GENERIC_FOLLOW_RANGE));
        attributeList.removeAll(quality.modifiers.keySet());

        int col = 0;
        int row = 0;
        for (Attribute attribute : attributeList) {
            var attrN = attribute.name().toLowerCase().replace("generic_", "generic.");
            var attrTrans = new TranslatableComponent("attribute.name.%s".formatted(attrN));
            var attrFinal = attrTrans.toPlainText();
            var attributeItem = new ItemStack(Material.PAPER);
            var attributeItemMeta = attributeItem.getItemMeta();
            attributeItemMeta.setDisplayName(Utils.colorize("&r%s".formatted(attrFinal)));
            attributeItemMeta.setLore(new ArrayList<>() {{
                add(Utils.colorize("&7Click to add Modifier."));
            }});
            attributeItem.setItemMeta(attributeItemMeta);
            contents.set(row, col, ClickableItem.of(attributeItem, e -> {
                e.setCancelled(true);
                quality.modifiers.put(attribute, new QualityAttributeModifier(0d));
                Registries.qualitiesRegistry.updateValue(quality.key, quality);
                QualitiesManager.saveQualityToFile(quality);
                player.getPersistentDataContainer().set(Keys.PLAYER_QUALITY_MODIFIER_EDITING, PersistentDataType.STRING, attribute.toString());
                Inventories.QUALITY_EDIT_MODIFIER.open(player);
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
            player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_EDITING);
            Inventories.QUALITY_MODIFIERS_LIST.open(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
