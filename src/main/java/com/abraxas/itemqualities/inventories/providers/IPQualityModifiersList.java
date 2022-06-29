package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;

import static com.abraxas.itemqualities.ItemQualities.getInstance;
import static com.abraxas.itemqualities.QualitiesManager.getQualityById;
import static com.abraxas.itemqualities.QualitiesManager.saveQualityToFile;
import static com.abraxas.itemqualities.api.Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING;
import static com.abraxas.itemqualities.api.Keys.PLAYER_QUALITY_MODIFIER_EDITING;
import static com.abraxas.itemqualities.api.Registries.qualitiesRegistry;
import static com.abraxas.itemqualities.inventories.Inventories.*;
import static com.abraxas.itemqualities.inventories.utils.InvUtils.*;
import static com.abraxas.itemqualities.utils.Utils.colorize;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;
import static org.bukkit.DyeColor.GREEN;
import static org.bukkit.DyeColor.LIME;
import static org.bukkit.Material.GREEN_BANNER;
import static org.bukkit.Material.PAPER;
import static org.bukkit.block.banner.PatternType.*;
import static org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS;
import static org.bukkit.persistence.PersistentDataType.STRING;

public class IPQualityModifiersList implements InventoryProvider {
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

        int col = 0;
        int row = 0;
        for (int i = 0; i < quality.modifiers.size(); i++) {
            var attr = quality.modifiers.keySet().stream().toList().get(i);
            var mod = quality.modifiers.get(attr);

            var attrN = attr.name().toLowerCase().replace("generic_", "generic.");
            var attrTrans = new TranslatableComponent("attribute.name.%s".formatted(attrN));
            var attrFinal = attrTrans.toPlainText();

            var modifierItem = new ItemStack(PAPER);
            var modifierItemMeta = modifierItem.getItemMeta();
            modifierItemMeta.setDisplayName(colorize("&r%s Modifier".formatted(attrFinal)));
            modifierItemMeta.setLore(new ArrayList<>() {{
                if (mod.slotSpecificAmounts == null)
                    add(colorize("&7Amount: &e%s".formatted(mod.amount)));
                else {
                    mod.slotSpecificAmounts.forEach((slot, amount) -> {
                        add(colorize("&7%s%s".formatted(slot.toString(), ": &e%s".formatted(amount))));
                    });
                }

                if (mod.slotSpecificAmounts == null && mod.slot != null && mod.ignoredSlots == null)
                    add(colorize("&7Slot: &e%s".formatted(mod.slot.toString())));
                else if (mod.slotSpecificAmounts != null && mod.slot == null && mod.ignoredSlots != null) {
                    var ignoredSlotStrings = new ArrayList<String>();
                    mod.ignoredSlots.forEach(is -> ignoredSlotStrings.add(is.toString()));
                    add(colorize("&7Ignored Slots: &e%s".formatted(String.join(", ", ignoredSlotStrings))));
                }
                add("");
                add(colorize("&7Left-Click to Edit"));
                add(colorize("&7Right-Click to Remove"));
            }});
            modifierItem.setItemMeta(modifierItemMeta);

            contents.set(row, col, ClickableItem.of(modifierItem, e -> {
                e.setCancelled(true);
                if (e.isLeftClick()) {
                    player.getPersistentDataContainer().set(PLAYER_QUALITY_MODIFIER_EDITING, STRING, attr.toString());
                    QUALITY_EDIT_MODIFIER.open(player);
                } else if (e.isRightClick()) {
                    quality.modifiers.remove(attr);
                    qualitiesRegistry.updateValue(quality.key, quality);
                    saveQualityToFile(quality);
                    player.getPersistentDataContainer().remove(PLAYER_QUALITY_MODIFIER_EDITING);
                    QUALITY_MODIFIERS_LIST.open(player);
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
            add(colorize("&7Add a new Modifier."));
        }});
        newQualityItemMeta.addPattern(new Pattern(LIME, STRAIGHT_CROSS));
        newQualityItemMeta.addPattern(new Pattern(GREEN, BORDER));
        newQualityItemMeta.addPattern(new Pattern(GREEN, STRIPE_TOP));
        newQualityItemMeta.addPattern(new Pattern(GREEN, STRIPE_BOTTOM));
        newQualityItemMeta.addItemFlags(HIDE_POTION_EFFECTS); // Wtf mojang
        newQualityItem.setItemMeta(newQualityItemMeta);
        contents.set(row, col, ClickableItem.of(newQualityItem, e -> {
            e.setCancelled(true);
            QUALITY_EDIT_MODIFIER_SELECT_ATTRIBUTE.open(player);
        }));

        contents.fillRow(2, ClickableItem.of(blankItem, PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(arrowLeftBtn, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().remove(PLAYER_QUALITY_MODIFIER_EDITING);
            QUALITY_EDIT_INVENTORY.open(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
