package com.abraxas.itemqualities.api;

import com.abraxas.itemqualities.ItemQualities;
import org.bukkit.NamespacedKey;

public class Keys {
    public static NamespacedKey ITEM_QUALITY_KEY;
    public static NamespacedKey ITEM_DURABILITY_KEY;
    public static NamespacedKey MAX_ITEM_DURABILITY_KEY;

    static {
        ITEM_QUALITY_KEY = new NamespacedKey(ItemQualities.getInstance(),"item_quality");
        ITEM_DURABILITY_KEY = new NamespacedKey(ItemQualities.getInstance(),"durability");
        MAX_ITEM_DURABILITY_KEY = new NamespacedKey(ItemQualities.getInstance(),"max_durability");
    }
}
