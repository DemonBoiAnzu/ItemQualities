package com.abraxas.itemqualities.listeners;

import com.abraxas.itemqualities.ItemQualities;
import com.abraxas.itemqualities.utils.UpdateChecker;
import com.abraxas.itemqualities.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ServerListeners implements Listener {
    ItemQualities main = ItemQualities.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (Utils.getConfig().newUpdateMessageOnJoin)
            UpdateChecker.sendNewVersionNotif(event.getPlayer());
    }
}
