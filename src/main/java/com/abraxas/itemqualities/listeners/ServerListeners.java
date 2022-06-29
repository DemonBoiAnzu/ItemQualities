package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.api.quality.ItemQuality;
import com.abraxas.itemqualities.utils.UpdateChecker;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import static com.abraxas.itemqualities.QualitiesManager.*;
import static com.abraxas.itemqualities.api.Keys.*;
import static com.abraxas.itemqualities.api.Registries.qualitiesRegistry;
import static com.abraxas.itemqualities.inventories.Inventories.QUALITY_EDIT_INVENTORY;
import static com.abraxas.itemqualities.inventories.Inventories.QUALITY_EDIT_MODIFIER;
import static com.abraxas.itemqualities.utils.QualityChatValues.*;
import static com.abraxas.itemqualities.utils.Utils.*;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.bukkit.persistence.PersistentDataType.STRING;

public class ServerListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (getConfig().newUpdateMessageOnJoin)
            UpdateChecker.sendNewVersionNotif(event.getPlayer());
        event.getPlayer().getPersistentDataContainer().remove(PLAYER_QUALITY_EDITING_OR_PREVIEWING);
        event.getPlayer().getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        var player = event.getPlayer();
        if (!player.getPersistentDataContainer().has(PLAYER_TYPING_VALUE_KEY, STRING)) return;
        var valueId = player.getPersistentDataContainer().get(PLAYER_TYPING_VALUE_KEY, STRING);

        if (!canUseQualityManager(player)) {
            event.getPlayer().getPersistentDataContainer().remove(PLAYER_QUALITY_EDITING_OR_PREVIEWING);
            event.getPlayer().getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
            return;
        }

        if (event.getMessage().equals("cancel") || event.getMessage().equals("Abbrechen") ||
                event.getMessage().equals("cancelar") || event.getMessage().equals("annuler")) {
            event.setCancelled(true);
            event.getPlayer().getPersistentDataContainer().remove(PLAYER_QUALITY_EDITING_OR_PREVIEWING);
            event.getPlayer().getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.canceled"));
            return;
        }

        if (valueId.equals(NEW_QUALITY_ID)) {
            event.setCancelled(true);
            var value = event.getMessage().toLowerCase().replace(" ", "_");
            var valSplit = value.split(":");
            var namespaceOwner = (valSplit.length < 2) ? "itemqualities" : valSplit[0];
            if (valSplit.length > 1) value = valSplit[1];
            var newQuality = new ItemQuality(new NamespacedKey(namespaceOwner, value), "&r%s".formatted(event.getMessage()), 0, 0);
            if (qualitiesRegistry.contains(newQuality.key)) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_already_exists").formatted(newQuality.key));
                event.getPlayer().getPersistentDataContainer().remove(PLAYER_QUALITY_EDITING_OR_PREVIEWING);
                event.getPlayer().getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
                return;
            }
            register(newQuality);
            saveQualityToFile(newQuality);
            event.getPlayer().getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
            event.getPlayer().getPersistentDataContainer().set(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, newQuality.key.toString());
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("quality id", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_ID)) {
            event.setCancelled(true);
            var value = event.getMessage().toLowerCase().replace(" ", "_");
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            deleteQuality(quality);
            var valSplit = value.split(":");
            var namespaceOwner = (valSplit.length < 2) ? rawQualityPreviewingKey[0] : valSplit[0];
            if (valSplit.length > 1) value = valSplit[1];
            var newKey = new NamespacedKey(namespaceOwner, value);
            if (qualitiesRegistry.contains(newKey)) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_already_exists").formatted(newKey));
                event.getPlayer().getPersistentDataContainer().remove(PLAYER_QUALITY_EDITING_OR_PREVIEWING);
                event.getPlayer().getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
                return;
            }
            quality.key = newKey;
            register(quality);
            saveQualityToFile(quality);
            event.getPlayer().getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
            event.getPlayer().getPersistentDataContainer().set(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, quality.key.toString());
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("quality id", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_DISPLAY)) {
            event.setCancelled(true);
            var value = event.getMessage();
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            quality.display = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Display", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_TIER)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            if (value < lowestTier) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer between %s and %s".formatted(lowestTier, highestTier)));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            quality.tier = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Tier", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_ADD_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            quality.addToItemChance = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Add Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_NO_DROPS_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            quality.noDropChance = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality No Drops Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_DOUBLE_DROPS_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            quality.doubleDropsChance = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Double Drops Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_MAX_DURABILITY_MOD)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            quality.itemMaxDurabilityMod = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Max Durability Mod", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_NO_DURABILITY_LOSS_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            quality.noDurabilityLossChance = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality No Durability Loss Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_EXTRA_DURABILITY_LOSS)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            quality.extraDurabilityLoss = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Extra Durability Loss", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_EXTRA_DURABILITY_LOSS_CHANCE)) {
            event.setCancelled(true);
            var value = 0;
            try {
                value = parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Integer"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            quality.extraDurabilityLossChance = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Quality Extra Durability Loss Chance", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_MODIFIER_NORMAL_AMOUNT)) {
            event.setCancelled(true);
            var value = 0d;
            try {
                value = parseDouble(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Double"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            var editing = Attribute.valueOf(player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_MODIFIER_EDITING, STRING, ""));
            var mod = quality.modifiers.get(editing);
            mod.amount = value;
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Modifier Amount", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_MODIFIER.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(UPDATE_QUALITY_MODIFIER_SLOT_AMOUNT)) {
            event.setCancelled(true);
            var value = 0d;
            try {
                value = parseDouble(event.getMessage());
            } catch (NumberFormatException e) {
                sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.unexpected_value_type").formatted("Double"));
                return;
            }
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_EDITING_OR_PREVIEWING, STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = getQualityById(qualityNamespace);
            var editing = Attribute.valueOf(player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_MODIFIER_EDITING, STRING, ""));
            var mod = quality.modifiers.get(editing);
            var slot = EquipmentSlot.valueOf(player.getPersistentDataContainer().getOrDefault(PLAYER_QUALITY_MODIFIER_EDITING_SLOT, STRING, ""));
            mod.slotSpecificAmounts.replace(slot, value);
            qualitiesRegistry.updateValue(quality.key, quality);
            saveQualityToFile(quality);
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_creation.value_updated").formatted("Modifier Amount", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    QUALITY_EDIT_MODIFIER.open(player);
                }
            }.runTaskLater(main, 15);
        }

        player.getPersistentDataContainer().remove(PLAYER_TYPING_VALUE_KEY);
    }
}
