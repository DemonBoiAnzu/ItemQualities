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
import org.bukkit.Material;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.abraxas.itemqualities.utils.Utils.*;
import static org.bukkit.inventory.EquipmentSlot.*;

public class QualitiesManager {
    static ItemQualities main = ItemQualities.getInstance();

    static Config config = main.getConfiguration();

    static List<ItemQuality> exampleQualities = new ArrayList<>() {{
        add(new ItemQualityBuilder(new NamespacedKey(main,"godly"), "&6Godly", 3, 10)
                .withNoDurabilityLossChance(70)
                .withMaxDurabilityAddition(100)
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
                .build());
        add(new ItemQualityBuilder(new NamespacedKey(main,"supreme_godlike"), "&eSupreme Godlike", 1,11)
                .withNoDurabilityLossChance(95)
                .withMaxDurabilityAddition(200)
                .withDoubleDropsChance(60)
                .withAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ATTACK_DAMAGE.name(),
                        5,
                        AttributeModifier.Operation.ADD_NUMBER,
                        HAND))

                .withAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR.name(),
                        3,
                        AttributeModifier.Operation.ADD_NUMBER))
                .withAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(),
                        Attribute.GENERIC_ARMOR_TOUGHNESS.name(),
                        2,
                        AttributeModifier.Operation.ADD_NUMBER))
                .build());
        add(new ItemQualityBuilder(new NamespacedKey(main,"horrible"), "&cHorrible",60,0)
                .withNoDropChance(60)
                .withAdditionalDurabilityLoss(2,90)
                .withMaxDurabilityAddition(-10)
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

            main.getLogger().info("Loading local ItemQuality files...");
            var qualities = Files.list(Path.of("%s/qualities".formatted(main.getDataFolder()))).toList();
            main.getLogger().info("Registering local ItemQuality files...");
            qualities.forEach(itemPath -> {
                try {
                    var json = Files.readString(itemPath);
                    if (config.debugMode)
                        main.getLogger().info("Registering ItemQuality '%s'...".formatted(itemPath.getFileName()));
                    var quality = ItemQuality.deserialize(json);

                    Registries.qualitiesRegistry.register(quality.key, quality);
                    if (config.debugMode)
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

    public static void addQualityToItem(ItemStack itemStack, ItemQuality itemQuality) {
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemCanHaveQuality(itemStack) || itemHasQuality(itemStack)) return;

        itemMeta.getPersistentDataContainer().set(Keys.ITEM_QUALITY_KEY, PersistentDataType.STRING, itemQuality.key.getKey());

        List<String> newLore = (itemMeta.getLore() != null) ? itemMeta.getLore() : new ArrayList<>();
        newLore.add("");

        if(!config.displayQualityInLore) {
            String itemName = WordUtils.capitalize(itemStack.getType().toString().toLowerCase().replace("_", " "));
            itemMeta.setDisplayName(colorize(config.itemQualityDisplayFormat.replace("{QUALITY}", itemQuality.display).replace("{ITEM}", itemName)));
        } else {
            newLore.add(colorize("&r%s &rQuality".formatted(itemQuality.display)));
            newLore.add("");
        }

        if (itemQuality.extraDurabilityLoss > 0)
            newLore.add(colorize("&c+%s Durability Loss".formatted(itemQuality.extraDurabilityLoss)));
        if (itemQuality.noDurabilityLossChance > 0)
            newLore.add(colorize("&aNo Durability Loss &o(%s%% Chance)".formatted(itemQuality.noDurabilityLossChance)));

        if(!Utils.isArmor(itemStack)) {
            if (itemQuality.noDropChance > 0)
                newLore.add(colorize("&cNo Drops &o(%s%% Chance)".formatted(itemQuality.noDropChance)));
            else if (itemQuality.doubleDropsChance > 0)
                newLore.add(colorize("&aDouble Drops &o(%s%% Chance)".formatted(itemQuality.doubleDropsChance)));
        }

        newLore.add("");

        // TODO: attribute modifiers not properly adding to item along with normal defaults.
        Map<EquipmentSlot, Multimap<Attribute, AttributeModifier>> defAttributes = new HashMap<>();
        defAttributes.put(HAND,itemStack.getType().getDefaultAttributeModifiers(HAND));
        defAttributes.put(OFF_HAND,itemStack.getType().getDefaultAttributeModifiers(OFF_HAND));
        defAttributes.put(HEAD,itemStack.getType().getDefaultAttributeModifiers(HEAD));
        defAttributes.put(CHEST,itemStack.getType().getDefaultAttributeModifiers(CHEST));
        defAttributes.put(LEGS,itemStack.getType().getDefaultAttributeModifiers(LEGS));
        defAttributes.put(FEET,itemStack.getType().getDefaultAttributeModifiers(FEET));

        itemQuality.modifiers.forEach((attribute, attributeModifier) -> {
            itemMeta.removeAttributeModifier(attribute);
            double initialValue = 0;
            UUID initialUUID = UUID.randomUUID();
            if(defAttributes.containsKey(attributeModifier.getSlot())) {
                var defForSlot = defAttributes.get(attributeModifier.getSlot());
                var curDef = defForSlot.get(attribute).stream().findFirst();
                if (curDef.isPresent()) {
                    var defaultMod = curDef.get();
                    initialValue = defaultMod.getAmount();
                    initialUUID = defaultMod.getUniqueId();
                }
            }
            var canAdd = true;
            if (Utils.isArmor(itemStack)) {
                if (attribute == Attribute.GENERIC_ATTACK_DAMAGE || attribute == Attribute.GENERIC_ATTACK_SPEED || attribute == Attribute.GENERIC_ATTACK_KNOCKBACK)
                    canAdd = false;
            } else if (!Utils.isArmor(itemStack)) {
                if (attribute == Attribute.GENERIC_ARMOR || attribute == Attribute.GENERIC_ARMOR_TOUGHNESS || attribute == Attribute.GENERIC_KNOCKBACK_RESISTANCE)
                    canAdd = false;
            }

            if (canAdd) {
                var newAmount = initialValue + attributeModifier.getAmount();
                var newMod = new AttributeModifier(initialUUID,
                        attributeModifier.getName(),
                        newAmount,
                        attributeModifier.getOperation(),
                        attributeModifier.getSlot());
                itemMeta.addAttributeModifier(attribute, newMod);
                newLore.add(colorize((attributeModifier.getAmount() > 0) ? "&a+" : "&c") + attributeModifier.getAmount() + " " + WordUtils.capitalize(attribute.name().toLowerCase().replace("_", " ").replace("generic ", "")));
            }
        });

        itemMeta.setLore(newLore);

        defAttributes.forEach((equipmentSlot, attributeAttributeModifierMultimap) -> {
            itemMeta.removeAttributeModifier(equipmentSlot);
            attributeAttributeModifierMultimap.forEach(itemMeta::addAttributeModifier);
        });

        var maxDur = DurabilityManager.getItemMaxDurability(itemStack);
        if(itemQuality.itemMaxDurabilityAddition > 0) {
            maxDur += itemQuality.itemMaxDurabilityAddition;
            itemMeta.getPersistentDataContainer().set(Keys.MAX_ITEM_DURABILITY_KEY,PersistentDataType.INTEGER,maxDur);
        }

        itemStack.setItemMeta(itemMeta);
    }

    public static void removeQualityFromItem(ItemStack itemStack) {
        if (!itemCanHaveQuality(itemStack) || !itemHasQuality(itemStack)) return;

        var itemsQuality = getQuality(itemStack);
        removeQualityFromItem(itemStack, itemsQuality);
    }

    public static void removeQualityFromItem(ItemStack itemStack, ItemQuality itemQuality) {
        var itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemCanHaveQuality(itemStack) || !itemHasQuality(itemStack)) return;

        itemMeta.getPersistentDataContainer().remove(Keys.ITEM_QUALITY_KEY);
        itemMeta.getPersistentDataContainer().remove(Keys.MAX_ITEM_DURABILITY_KEY);
        itemMeta.getPersistentDataContainer().remove(Keys.ITEM_DURABILITY_KEY);

        String itemName = WordUtils.capitalize(itemStack.getType().toString().toLowerCase().replace("_"," "));
        itemMeta.setDisplayName(colorize("&r"+itemName));

        itemMeta.setLore(new ArrayList<>());

        if(itemMeta.hasAttributeModifiers()) itemQuality.modifiers.forEach(itemMeta::removeAttributeModifier);

        itemStack.setItemMeta(itemMeta);
    }

    public static boolean itemCanHaveQuality(ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof Damageable && itemStack.getType().getMaxDurability() > 0;
    }

    public static ItemQuality getRandomQuality() {
        List<ItemQuality> itemQualities = new ArrayList<>(){{
            addAll(Registries.qualitiesRegistry.getRegistry().values());
        }};
        itemQualities.sort(new ItemQualityComparator());
        Collections.reverse(itemQualities);

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
