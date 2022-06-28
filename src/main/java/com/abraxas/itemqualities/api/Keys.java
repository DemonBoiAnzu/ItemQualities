package com.abraxas.itemqualities.api;

import com.abraxas.itemqualities.ItemQualities;
import org.bukkit.NamespacedKey;

public class Keys {
    public static NamespacedKey ITEM_QUALITY;
    public static NamespacedKey ITEM_DURABILITY;
    public static NamespacedKey MAX_ITEM_DURABILITY;
    public static NamespacedKey ITEM_PROJECTILE_DAMAGE;
    public static NamespacedKey ITEM_CRAFTED;
    public static NamespacedKey ITEM_CUSTOM_NAME;
    public static NamespacedKey PLAYER_QUALITY_EDITING_OR_PREVIEWING;
    public static NamespacedKey PLAYER_TYPING_VALUE_KEY;
    public static NamespacedKey PLAYER_QUALITY_MODIFIER_EDITING;
    public static NamespacedKey PLAYER_QUALITY_MODIFIER_EDITING_SLOT;
    public static NamespacedKey PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC;
    public static NamespacedKey PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED;
    public static NamespacedKey PLAYER_QUALITY_MODIFIER_SELECTING_SLOT;

    static {
        ITEM_QUALITY = new NamespacedKey(ItemQualities.getInstance(), "item_quality");
        ITEM_DURABILITY = new NamespacedKey(ItemQualities.getInstance(), "durability");
        MAX_ITEM_DURABILITY = new NamespacedKey(ItemQualities.getInstance(), "max_durability");
        ITEM_PROJECTILE_DAMAGE = new NamespacedKey(ItemQualities.getInstance(), "projectile_damage");
        ITEM_CRAFTED = new NamespacedKey(ItemQualities.getInstance(), "crafted");
        ITEM_CUSTOM_NAME = new NamespacedKey(ItemQualities.getInstance(), "custom_name");
        PLAYER_QUALITY_EDITING_OR_PREVIEWING = new NamespacedKey(ItemQualities.getInstance(), "editing_previewing_quality");
        PLAYER_TYPING_VALUE_KEY = new NamespacedKey(ItemQualities.getInstance(), "typing_value");
        PLAYER_QUALITY_MODIFIER_EDITING = new NamespacedKey(ItemQualities.getInstance(), "editing_quality_modifier");
        PLAYER_QUALITY_MODIFIER_EDITING_SLOT = new NamespacedKey(ItemQualities.getInstance(), "editing_quality_modifier_slot");
        PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_SPECIFIC = new NamespacedKey(ItemQualities.getInstance(), "editing_quality_modifier_slot_specific_amount");
        PLAYER_QUALITY_MODIFIER_SELECTING_SLOT_FOR_IGNORED = new NamespacedKey(ItemQualities.getInstance(), "editing_quality_modifier_slot_ignored");
        PLAYER_QUALITY_MODIFIER_SELECTING_SLOT = new NamespacedKey(ItemQualities.getInstance(), "editing_quality_modifier_slot");
    }
}
