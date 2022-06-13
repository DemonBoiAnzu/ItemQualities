package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
import com.abraxas.itemqualities.utils.Permissions;
import com.abraxas.itemqualities.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import static com.abraxas.itemqualities.utils.Utils.getConfig;
import static com.abraxas.itemqualities.utils.Utils.sendMessageWithPrefix;

public class BlockListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void reforgeItem(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var item = player.getInventory().getItemInMainHand();
        var block = event.getClickedBlock();
        var action = event.getAction();
        var hand = event.getHand();
        if (action != Action.RIGHT_CLICK_BLOCK ||
                hand != EquipmentSlot.HAND ||
                block == null ||
                !player.isSneaking()) return;

        if (!block.getType().equals(Material.ANVIL)) return;
        if (!getConfig().reforgeStationEnabled) return;

        if (!player.hasPermission(Permissions.USE_REFORGE_PERMISSION)) {
            sendMessageWithPrefix(player, main.getTranslation("message.plugin.no_permission").formatted(Permissions.USE_REFORGE_PERMISSION));
            return;
        }

        var cost = Utils.getReforgeEXPCost(item.getType());
        if (cost <= 0) {
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.item_cant_be_reforged"));
            return;
        }

        if (player.getGameMode().equals(GameMode.SURVIVAL) && player.getLevel() < cost) {
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.cant_afford").formatted(cost));
            return;
        }

        var newQuality = QualitiesManager.getRandomQuality(QualitiesManager.getQuality(item));
        QualitiesManager.removeQualityFromItem(item);
        QualitiesManager.addQualityToItem(item, newQuality);
        if (player.getGameMode().equals(GameMode.SURVIVAL)) player.setLevel(player.getLevel() - cost);
        player.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1f, 0.8f);
        if (player.getGameMode().equals(GameMode.SURVIVAL))
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.success_survival").formatted(Utils.formalizedString(item.getType().toString()),
                    newQuality.display, cost));
        else
            sendMessageWithPrefix(player, main.getTranslation("message.reforge.success_creative").formatted(Utils.formalizedString(item.getType().toString()),
                    newQuality.display));

        event.setCancelled(true);
    }
}
