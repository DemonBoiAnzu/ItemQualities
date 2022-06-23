package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.api.ItemQualityComparator;
import com.abraxas.itemqualities.api.Keys;
import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.api.quality.ItemQuality;
import com.abraxas.itemqualities.inventories.Inventories;
import com.abraxas.itemqualities.inventories.utils.InvUtils;
import com.abraxas.itemqualities.utils.QualityChatValues;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.abraxas.itemqualities.utils.Utils.colorize;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;

// TODO: Finish up the editing, add deletion system and finish translations for new messages
public class QualityManagerInvProvider implements InventoryProvider {
    @Override
    public void init(Player player, InventoryContents contents) {
        var pagination = contents.pagination();
        pagination.setItemsPerPage(45);

        List<ItemQuality> registeredQualities = new ArrayList<>() {{
            addAll(Registries.qualitiesRegistry.getRegistry().values());
        }};
        registeredQualities.sort(new ItemQualityComparator());
        Collections.reverse(registeredQualities);
        var items = new ClickableItem[registeredQualities.size()];

        for (int i = 0; i < registeredQualities.size(); i++) {
            var quality = registeredQualities.get(i);
            var qualityItem = new ItemStack(Material.PAPER);
            var qualityItemMeta = qualityItem.getItemMeta();
            qualityItemMeta.setDisplayName(colorize(quality.display + " &bQuality"));
            qualityItemMeta.setLore(new ArrayList<>() {{
                add(colorize("&bTier: &e%s".formatted(quality.tier)));
                add("");
                add(colorize("&eLeft-Click &7to preview."));
                add(colorize("&eRight-Click &7to edit."));
                add("");
                add(colorize("&9&oFrom '%s'".formatted(quality.key.getNamespace())));
            }});
            qualityItem.setItemMeta(qualityItemMeta);
            var qualityClickableItem = ClickableItem.of(qualityItem, e -> {
                player.getPersistentDataContainer().set(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY, PersistentDataType.STRING, quality.key.toString());
                if (e.isLeftClick())
                    Inventories.QUALITY_PREVIEW_INVENTORY.open(player);
                else if (e.isRightClick())
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
            });

            items[i] = qualityClickableItem;
        }

        pagination.setItems(items);

        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));

        if (contents.firstEmpty().isPresent()) {
            var firstEmpty = contents.firstEmpty().get();
            var newQualityItem = new ItemStack(Material.GREEN_BANNER);
            var newQualityItemMeta = (BannerMeta) newQualityItem.getItemMeta();
            newQualityItemMeta.setDisplayName(colorize("&aCreate New"));
            newQualityItemMeta.setLore(new ArrayList<>() {{
                add(colorize("&7Create a new Item Quality."));
            }});
            newQualityItemMeta.addPattern(new Pattern(DyeColor.LIME, PatternType.STRAIGHT_CROSS));
            newQualityItemMeta.addPattern(new Pattern(DyeColor.GREEN, PatternType.BORDER));
            newQualityItemMeta.addPattern(new Pattern(DyeColor.GREEN, PatternType.STRIPE_TOP));
            newQualityItemMeta.addPattern(new Pattern(DyeColor.GREEN, PatternType.STRIPE_BOTTOM));
            newQualityItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); // Wtf mojang
            newQualityItem.setItemMeta(newQualityItemMeta);
            contents.set(firstEmpty, ClickableItem.of(newQualityItem, e -> {
                e.setCancelled(true);
                player.getPersistentDataContainer().set(Keys.PLAYER_TYPING_VALUE_KEY, PersistentDataType.STRING, QualityChatValues.NEW_QUALITY_ID);
                sendMessageWithPrefix(player, ItemQualities.getInstance().getTranslation("message.plugin.qualitycreation.enterid"));
                player.closeInventory();
            }));
        }

        contents.fillRow(5, ClickableItem.of(InvUtils.blankItem, InvUtils.PREVENT_PICKUP));
        // Back
        contents.set(5, 3, ClickableItem.of(InvUtils.arrowLeftBtn, e -> Inventories.QUALITY_MANAGER_INVENTORY.open(player, pagination.previous().getPage())));
        // Close GUI
        contents.set(5, 4, ClickableItem.of(InvUtils.closeBtn, InvUtils.CLOSE_GUI));
        // Next Page
        contents.set(5, 5, ClickableItem.of(InvUtils.arrowRightBtn, e -> Inventories.QUALITY_MANAGER_INVENTORY.open(player, pagination.next().getPage())));

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                var it = contents.get(row, column);
                if (it.isEmpty())
                    contents.set(row, column, ClickableItem.of(InvUtils.blankItemSecondary, InvUtils.PREVENT_PICKUP));
            }
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
