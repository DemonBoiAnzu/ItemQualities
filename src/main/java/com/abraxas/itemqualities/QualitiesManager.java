package com.abraxas.itemqualities;

import com.abraxas.itemqualities.api.*;
import com.abraxas.itemqualities.api.quality.ItemQuality;
import com.abraxas.itemqualities.api.quality.ItemQualityBuilder;
import com.abraxas.itemqualities.utils.Utils;
import com.google.common.collect.Multimap;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.abraxas.itemqualities.utils.Utils.*;
import static org.bukkit.inventory.EquipmentSlot.*;

public class QualitiesManager {
    static ItemQualities main = ItemQualities.getInstance();

    static List<ItemQuality> exampleQualities = new ArrayList<>() {{
        add(new ItemQualityBuilder(new NamespacedKey(main, "horrible"), "&cHorrible", 60, 0)
                .withNoDropChance(60)
                .withAdditionalDurabilityLoss(2, 90)
                .withMaxDurabilityMod(-10)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new QualityAttributeModifier(-2,
                        HAND))
                .withAttributeModifier(Attribute.GENERIC_ARMOR, new QualityAttributeModifier(-3))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new QualityAttributeModifier(-2))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new QualityAttributeModifier(-0.1))
                .build());

        add(new ItemQualityBuilder(new NamespacedKey(main, "bad"), "&cBad", 57, 1)
                .withNoDropChance(55)
                .withAdditionalDurabilityLoss(2, 85)
                .withMaxDurabilityMod(-7)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new QualityAttributeModifier(-1.8,
                        HAND))
                .withAttributeModifier(Attribute.GENERIC_ARMOR, new QualityAttributeModifier(-2.6))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new QualityAttributeModifier(-1.8))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new QualityAttributeModifier(-0.08))
                .build());

        add(new ItemQualityBuilder(new NamespacedKey(main, "rusted"), "&cRusted", 50, 2)
                .withNoDropChance(49)
                .withAdditionalDurabilityLoss(2, 75)
                .withMaxDurabilityMod(-5)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new QualityAttributeModifier(-1.4,
                        HAND))
                .withAttributeModifier(Attribute.GENERIC_ARMOR, new QualityAttributeModifier(-2.4))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new QualityAttributeModifier(-1.6))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new QualityAttributeModifier(-0.07))
                .build());

        add(new ItemQualityBuilder(new NamespacedKey(main, "chipped"), "&cChipped", 48, 3)
                .withNoDropChance(55)
                .withAdditionalDurabilityLoss(2, 85)
                .withMaxDurabilityMod(-7)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new QualityAttributeModifier(-1.1,
                        HAND))
                .withAttributeModifier(Attribute.GENERIC_ARMOR, new QualityAttributeModifier(-2.3))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new QualityAttributeModifier(-1.4))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new QualityAttributeModifier(-0.05))
                .build());

        add(new ItemQualityBuilder(new NamespacedKey(main, "decent"), "&eDecent", 60, 4)
                .withNoDropChance(45)
                .withAdditionalDurabilityLoss(2, 75)
                .withMaxDurabilityMod(-4)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new QualityAttributeModifier(-1,
                        HAND))
                .withAttributeModifier(Attribute.GENERIC_ARMOR, new QualityAttributeModifier(-2))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new QualityAttributeModifier(-1.1))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new QualityAttributeModifier(-0.03))
                .build());

        add(new ItemQualityBuilder(new NamespacedKey(main, "good"), "&2Good", 50, 5)
                .withNoDropChance(40)
                .withAdditionalDurabilityLoss(1, 65)
                .withMaxDurabilityMod(-3)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new QualityAttributeModifier(-0.5,
                        HAND))
                .withAttributeModifier(Attribute.GENERIC_ARMOR, new QualityAttributeModifier(-1.7))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new QualityAttributeModifier(-1))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new QualityAttributeModifier(-0.01))
                .build());

        add(new ItemQualityBuilder(new NamespacedKey(main, "great"), "&eGreat", 48, 6)
                .withNoDropChance(10)
                .withAdditionalDurabilityLoss(1, 15)
                .withMaxDurabilityMod(-3)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new QualityAttributeModifier(-1,
                        HAND))
                .withAttributeModifier(Attribute.GENERIC_ARMOR, new QualityAttributeModifier(-0.7))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new QualityAttributeModifier(-0.009))
                .build());

        add(new ItemQualityBuilder(new NamespacedKey(main, "perfect"), "&aPerfect", 45, 8)
                .build());

        add(new ItemQualityBuilder(new NamespacedKey(main, "legendary"), "&6Legendary", 4, 9)
                .withNoDurabilityLossChance(60)
                .withMaxDurabilityMod(70)
                .withDoubleDropsChance(7)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new QualityAttributeModifier(2.3,
                        HAND))
                .withAttributeModifier(Attribute.GENERIC_ARMOR, new QualityAttributeModifier(0.8))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new QualityAttributeModifier(0.7))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new QualityAttributeModifier(0.05))
                .build());

        add(new ItemQualityBuilder(new NamespacedKey(main, "godly"), "&6Godly", 3, 10)
                .withNoDurabilityLossChance(70)
                .withMaxDurabilityMod(100)
                .withDoubleDropsChance(10)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new QualityAttributeModifier(3,
                        HAND))
                .withAttributeModifier(Attribute.GENERIC_ARMOR, new QualityAttributeModifier(1))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new QualityAttributeModifier(1))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new QualityAttributeModifier(0.1))
                .build());
    }};

    public static void loadAndRegister() {
        try {
            if (!Files.exists(Path.of("%s/qualities/".formatted(main.getDataFolder()))))
                Files.createDirectory(Path.of("%s/qualities".formatted(main.getDataFolder())));

            if (main.config.exampleItemQualitiesEnabled) {
                exampleQualities.forEach(i -> {
                    var path = Path.of("%s/qualities/%s.json".formatted(main.getDataFolder(), i.key.getKey()));
                    if (!Files.exists(path)) {
                        i.display = colorize(i.display);
                        var itDes = ItemQuality.serialize(i);
                        try {
                            var f = new File(String.valueOf(path));
                            f.createNewFile();
                            Files.writeString(f.toPath(), itDes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                exampleQualities.forEach(i -> {
                    var path = Path.of("%s/qualities/%s.json".formatted(main.getDataFolder(), i.key));
                    if (Files.exists(path)) {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Registries.qualitiesRegistry.unregister(i.key);
                });
            }
            Registries.qualitiesRegistry.getRegistry().clear();

            Utils.log(main.getTranslation("message.plugin.loading_local_quality_files"));
            var qualities = Files.list(Path.of("%s/qualities".formatted(main.getDataFolder()))).toList();
            Utils.log(main.getTranslation("message.plugin.registering_qualities"));
            qualities.forEach(itemPath -> {
                try {
                    var json = Files.readString(itemPath, StandardCharsets.UTF_8);
                    if (getConfig().debugMode)
                        Utils.log(main.getTranslation("message.plugin.registering_quality").formatted(itemPath.getFileName()));
                    var quality = ItemQuality.deserialize(json);

                    Registries.qualitiesRegistry.register(quality.key, quality);
                    if (getConfig().debugMode)
                        Utils.log(main.getTranslation("message.plugin.quality_registered").formatted(quality.key));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Utils.log(main.getTranslation("message.plugin.registration_complete"));
            Utils.log(main.getTranslation("message.plugin.custom_quality_reminder").formatted("https://github.com/Steel-Dev/ItemQualities/wiki/Creating"));
        } catch (IOException e) {
            Utils.log(main.getTranslation("message.plugin.registration_error"));
            e.printStackTrace();
        }
    }

    public static ItemStack refreshItem(ItemStack itemStack) {
        return refreshItem(itemStack, null);
    }

    public static ItemStack refreshItem(ItemStack itemStack, ItemQuality updatedQuality) {
        var itemsQuality = (updatedQuality != null) ? updatedQuality : getQuality(itemStack);
        removeQualityFromItem(itemStack);
        return addQualityToItem(itemStack, (itemsQuality == null) ? getRandomQuality() : itemsQuality);
    }

    public static ItemStack addQualityToItem(ItemStack itemStack, ItemQuality itemQuality) {
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemCanHaveQuality(itemStack) || itemHasQuality(itemStack)) return itemStack;
        if (itemMeta.getItemFlags().contains(ItemFlag.HIDE_ATTRIBUTES)) return itemStack;

        itemMeta.getPersistentDataContainer().set(Keys.ITEM_QUALITY_KEY, PersistentDataType.STRING, itemQuality.key.getKey());

        List<String> newLore = (itemMeta.getLore() != null) ? itemMeta.getLore() : new ArrayList<>();
        if (!getConfig().displayQualityInLore) {
            var customItemName = itemMeta.getPersistentDataContainer().getOrDefault(Keys.ITEM_CUSTOM_NAME_KEY, PersistentDataType.STRING, "");
            String itemName = (!customItemName.isEmpty()) ? customItemName : new TranslatableComponent("item.minecraft.%s".formatted(itemStack.getType().toString().toLowerCase())).toPlainText();
            itemMeta.setDisplayName(colorize(getConfig().itemQualityDisplayFormat.replace("{QUALITY}", itemQuality.display).replace("{ITEM}", itemName)));
        } else {
            newLore.add(colorize("&r%s %s".formatted(itemQuality.display, main.getTranslation("lore.stat.quality"))));
            newLore.add("");
        }

        if (itemQuality.extraDurabilityLoss > 0)
            newLore.add(colorize("&c+%s %s".formatted(itemQuality.extraDurabilityLoss, main.getTranslation("lore.stat.durability_loss"))));
        if (itemQuality.noDurabilityLossChance > 0)
            newLore.add(colorize("&a%s".formatted(main.getTranslation("lore.stat.no_durability_loss").formatted(itemQuality.noDurabilityLossChance))));

        if (itemQuality.itemMaxDurabilityMod != 0)
            newLore.add(colorize("%s%s %s".formatted((itemQuality.itemMaxDurabilityMod < 0) ? "&c" : "&a+", itemQuality.itemMaxDurabilityMod, main.getTranslation("lore.stat.max_durability"))));

        if (isMeleeWeapon(itemStack) || isMiningTool(itemStack) || isProjectileLauncher(itemStack)) {
            if (itemQuality.noDropChance > 0)
                newLore.add(colorize("&c%s".formatted(main.getTranslation("lore.stat.no_drops").formatted(itemQuality.noDropChance))));
            else if (itemQuality.doubleDropsChance > 0)
                newLore.add(colorize("&a%s".formatted(main.getTranslation("lore.stat.double_drops").formatted(itemQuality.doubleDropsChance))));
        }

        if (itemQuality.modifiers.size() > 0) newLore.add("");

        Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> defAttributes = new HashMap<>();
        defAttributes.put(HAND, itemStack.getType().getDefaultAttributeModifiers(HAND));
        defAttributes.put(OFF_HAND, itemStack.getType().getDefaultAttributeModifiers(OFF_HAND));
        defAttributes.put(HEAD, itemStack.getType().getDefaultAttributeModifiers(HEAD));
        defAttributes.put(CHEST, itemStack.getType().getDefaultAttributeModifiers(CHEST));
        defAttributes.put(LEGS, itemStack.getType().getDefaultAttributeModifiers(LEGS));
        defAttributes.put(FEET, itemStack.getType().getDefaultAttributeModifiers(FEET));

        if (itemMeta.hasAttributeModifiers())
            itemMeta.getAttributeModifiers().forEach(itemMeta::removeAttributeModifier);

        itemQuality.modifiers.forEach((attribute, attributeModifier) -> {
            var canAdd = true;

            switch (attribute) {
                case GENERIC_ATTACK_DAMAGE:
                case GENERIC_ATTACK_SPEED:
                case GENERIC_ATTACK_KNOCKBACK:
                    if (!isMeleeWeapon(itemStack) && !isProjectileLauncher(itemStack)) canAdd = false;
                    break;

                case GENERIC_ARMOR:
                case GENERIC_KNOCKBACK_RESISTANCE:
                case GENERIC_ARMOR_TOUGHNESS:
                    if (!isArmor(itemStack)) canAdd = false;
                    break;
            }

            EquipmentSlot slot = attributeModifier.getSlot(itemStack);
            if (slot != Utils.getItemsMainSlot(itemStack)) canAdd = false;
            if (attributeModifier.ignoredSlots != null && attributeModifier.ignoredSlots.contains(slot)) canAdd = false;

            if (canAdd) {
                double initialValue = 0;

                if (defAttributes.containsKey(attributeModifier.getSlot(itemStack))) {
                    var defForSlot = defAttributes.get(attributeModifier.getSlot(itemStack));
                    var curDef = defForSlot.get(attribute).stream().findFirst();
                    if (curDef.isPresent()) {
                        var defaultMod = curDef.get();
                        initialValue = defaultMod.getAmount();
                    }
                }

                var sharpnessLevel = itemStack.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                var newAmount = initialValue + attributeModifier.getAmount(slot) + ((sharpnessLevel == 1) ? 1 :
                        (sharpnessLevel == 2) ? 1.5d :
                                (sharpnessLevel == 3) ? 2 :
                                        (sharpnessLevel == 4) ? 2.5d :
                                                (sharpnessLevel == 5) ? 3 : 0);
                var newMod = new QualityAttributeModifier(newAmount,
                        slot);
                itemMeta.removeAttributeModifier(attribute);
                if (itemMeta.getAttributeModifiers() == null || !itemMeta.getAttributeModifiers().containsKey(attribute))
                    itemMeta.addAttributeModifier(attribute, newMod.getModifier(itemStack, attribute));
                var attrN = attribute.name().toLowerCase().replace("generic_", "generic.");
                var attrTrans = new TranslatableComponent("attribute.name.%s".formatted(attrN));

                var attrFinal = attrTrans.toPlainText();
                newLore.add(colorize((attributeModifier.getAmount(slot) > 0) ? "&a+" : "&c") + attributeModifier.getAmount(slot) + " " + attrFinal);
            }
        });

        itemMeta.setLore(newLore);

        defAttributes.forEach((equipmentSlot, attributeAttributeModifierMultimap) -> {
            attributeAttributeModifierMultimap.forEach((attribute, attributeModifier) -> {
                if (itemMeta.getAttributeModifiers() != null &&
                        !itemMeta.getAttributeModifiers().containsKey(attribute) &&
                        !itemMeta.getAttributeModifiers().containsValue(attributeModifier))
                    itemMeta.addAttributeModifier(attribute, attributeModifier);
            });
        });

        var maxDur = DurabilityManager.getItemMaxDurability(itemStack);
        if (itemQuality.itemMaxDurabilityMod > 0) {
            maxDur += itemQuality.itemMaxDurabilityMod;
            itemMeta.getPersistentDataContainer().set(Keys.MAX_ITEM_DURABILITY_KEY, PersistentDataType.INTEGER, maxDur);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack removeQualityFromItem(ItemStack itemStack) {
        if (!itemCanHaveQuality(itemStack) || !itemHasQuality(itemStack)) return itemStack;

        var itemsQuality = getQuality(itemStack);
        return removeQualityFromItem(itemStack, itemsQuality);
    }

    public static ItemStack removeQualityFromItem(ItemStack itemStack, ItemQuality itemQuality) {
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemCanHaveQuality(itemStack) || !itemHasQuality(itemStack)) return itemStack;

        itemMeta.getPersistentDataContainer().remove(Keys.ITEM_QUALITY_KEY);
        itemMeta.getPersistentDataContainer().remove(Keys.MAX_ITEM_DURABILITY_KEY);
        itemMeta.getPersistentDataContainer().remove(Keys.ITEM_DURABILITY_KEY);

        var customItemName = itemMeta.getPersistentDataContainer().getOrDefault(Keys.ITEM_CUSTOM_NAME_KEY, PersistentDataType.STRING, "");
        String itemName = (!customItemName.isEmpty()) ? customItemName : new TranslatableComponent("item.minecraft.%s".formatted(itemStack.getType().toString().toLowerCase())).toPlainText();
        itemMeta.setDisplayName(colorize("&r" + itemName));

        itemMeta.setLore(new ArrayList<>());

        if (itemMeta.hasAttributeModifiers()) itemQuality.modifiers.forEach((attribute, qualityAttributeModifier) -> {
            itemMeta.removeAttributeModifier(attribute);
            itemMeta.removeAttributeModifier(attribute, qualityAttributeModifier.getModifier(itemStack, attribute));
        });

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static boolean itemCanHaveQuality(ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof Damageable && itemStack.getType().getMaxDurability() > 0 && !getConfig().itemBlacklist.contains(itemStack.getType());
    }

    public static ItemQuality getRandomQuality() {
        return getRandomQuality(null);
    }

    public static ItemQuality getRandomQuality(ItemQuality exclude) {
        List<ItemQuality> itemQualities = new ArrayList<>() {{
            addAll(Registries.qualitiesRegistry.getRegistry().values());
        }};
        itemQualities.sort(new ItemQualityComparator());
        Collections.reverse(itemQualities);
        if (exclude != null) itemQualities.remove(exclude);

        for (ItemQuality quality : itemQualities) {
            if (chanceOf(quality.addToItemChance)) return quality;
        }

        return itemQualities.get(Utils.getRandom().nextInt(itemQualities.size()));
    }

    public static ItemQuality getQualityById(String id) {
        for (NamespacedKey key : Registries.qualitiesRegistry.getRegistry().keySet()) {
            if (key.getKey().equals(id)) return Registries.qualitiesRegistry.get(key);
        }
        return null;
    }

    public static ItemQuality getQualityById(NamespacedKey id) {
        return Registries.qualitiesRegistry.get(id);
    }

    public static ItemQuality getQuality(ItemStack itemStack) {
        if (!itemHasQuality(itemStack)) return null;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return null;
        return getQualityById(meta.getPersistentDataContainer().get(Keys.ITEM_QUALITY_KEY, PersistentDataType.STRING));
    }

    public static boolean itemHasQuality(ItemStack itemStack) {
        if (itemStack.getItemMeta() == null) return false;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(Keys.ITEM_QUALITY_KEY, PersistentDataType.STRING);
    }
}
