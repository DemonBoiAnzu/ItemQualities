package com.abraxas.itemqualities.api;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.abraxas.itemqualities.utils.Utils.getItemsMainSlot;
import static java.util.UUID.randomUUID;
import static org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER;

public class QualityAttributeModifier implements Serializable {
    public double amount;
    public Map<EquipmentSlot, Double> slotSpecificAmounts;
    public EquipmentSlot slot;
    public List<EquipmentSlot> ignoredSlots;

    public QualityAttributeModifier(double amount, EquipmentSlot slot) {
        this.amount = amount;
        this.slot = slot;
    }

    public QualityAttributeModifier(double amount) {
        this.amount = amount;
    }

    public QualityAttributeModifier(double amount, List<EquipmentSlot> ignoredSlots) {
        this.amount = amount;
        this.ignoredSlots = ignoredSlots;
    }

    public QualityAttributeModifier(Map<EquipmentSlot, Double> slotSpecificAmounts) {
        this.slotSpecificAmounts = slotSpecificAmounts;
    }

    public QualityAttributeModifier(Map<EquipmentSlot, Double> slotSpecificAmounts, List<EquipmentSlot> ignoredSlots) {
        this.slotSpecificAmounts = slotSpecificAmounts;
        this.ignoredSlots = ignoredSlots;
    }

    public AttributeModifier getModifier(ItemStack itemStack, Attribute attr) {
        return new AttributeModifier(randomUUID(), attr.getKey().toString(), getAmount(getSlot(itemStack)), ADD_NUMBER, getSlot(itemStack));
    }

    public EquipmentSlot getSlot(ItemStack itemStack) {
        if (slot == null) return getItemsMainSlot(itemStack);
        return slot;
    }

    public double getAmount(EquipmentSlot slot) {
        if (slotSpecificAmounts != null && slotSpecificAmounts.size() > 0)
            return slotSpecificAmounts.getOrDefault(slot, 0d);
        return amount;
    }
}
