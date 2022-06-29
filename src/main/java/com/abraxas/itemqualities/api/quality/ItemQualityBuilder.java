package com.abraxas.itemqualities.api.quality;

import com.abraxas.itemqualities.api.QualityAttributeModifier;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;

import java.util.Map;

import static com.abraxas.itemqualities.api.Registries.qualitiesRegistry;

public class ItemQualityBuilder {
    ItemQuality itemQuality;

    public ItemQualityBuilder(NamespacedKey key, String qualityDisplay, int addToItemChance, int tier) {
        itemQuality = new ItemQuality(key, qualityDisplay, addToItemChance, tier);
    }

    public ItemQualityBuilder withMaxDurabilityMod(int addition) {
        itemQuality.itemMaxDurabilityMod = addition;
        return this;
    }

    public ItemQualityBuilder withNoDropChance(int chance) {
        itemQuality.noDropChance = chance;
        return this;
    }

    public ItemQualityBuilder withDoubleDropsChance(int chance) {
        itemQuality.doubleDropsChance = chance;
        return this;
    }

    public ItemQualityBuilder withNoDurabilityLossChance(int chance) {
        itemQuality.noDurabilityLossChance = chance;
        return this;
    }

    public ItemQualityBuilder withAdditionalDurabilityLoss(int loss, int chance) {
        itemQuality.extraDurabilityLoss = loss;
        itemQuality.extraDurabilityLossChance = chance;
        return this;
    }

    public ItemQualityBuilder withAttributeModifier(Attribute attribute, QualityAttributeModifier modifier) {
        itemQuality.modifiers.put(attribute, modifier);
        return this;
    }

    public ItemQualityBuilder withAttributeModifiers(Map<Attribute, QualityAttributeModifier> mods) {
        itemQuality.modifiers.putAll(mods);
        return this;
    }

    public ItemQuality build() {
        return itemQuality;
    }

    public ItemQuality buildAndRegister() {
        return qualitiesRegistry.register(itemQuality.key, itemQuality);
    }
}
