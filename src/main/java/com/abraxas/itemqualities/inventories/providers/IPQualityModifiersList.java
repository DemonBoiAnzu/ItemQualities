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
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

import static com.abraxas.itemqualities.utils.Utils.colorize;

public class IPQualityModifiersList implements InventoryProvider {
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

        int col = 0;
        int row = 0;
        for (int i = 0; i < quality.modifiers.size(); i++) {
            var attr = quality.modifiers.keySet().stream().toList().get(i);
            var mod = quality.modifiers.get(attr);

            var attrN = attr.name().toLowerCase().replace("generic_", "generic.");
            var attrTrans = new TranslatableComponent("attribute.name.%s".formatted(attrN));
            var attrFinal = attrTrans.toPlainText();

            var modifierItem = new ItemStack(Material.PAPER);
            var modifierItemMeta = modifierItem.getItemMeta();
            modifierItemMeta.setDisplayName(Utils.colorize("&r%s Modifier".formatted(attrFinal)));
            modifierItemMeta.setLore(new ArrayList<>() {{
                if (mod.slotSpecificAmounts == null)
                    add(Utils.colorize("&7Amount: &e%s".formatted(mod.amount)));
                else {
                    mod.slotSpecificAmounts.forEach((slot, amount) -> {
                        add(Utils.colorize("&7%s%s".formatted(slot.toString(), ": &e%s".formatted(amount))));
                    });
                }

                if (mod.slotSpecificAmounts == null && mod.slot != null && mod.ignoredSlots == null)
                    add(Utils.colorize("&7Slot: &e%s".formatted(mod.slot.toString())));
                else if (mod.slotSpecificAmounts != null && mod.slot == null && mod.ignoredSlots != null) {
                    var ignoredSlotStrings = new ArrayList<String>();
                    mod.ignoredSlots.forEach(is -> ignoredSlotStrings.add(is.toString()));
                    add(Utils.colorize("&7Ignored Slots: &e%s".formatted(String.join(", ", ignoredSlotStrings))));
                }
                add("");
                add(Utils.colorize("&7Left-Click to Edit"));
                add(Utils.colorize("&7Right-Click to Remove"));
            }});
            modifierItem.setItemMeta(modifierItemMeta);

            contents.set(row, col, ClickableItem.of(modifierItem, e -> {
                e.setCancelled(true);
                if (e.isLeftClick()) {
                    player.getPersistentDataContainer().set(Keys.PLAYER_QUALITY_MODIFIER_EDITING, PersistentDataType.STRING, attr.toString());
                    Inventories.QUALITY_EDIT_MODIFIER.open(player);
                } else if (e.isRightClick()) {
                    quality.modifiers.remove(attr);
                    Registries.qualitiesRegistry.updateValue(quality.key, quality);
                    QualitiesManager.saveQualityToFile(quality);
                    player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_EDITING);
                    Inventories.QUALITY_MODIFIERS_LIST.open(player);
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
            add(colorize("&7Add a new Modifier."));
        }});
        newQualityItemMeta.addPattern(new Pattern(DyeColor.LIME, PatternType.STRAIGHT_CROSS));
        newQualityItemMeta.addPattern(new Pattern(DyeColor.GREEN, PatternType.BORDER));
        newQualityItemMeta.addPattern(new Pattern(DyeColor.GREEN, PatternType.STRIPE_TOP));
        newQualityItemMeta.addPattern(new Pattern(DyeColor.GREEN, PatternType.STRIPE_BOTTOM));
        newQualityItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); // Wtf mojang
        newQualityItem.setItemMeta(newQualityItemMeta);
        contents.set(row, col, ClickableItem.of(newQualityItem, e -> {
            e.setCancelled(true);
            Inventories.QUALITY_EDIT_MODIFIER_SELECT_ATTRIBUTE.open(player);
        }));

        contents.fillRow(2, ClickableItem.of(InvUtils.blankItem, InvUtils.PREVENT_PICKUP));
        // Go Back
        contents.set(2, 4, ClickableItem.of(InvUtils.arrowLeftBtn, e -> {
            e.setCancelled(true);
            player.getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_MODIFIER_EDITING);
            Inventories.QUALITY_EDIT_INVENTORY.open(player);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
