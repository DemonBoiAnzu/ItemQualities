package com.abraxas.itemqualities.api;

import com.abraxas.itemqualities.ItemQualities;
import org.bukkit.NamespacedKey;

public class Keys {
    public static NamespacedKey ITEM_QUALITY_KEY;
    public static NamespacedKey ITEM_DURABILITY_KEY;
    public static NamespacedKey MAX_ITEM_DURABILITY_KEY;
    public static NamespacedKey ITEM_PROJECTILE_DAMAGE_KEY;
    public static NamespacedKey ITEM_CRAFTED_KEY;
    public static NamespacedKey ITEM_CUSTOM_NAME_KEY;
    public static NamespacedKey PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY;
    public static NamespacedKey PLAYER_TYPING_VALUE_KEY;
    public static NamespacedKey PLAYER_QUALITY_MODIFIER_EDITING;

    static {
        ITEM_QUALITY_KEY = new NamespacedKey(ItemQualities.getInstance(), "item_quality");
        ITEM_DURABILITY_KEY = new NamespacedKey(ItemQualities.getInstance(), "durability");
        MAX_ITEM_DURABILITY_KEY = new NamespacedKey(ItemQualities.getInstance(), "max_durability");
        ITEM_PROJECTILE_DAMAGE_KEY = new NamespacedKey(ItemQualities.getInstance(), "projectile_damage");
        ITEM_CRAFTED_KEY = new NamespacedKey(ItemQualities.getInstance(), "crafted");
        ITEM_CUSTOM_NAME_KEY = new NamespacedKey(ItemQualities.getInstance(), "custom_name");
        PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY = new NamespacedKey(ItemQualities.getInstance(), "editing_previewing_quality");
        PLAYER_TYPING_VALUE_KEY = new NamespacedKey(ItemQualities.getInstance(), "typing_value");
        PLAYER_QUALITY_MODIFIER_EDITING = new NamespacedKey(ItemQualities.getInstance(), "editing_quality_modifier");
    }
}
