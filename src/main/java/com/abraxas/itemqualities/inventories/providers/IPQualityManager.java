package com.abraxas.itemqualities.inventories.providers;

import com.abraxas.itemqualities.api.ItemQualityComparator;
import com.abraxas.itemqualities.api.quality.ItemQuality;
import com.abraxas.itemqualities.inventories.utils.InvUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.abraxas.itemqualities.ItemQualities.getInstance;
import static com.abraxas.itemqualities.api.Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING;
import static com.abraxas.itemqualities.api.Keys.PLAYER_TYPING_VALUE_KEY;
import static com.abraxas.itemqualities.api.Registries.qualitiesRegistry;
import static com.abraxas.itemqualities.inventories.Inventories.*;
import static com.abraxas.itemqualities.inventories.utils.InvUtils.*;
import static com.abraxas.itemqualities.utils.QualityChatValues.NEW_QUALITY_ID;
import static com.abraxas.itemqualities.utils.Utils.colorize;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;
import static fr.minuskube.inv.content.SlotIterator.Type.HORIZONTAL;
import static org.bukkit.DyeColor.GREEN;
import static org.bukkit.DyeColor.LIME;
import static org.bukkit.Material.GREEN_BANNER;
import static org.bukkit.Material.PAPER;
import static org.bukkit.block.banner.PatternType.*;
import static org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS;
import static org.bukkit.persistence.PersistentDataType.STRING;

// TODO: Finish translations for new messages
public class IPQualityManager implements InventoryProvider {
    @Override
    public void init(Player player, InventoryContents contents) {
        var pagination = contents.pagination();
        pagination.setItemsPerPage(45);

        List<ItemQuality> registeredQualities = new ArrayList<>() {{
            addAll(qualitiesRegistry.getRegistry().values());
        }};
        registeredQualities.sort(new ItemQualityComparator());
        Collections.reverse(registeredQualities);
        var items = new ClickableItem[registeredQualities.size()];

        for (int i = 0; i < registeredQualities.size(); i++) {
            var quality = registeredQualities.get(i);
            var qualityItem = new ItemStack(PAPER);
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
                player.getPersistentDataContainer().set(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, quality.key.toString());
                if (e.isLeftClick())
                    QUALITY_PREVIEW_INVENTORY.open(player);
                else if (e.isRightClick())
                    QUALITY_EDIT_INVENTORY.open(player);
            });

            items[i] = qualityClickableItem;
        }

        pagination.setItems(items);

        pagination.addToIterator(contents.newIterator(HORIZONTAL, 0, 0));

        if (contents.firstEmpty().isPresent()) {
            var firstEmpty = contents.firstEmpty().get();
            var newQualityItem = new ItemStack(GREEN_BANNER);
            var newQualityItemMeta = (BannerMeta) newQualityItem.getItemMeta();
            newQualityItemMeta.setDisplayName(colorize("&aCreate New"));
            newQualityItemMeta.setLore(new ArrayList<>() {{
                add(colorize("&7Create a new Item Quality."));
            }});
            newQualityItemMeta.addPattern(new Pattern(LIME, STRAIGHT_CROSS));
            newQualityItemMeta.addPattern(new Pattern(GREEN, BORDER));
            newQualityItemMeta.addPattern(new Pattern(GREEN, STRIPE_TOP));
            newQualityItemMeta.addPattern(new Pattern(GREEN, STRIPE_BOTTOM));
            newQualityItemMeta.addItemFlags(HIDE_POTION_EFFECTS); // Wtf mojang
            newQualityItem.setItemMeta(newQualityItemMeta);
            contents.set(firstEmpty, ClickableItem.of(newQualityItem, e -> {
                e.setCancelled(true);
                player.getPersistentDataContainer().set(PLAYER_TYPING_VALUE_KEY, STRING, NEW_QUALITY_ID);
                sendMessageWithPrefix(player, getInstance().getTranslation("message.plugin.quality_creation.enter_value").formatted("ID"));
                player.closeInventory();
            }));
        }

        contents.fillRow(5, ClickableItem.of(blankItem, PREVENT_PICKUP));
        // Back
        contents.set(5, 3, ClickableItem.of(arrowLeftBtn, e -> QUALITY_MANAGER_INVENTORY.open(player, pagination.previous().getPage())));
        // Close GUI
        contents.set(5, 4, ClickableItem.of(closeBtn, CLOSE_GUI));
        // Next Page
        contents.set(5, 5, ClickableItem.of(InvUtils.arrowRightBtn, e -> QUALITY_MANAGER_INVENTORY.open(player, pagination.next().getPage())));

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                var it = contents.get(row, column);
                if (it.isEmpty())
                    contents.set(row, column, ClickableItem.of(blankItemSecondary, PREVENT_PICKUP));
            }
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
