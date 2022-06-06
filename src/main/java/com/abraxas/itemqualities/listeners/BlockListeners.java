package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.QualitiesManager;
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

        var cost = Utils.getReforgeEXPCost(item.getType());
        if (!QualitiesManager.itemHasQuality(item) || cost <= 0) {
            Utils.sendMessageWithPrefix(player, "&cThis item cannot be reforged.");
            return;
        }

        if (player.getGameMode().equals(GameMode.SURVIVAL) && player.getLevel() < cost) {
            Utils.sendMessageWithPrefix(player, "&cYou don't have enough EXP Levels to Reforge this item. &7(You need &e%s&7)".formatted(cost));
            return;
        }

        var newQuality = QualitiesManager.getRandomQuality(QualitiesManager.getQuality(item));
        QualitiesManager.removeQualityFromItem(item);
        QualitiesManager.addQualityToItem(item, newQuality);
        if (player.getGameMode().equals(GameMode.SURVIVAL)) player.setLevel(player.getLevel() - cost);
        player.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1f, 0.8f);
        if (player.getGameMode().equals(GameMode.SURVIVAL))
            Utils.sendMessageWithPrefix(player, "&aSuccessfully reforged your &e%s &ato &e%s &afor &e%s EXP Levels&a.".formatted(Utils.formalizedString(item.getType().toString()),
                    newQuality.display, cost));
        else
            Utils.sendMessageWithPrefix(player, "&aSuccessfully reforged your &e%s &ato &e%s&a!".formatted(Utils.formalizedString(item.getType().toString()),
                    newQuality.display));

        event.setCancelled(true);
    }
}
