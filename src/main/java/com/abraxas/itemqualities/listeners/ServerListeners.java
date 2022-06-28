package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.api.Keys;
import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.api.quality.ItemQuality;
import com.abraxas.itemqualities.inventories.Inventories;
import com.abraxas.itemqualities.utils.QualityChatValues;
import com.abraxas.itemqualities.utils.UpdateChecker;
import com.abraxas.itemqualities.utils.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (Utils.getConfig().newUpdateMessageOnJoin)
            UpdateChecker.sendNewVersionNotif(event.getPlayer());
        event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING);
        event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        var player = event.getPlayer();
        if (!player.getPersistentDataContainer().has(Keys.PLAYER_TYPING_VALUE_KEY, PersistentDataType.STRING)) return;
        var valueId = player.getPersistentDataContainer().get(Keys.PLAYER_TYPING_VALUE_KEY, PersistentDataType.STRING);

        if (!Utils.canUseQualityManager(player)) {
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING);
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
            return;
        }

        if (event.getMessage().equals("cancel")) {
            event.setCancelled(true);
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING);
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.canceled"));
            return;
        }

        if (valueId.equals(QualityChatValues.NEW_QUALITY_ID)) {
            event.setCancelled(true);
            var value = event.getMessage().toLowerCase().replace(" ", "_");
            var newQuality = new ItemQuality(new NamespacedKey(main, value), "&r%s".formatted(event.getMessage()), 0, 0);
            if (Registries.qualitiesRegistry.contains(newQuality.key)) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_already_exists").formatted(newQuality.key));
                event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING);
                event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
                return;
            }
            QualitiesManager.register(newQuality);
            QualitiesManager.saveQualityToFile(newQuality);
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
            event.getPlayer().getPersistentDataContainer().set(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, newQuality.key.toString());
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("quality id", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_ID)) {
            event.setCancelled(true);
            var value = event.getMessage().toLowerCase().replace(" ", "_");
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            QualitiesManager.deleteQuality(quality);
            var newKey = new NamespacedKey(rawQualityPreviewingKey[0], value);
            if (Registries.qualitiesRegistry.contains(newKey)) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_already_exists").formatted(newKey));
                event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING);
                event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
                return;
            }
            quality.key = newKey;
            QualitiesManager.register(quality);
            QualitiesManager.saveQualityToFile(quality);
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
            event.getPlayer().getPersistentDataContainer().set(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, quality.key.toString());
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("quality id", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_DISPLAY)) {
            event.setCancelled(true);
            var value = event.getMessage();
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            quality.display = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Display", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_TIER)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            quality.tier = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Tier", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_ADD_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            quality.addToItemChance = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Add Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_NO_DROPS_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            quality.noDropChance = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality No Drops Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_DOUBLE_DROPS_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            quality.doubleDropsChance = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Double Drops Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_MAX_DURABILITY_MOD)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            quality.itemMaxDurabilityMod = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Max Durability Mod", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_NO_DURABILITY_LOSS_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            quality.noDurabilityLossChance = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality No Durability Loss Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_EXTRA_DURABILITY_LOSS)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            quality.extraDurabilityLoss = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Extra Durability Loss", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_EXTRA_DURABILITY_LOSS_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            quality.extraDurabilityLossChance = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Extra Durability Loss Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_MODIFIER_NORMAL_AMOUNT)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            var editing = Attribute.valueOf(player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_MODIFIER_EDITING, PersistentDataType.STRING, ""));
            var mod = quality.modifiers.get(editing);
            mod.amount = value;
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Modifier Amount", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_MODIFIER.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_MODIFIER_SLOT_AMOUNT)) {
            event.setCancelled(true);
            var value = 0d;
            try {
                value = Double.parseDouble(event.getMessage());
            } catch (NumberFormatException e) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            var editing = Attribute.valueOf(player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_MODIFIER_EDITING, PersistentDataType.STRING, ""));
            var mod = quality.modifiers.get(editing);
            var slot = EquipmentSlot.valueOf(player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_MODIFIER_EDITING_SLOT, PersistentDataType.STRING, ""));
            mod.slotSpecificAmounts.replace(slot, value);
            Registries.qualitiesRegistry.updateValue(quality.key, quality);
            QualitiesManager.saveQualityToFile(quality);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Modifier Amount", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_MODIFIER.open(player);
                }
            }.runTaskLater(main, 15);
        }

        player.getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
    }
}
