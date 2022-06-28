package com.abraxas.itemqualities.inventories;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.inventories.providers.*;
import com.abraxas.itemqualities.utils.Utils;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.event.inventory.InventoryType;

public class Inventories {
    public static final SmartInventory QUALITY_MANAGER_INVENTORY = SmartInventory.builder()
            .id("quality_manager")
            .provider(new IPQualityManager())
            .type(InventoryType.CHEST)
            .size(6, 9)
            .title(Utils.colorize("&8ItemQuality Management"))
            .manager(ItemQualities.getInstance().getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_EDIT_INVENTORY = SmartInventory.builder()
            .id("quality_edit")
            .provider(new IPQualityEdit())
            .type(InventoryType.CHEST)
            .size(3, 9)
            .title(Utils.colorize("&8Edit ItemQuality"))
            .manager(ItemQualities.getInstance().getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_PREVIEW_INVENTORY = SmartInventory.builder()
            .id("quality_preview")
            .provider(new IPQualityPreview())
            .type(InventoryType.CHEST)
            .size(3, 9)
            .title(Utils.colorize("&8ItemQuality Preview"))
            .manager(ItemQualities.getInstance().getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_MODIFIERS_LIST = SmartInventory.builder()
            .id("quality_edit_modifiers")
            .provider(new IPQualityModifiersList())
            .type(InventoryType.CHEST)
            .size(3, 9)
            .title(Utils.colorize("&8ItemQuality Modifiers"))
            .manager(ItemQualities.getInstance().getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_MODIFIER_SELECT_SLOT = SmartInventory.builder()
            .id("quality_modifier_select_slot")
            .provider(new IPQualityEditModSelSlot())
            .type(InventoryType.CHEST)
            .size(3, 9)
            .title(Utils.colorize("&8ItemQuality Slots"))
            .manager(ItemQualities.getInstance().getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_MODIFIER_SLOT_SPEC_AM_LIST = SmartInventory.builder()
            .id("quality_edit_modifiers_slots")
            .provider(new IPQualityModifierSlotSpecAmList())
            .type(InventoryType.CHEST)
            .size(3, 9)
            .title(Utils.colorize("&8ItemQuality Slots"))
            .manager(ItemQualities.getInstance().getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_EDIT_MODIFIER = SmartInventory.builder()
            .id("quality_edit_modifier")
            .provider(new IPQualityEditModifer())
            .type(InventoryType.CHEST)
            .size(3, 9)
            .title(Utils.colorize("&8Edit ItemQuality Modifier"))
            .manager(ItemQualities.getInstance().getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_EDIT_MODIFIER_SELECT_ATTRIBUTE = SmartInventory.builder()
            .id("quality_edit_modifier_select_attribute")
            .provider(new IPQualityEditModSelAttr())
            .type(InventoryType.CHEST)
            .size(3, 9)
            .title(Utils.colorize("&8Select ItemQuality Attribute"))
            .manager(ItemQualities.getInstance().getInventoryManager())
            .build();
}
