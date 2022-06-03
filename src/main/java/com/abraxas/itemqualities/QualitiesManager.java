package com.abraxas.itemqualities;

import com.abraxas.itemqualities.api.DurabilityManager;
import com.abraxas.itemqualities.api.ItemQualityComparator;
import com.abraxas.itemqualities.api.Keys;
import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.api.quality.ItemQuality;
import com.abraxas.itemqualities.api.quality.ItemQualityBuilder;
import com.abraxas.itemqualities.utils.Utils;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.WordUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
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
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ATTACK_DAMAGE.name(),
                        -2,
                        AttributeModifier.Operation.ADD_NUMBER,
                        HAND))

                .withAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR.name(),
                        -3,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        -2,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        -0.1,
                        AttributeModifier.Operation.ADD_NUMBER))
                .build());
        add(new ItemQualityBuilder(new NamespacedKey(main, "good"), "&2Good", 55, 1)
                .withNoDropChance(40)
                .withAdditionalDurabilityLoss(1, 90)
                .withMaxDurabilityMod(-3)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ATTACK_DAMAGE.name(),
                        -1.3,
                        AttributeModifier.Operation.ADD_NUMBER,
                        HAND))

                .withAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR.name(),
                        -2.4,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        -1.8,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        -0.05,
                        AttributeModifier.Operation.ADD_NUMBER))
                .build());
        add(new ItemQualityBuilder(new NamespacedKey(main, "great"), "&eGreat", 55, 2)
                .withNoDropChance(20)
                .withAdditionalDurabilityLoss(1, 40)
                .withMaxDurabilityMod(-3)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ATTACK_DAMAGE.name(),
                        -1,
                        AttributeModifier.Operation.ADD_NUMBER,
                        HAND))

                .withAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR.name(),
                        -2,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        -1,
                        AttributeModifier.Operation.ADD_NUMBER))
                .build());
        add(new ItemQualityBuilder(new NamespacedKey(main, "perfect"), "&aPerfect", 45, 3)
                .build());
        add(new ItemQualityBuilder(new NamespacedKey(main, "legendary"), "&6Legendary", 4, 9)
                .withNoDurabilityLossChance(60)
                .withMaxDurabilityMod(70)
                .withDoubleDropsChance(7)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ATTACK_DAMAGE.name(),
                        2.3,
                        AttributeModifier.Operation.ADD_NUMBER,
                        HAND))

                .withAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR.name(),
                        0.8,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        0.7,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        0.05,
                        AttributeModifier.Operation.ADD_NUMBER))
                .build());
        add(new ItemQualityBuilder(new NamespacedKey(main, "godly"), "&6Godly", 3, 10)
                .withNoDurabilityLossChance(70)
                .withMaxDurabilityMod(100)
                .withDoubleDropsChance(10)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ATTACK_DAMAGE.name(),
                        3,
                        AttributeModifier.Operation.ADD_NUMBER,
                        HAND))

                .withAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR.name(),
                        1,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        1,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        0.1,
                        AttributeModifier.Operation.ADD_NUMBER))
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
            }
            else {
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

            main.getLogger().info("Loading local ItemQuality files...");
            var qualities = Files.list(Path.of("%s/qualities".formatted(main.getDataFolder()))).toList();
            main.getLogger().info("Registering local ItemQuality files...");
            qualities.forEach(itemPath -> {
                try {
                    var json = Files.readString(itemPath, StandardCharsets.UTF_8);
                    if (getConfig().debugMode)
                        main.getLogger().info("Registering ItemQuality '%s'...".formatted(itemPath.getFileName()));
                    var quality = ItemQuality.deserialize(json);

                    Registries.qualitiesRegistry.register(quality.key, quality);
                    if (getConfig().debugMode)
                        main.getLogger().info("ItemQuality '%s' successfully registered!".formatted(quality.key));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            main.getLogger().info("ItemQuality registration completed.");
        } catch (IOException e) {
            main.getLogger().info("An error occurred during ItemQuality registration.");
            e.printStackTrace();
        }
    }

    public static ItemStack addQualityToItem(ItemStack itemStack, ItemQuality itemQuality) {
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemCanHaveQuality(itemStack) || itemHasQuality(itemStack)) return itemStack;

        itemMeta.getPersistentDataContainer().set(Keys.ITEM_QUALITY_KEY, PersistentDataType.STRING, itemQuality.key.getKey());

        List<String> newLore = (itemMeta.getLore() != null) ? itemMeta.getLore() : new ArrayList<>();

        if (!getConfig().displayQualityInLore) {
            String itemName = Utils.formalizedString(itemStack.getType().toString());
            itemMeta.setDisplayName(colorize(getConfig().itemQualityDisplayFormat.replace("{QUALITY}", itemQuality.display).replace("{ITEM}", itemName)));
        } else {
            newLore.add(colorize("&r%s &7Quality".formatted(itemQuality.display)));
            newLore.add("");
        }

        if (itemQuality.extraDurabilityLoss > 0)
            newLore.add(colorize("&c+%s Durability Loss".formatted(itemQuality.extraDurabilityLoss)));
        if (itemQuality.noDurabilityLossChance > 0)
            newLore.add(colorize("&aNo Durability Loss &o(%s%% Chance)".formatted(itemQuality.noDurabilityLossChance)));

        if (itemQuality.itemMaxDurabilityMod != 0)
            newLore.add(colorize("%s%s Max Durability".formatted((itemQuality.itemMaxDurabilityMod < 0) ? "&c" : "&a+", itemQuality.itemMaxDurabilityMod)));

        if (isMeleeWeapon(itemStack) || isMiningTool(itemStack) || isProjectileLauncher(itemStack)) {
            if (itemQuality.noDropChance > 0)
                newLore.add(colorize("&cNo Drops &o(%s%% Chance)".formatted(itemQuality.noDropChance)));
            else if (itemQuality.doubleDropsChance > 0)
                newLore.add(colorize("&aDouble Drops &o(%s%% Chance)".formatted(itemQuality.doubleDropsChance)));
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

            if (canAdd) {
                double initialValue = 0;

                if (defAttributes.containsKey(attributeModifier.getSlot())) {
                    var defForSlot = defAttributes.get(attributeModifier.getSlot());
                    var curDef = defForSlot.get(attribute).stream().findFirst();
                    if (curDef.isPresent()) {
                        var defaultMod = curDef.get();
                        initialValue = defaultMod.getAmount();
                    }
                }

                EquipmentSlot slot = attributeModifier.getSlot();
                if (isArmor(itemStack)) {
                    if (attribute == Attribute.GENERIC_ARMOR || attribute == Attribute.GENERIC_ARMOR_TOUGHNESS || attribute == Attribute.GENERIC_KNOCKBACK_RESISTANCE) {
                        var itemSt = itemStack.getType().toString();
                        if (itemSt.contains("HELMET")) slot = HEAD;
                        else if (itemSt.contains("CHESTPLATE") || itemSt.contains("TUNIC") || itemSt.contains("ELYTRA"))
                            slot = CHEST;
                        else if (itemSt.contains("LEGGINGS")) slot = LEGS;
                        else if (itemSt.contains("BOOTS")) slot = FEET;

                        var defForArmorSlot = defAttributes.get(slot);
                        for (Attribute attr : defForArmorSlot.keys()) {
                            if (attr.equals(attribute)) {
                                var curDefForArmorSlot = defForArmorSlot.get(attr).stream().findFirst();
                                if (curDefForArmorSlot.isPresent()) {
                                    var defaultMod = curDefForArmorSlot.get();
                                    initialValue = defaultMod.getAmount();
                                }
                            }
                        }
                    }
                }
                var newAmount = initialValue + attributeModifier.getAmount();

                var newMod = new AttributeModifier(UUID.randomUUID(),
                        attributeModifier.getName(),
                        newAmount,
                        attributeModifier.getOperation(),
                        slot);
                itemMeta.removeAttributeModifier(attribute);
                if (itemMeta.getAttributeModifiers() == null || !itemMeta.getAttributeModifiers().containsKey(attribute))
                    itemMeta.addAttributeModifier(attribute, newMod);
                newLore.add(colorize((attributeModifier.getAmount() > 0) ? "&a+" : "&c") + attributeModifier.getAmount() + " " + WordUtils.capitalize(attribute.name().toLowerCase().replace("_", " ").replace("generic ", "")));
            }
        });

        itemMeta.setLore(newLore);

        defAttributes.forEach((equipmentSlot, attributeAttributeModifierMultimap) -> {
            attributeAttributeModifierMultimap.forEach((attribute, attributeModifier) -> {
                if (itemMeta.getAttributeModifiers() == null || !itemMeta.getAttributeModifiers().containsKey(attribute))
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

        String itemName = WordUtils.capitalize(itemStack.getType().toString().toLowerCase().replace("_", " "));
        itemMeta.setDisplayName(colorize("&r" + itemName));

        itemMeta.setLore(new ArrayList<>());

        if (itemMeta.hasAttributeModifiers()) itemQuality.modifiers.forEach(itemMeta::removeAttributeModifier);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static boolean itemCanHaveQuality(ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof Damageable && itemStack.getType().getMaxDurability() > 0;
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
            if(key.getKey().equals(id)) return Registries.qualitiesRegistry.get(key);
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
