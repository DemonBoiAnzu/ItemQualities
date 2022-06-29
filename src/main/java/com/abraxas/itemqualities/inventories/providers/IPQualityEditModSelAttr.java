package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.api.QualityAttributeModifier;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.abraxas.itemqualities.ItemQualities.getInstance;
import static com.abraxas.itemqualities.QualitiesManager.getQualityById;
import static com.abraxas.itemqualities.api.Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING;
import static com.abraxas.itemqualities.api.Keys.PLAYER_QUALITY_MODIFIER_EDITING;
import static com.abraxas.itemqualities.api.Registries.qualitiesRegistry;
import static com.abraxas.itemqualities.inventories.Inventories.QUALITY_EDIT_MODIFIER;
import static com.abraxas.itemqualities.inventories.Inventories.QUALITY_MODIFIERS_LIST;
import static com.abraxas.itemqualities.inventories.utils.InvUtils.*;
import static com.abraxas.itemqualities.utils.Utils.colorize;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;
import static org.bukkit.Material.PAPER;
import static org.bukkit.attribute.Attribute.*;
import static org.bukkit.persistence.PersistentDataType.STRING;

public class IPQualityEditModSelAttr implements InventoryProvider {
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

        List<Attribute> attributeList = new ArrayList<>() {{
            addAll(List.of(Attribute.values()));
        }};
        attributeList.removeAll(List.of(ZOMBIE_SPAWN_REINFORCEMENTS, HORSE_JUMP_STRENGTH, GENERIC_FLYING_SPEED, GENERIC_FOLLOW_RANGE));
        attributeList.removeAll(quality.modifiers.keySet());

        int col = 0;
        int row = 0;
        for (Attribute attribute : attributeList) {
            var attrN = attribute.name().toLowerCase().replace("generic_", "generic.");
            var attrTrans = new TranslatableComponent("attribute.name.%s".formatted(attrN));
            var attrFinal = attrTrans.toPlainText();
            var attributeItem = new ItemStack(PAPER);
            var attributeItemMeta = attributeItem.getItemMeta();
            attributeItemMeta.setDisplayName(colorize("&r%s".formatted(attrFinal)));
            attributeItemMeta.setLore(new ArrayList<>() {{
                add(colorize("&7Click to add Modifier."));
            }});
            attributeItem.setItemMeta(attributeItemMeta);
            contents.set(row, col, ClickableItem.of(attributeItem, e -> {
                e.setCancelled(true);
                quality.modifiers.put(attribute, new QualityAttributeModifier(0d));
                qualitiesRegistry.updateValue(quality.key, quality);
                QualitiesManager.saveQualityToFile(quality);
                player.getPersistentDataContainer().set(PLAYER_QUALITY_MODIFIER_EDITING, STRING, attribute.toString());
                QUALITY_EDIT_MODIFIER.open(player);
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
            player.getPersistentDataContainer().remove(PLAYER_QUALITY_MODIFIER_EDITING);
            QUALITY_MODIFIERS_LIST.open(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
