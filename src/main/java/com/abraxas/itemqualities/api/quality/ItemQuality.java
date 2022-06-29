package com.abraxas.itemqualities.api.quality;

import com.abraxas.itemqualities.api.QualityAttributeModifier;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;

import java.util.HashMap;
import java.util.Map;

import static com.abraxas.itemqualities.api.APIUtils.getGson;

public class ItemQuality {
    public transient NamespacedKey key;
    public String display;

    public Map<Attribute, QualityAttributeModifier> modifiers;

    public int addToItemChance;

    public int itemMaxDurabilityMod;
    public int noDurabilityLossChance;
    public int extraDurabilityLoss;
    public int extraDurabilityLossChance;

    public int noDropChance;
    public int doubleDropsChance;

    public int tier;

    public ItemQuality(NamespacedKey key, String display, int addToItemChance, int tier) {
        this.key = key;
        this.display = display;
        this.addToItemChance = addToItemChance;
        this.tier = tier;
        modifiers = new HashMap<>();
    }

    public static ItemQuality deserialize(String json) {
        return getGson().fromJson(json, ItemQuality.class);
    }

    public static String serialize(ItemQuality item) {
        return getGson().toJson(item, ItemQuality.class);
    }

    public String toString() {
        return "ItemQuality{id: %s, ".formatted(key.toString()) +
                "addChance: %s, ".formatted(addToItemChance) +
                "durMod: %s, ".formatted(itemMaxDurabilityMod) +
                "noDurLossChance: %s, ".formatted(noDurabilityLossChance) +
                "extraDurLoss: %s, ".formatted(extraDurabilityLoss) +
                "extraDurLossChance: %s, ".formatted(extraDurabilityLossChance) +
                "noDropChance: %s, ".formatted(noDropChance) +
                "doubleDropChance: %s, ".formatted(doubleDropsChance) +
                "tier: %s, ".formatted(tier) +
                "modifiers:[%s]}".formatted(modifiers.toString());
    }
}
