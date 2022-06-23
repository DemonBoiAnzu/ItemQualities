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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (Utils.getConfig().newUpdateMessageOnJoin)
            UpdateChecker.sendNewVersionNotif(event.getPlayer());
        event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY);
        event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        var player = event.getPlayer();
        if (!player.getPersistentDataContainer().has(Keys.PLAYER_TYPING_VALUE_KEY, PersistentDataType.STRING)) return;
        var valueId = player.getPersistentDataContainer().get(Keys.PLAYER_TYPING_VALUE_KEY, PersistentDataType.STRING);

        if (!Utils.canUseQualityManager(player)) {
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY);
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
            return;
        }

        if (event.getMessage().equals("cancel")) {
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY);
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.qualitycreation.canceled"));
            return;
        }

        if (valueId.equals(QualityChatValues.NEW_QUALITY_ID)) {
            event.setCancelled(true);
            var value = event.getMessage().toLowerCase().replace(" ", "_");
            var newQuality = new ItemQuality(new NamespacedKey(main, value), "&r%s".formatted(event.getMessage()), 0, 0);
            if (Registries.qualitiesRegistry.contains(newQuality.key)) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_already_exists").formatted(newQuality.key));
                event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY);
                event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
                return;
            }
            QualitiesManager.register(newQuality);
            QualitiesManager.saveQualityToFile(newQuality);
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
            event.getPlayer().getPersistentDataContainer().set(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY, PersistentDataType.STRING, newQuality.key.toString());
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.qualitycreation.value_updated").formatted("quality id", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        } else if (valueId.equals(QualityChatValues.UPDATE_QUALITY_ID)) {
            event.setCancelled(true);
            var value = event.getMessage().toLowerCase().replace(" ", "_");
            var rawQualityPreviewingKey = player.getPersistentDataContainer().getOrDefault(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY, PersistentDataType.STRING, "").split(":");
            var qualityNamespace = new NamespacedKey(rawQualityPreviewingKey[0], rawQualityPreviewingKey[1]);
            var quality = QualitiesManager.getQualityById(qualityNamespace);
            QualitiesManager.deleteQuality(quality);
            var newKey = new NamespacedKey(rawQualityPreviewingKey[0], value);
            if (Registries.qualitiesRegistry.contains(newKey)) {
                Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.quality_already_exists").formatted(newKey));
                event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY);
                event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
                return;
            }
            quality.key = newKey;
            QualitiesManager.register(quality);
            QualitiesManager.saveQualityToFile(quality);
            event.getPlayer().getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
            event.getPlayer().getPersistentDataContainer().set(Keys.PLAYER_QUALITY_EDITING_OR_PREVIEWING_KEY, PersistentDataType.STRING, quality.key.toString());
            Utils.sendMessageWithPrefix(player, main.getTranslation("message.plugin.qualitycreation.value_updated").formatted("quality id", value));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Inventories.QUALITY_EDIT_INVENTORY.open(player);
                }
            }.runTaskLater(main, 15);
        }

        player.getPersistentDataContainer().remove(Keys.PLAYER_TYPING_VALUE_KEY);
    }
}
