package com.abraxas.itemqualities.inventories;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.inventories.providers.*;
import fr.minuskube.inv.SmartInventory;

import static com.abraxas.itemqualities.utils.Utils.colorize;
import static org.bukkit.event.inventory.InventoryType.CHEST;

public class Inventories {
    static ItemQualities main = ItemQualities.getInstance();

    public static final SmartInventory QUALITY_MANAGER_INVENTORY = SmartInventory.builder()
            .id("quality_manager")
            .provider(new IPQualityManager())
            .type(CHEST)
            .size(6, 9)
            .title(colorize("&8ItemQuality Management"))
            .manager(main.getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_EDIT_INVENTORY = SmartInventory.builder()
            .id("quality_edit")
            .provider(new IPQualityEdit())
            .type(CHEST)
            .size(3, 9)
            .title(colorize("&8Edit ItemQuality"))
            .manager(main.getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_PREVIEW_INVENTORY = SmartInventory.builder()
            .id("quality_preview")
            .provider(new IPQualityPreview())
            .type(CHEST)
            .size(3, 9)
            .title(colorize("&8ItemQuality Preview"))
            .manager(main.getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_MODIFIERS_LIST = SmartInventory.builder()
            .id("quality_edit_modifiers")
            .provider(new IPQualityModifiersList())
            .type(CHEST)
            .size(3, 9)
            .title(colorize("&8ItemQuality Modifiers"))
            .manager(main.getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_MODIFIER_SELECT_SLOT = SmartInventory.builder()
            .id("quality_modifier_select_slot")
            .provider(new IPQualityEditModSelSlot())
            .type(CHEST)
            .size(3, 9)
            .title(colorize("&8ItemQuality Slots"))
            .manager(main.getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_MODIFIER_SLOT_SPEC_AM_LIST = SmartInventory.builder()
            .id("quality_edit_modifiers_slots")
            .provider(new IPQualityModifierSlotSpecAmList())
            .type(CHEST)
            .size(3, 9)
            .title(colorize("&8ItemQuality Slots"))
            .manager(main.getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_EDIT_MODIFIER = SmartInventory.builder()
            .id("quality_edit_modifier")
            .provider(new IPQualityEditModifier())
            .type(CHEST)
            .size(3, 9)
            .title(colorize("&8Edit ItemQuality Modifier"))
            .manager(main.getInventoryManager())
            .build();

    public static final SmartInventory QUALITY_EDIT_MODIFIER_SELECT_ATTRIBUTE = SmartInventory.builder()
            .id("quality_edit_modifier_select_attribute")
            .provider(new IPQualityEditModSelAttr())
            .type(CHEST)
            .size(3, 9)
            .title(colorize("&8Select ItemQuality Attribute"))
            .manager(main.getInventoryManager())
            .build();
}
