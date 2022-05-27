package com.abraxas.itemqualities.api.quality;

import com.abraxas.itemqualities.api.Registries;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

import java.util.Map;

public class ItemQualityBuilder {
    ItemQuality itemQuality;

    public ItemQualityBuilder(NamespacedKey key, String qualityDisplay, int addToItemChance, int tier){
        itemQuality = new ItemQuality(key,qualityDisplay,addToItemChance, tier);
    }

    public ItemQualityBuilder withMaxDurabilityAddition(int addition){
        itemQuality.itemMaxDurabilityAddition = addition;
        return this;
    }

    public ItemQualityBuilder withNoDropChance(int chance){
        itemQuality.noDropChance = chance;
        return this;
    }

    public ItemQualityBuilder withDoubleDropsChance(int chance){
        itemQuality.doubleDropsChance = chance;
        return this;
    }

    public ItemQualityBuilder withNoDurabilityLossChance(int chance){
        itemQuality.noDurabilityLossChance = chance;
        return this;
    }

    public ItemQualityBuilder withAdditionalDurabilityLoss(int loss, int chance){
        itemQuality.extraDurabilityLoss = loss;
        itemQuality.extraDurabilityLossChance = chance;
        return this;
    }

    public ItemQualityBuilder withAttributeModifier(Attribute attribute, AttributeModifier modifier){
        itemQuality.modifiers.put(attribute,modifier);
        return this;
    }

    public ItemQualityBuilder withAttributeModifiers(Map<Attribute,AttributeModifier> mods){
        itemQuality.modifiers.putAll(mods);
        return this;
    }

    public ItemQuality build(){
        return itemQuality;
    }

    public ItemQuality buildAndRegister(){
        return Registries.qualitiesRegistry.register(itemQuality.key,itemQuality);
    }
}
