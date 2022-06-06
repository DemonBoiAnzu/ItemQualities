package com.abraxas.itemqualities.utils;

import com.abraxas.itemqualities.ItemQualities;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Consumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class UpdateChecker {
    private static final String resourceLink = "https://www.spigotmc.org/resources/item-qualities.102350/";
    private static final int resourceId = 102350;
    static ItemQualities main = ItemQualities.getInstance();
    private static boolean outdated;
    private static String newVersion;

    public static void checkForNewVersion() {
        Utils.log(Utils.colorize(main.getTranslation("message.plugin.checking_for_update")));
        getVersion(version -> {
            var latestVersion = Integer.parseInt(version.replaceAll("\\.", ""));
            var currentVersion = Integer.parseInt(main.getDescription().getVersion().replaceAll("\\.", ""));

            outdated = false;
            if (currentVersion == latestVersion)
                Utils.log(Utils.colorize(main.getTranslation("message.plugin.on_latest_version").formatted(main.getDescription().getVersion())));
            else if (currentVersion > latestVersion)
                Utils.log(Utils.colorize(main.getTranslation("message.plugin.on_indev_version").formatted(main.getDescription().getVersion())));
            else {
                outdated = true;
                newVersion = version;
                Utils.log(Utils.colorize("%s (&e%s - %s&7)".formatted(main.getTranslation("message.plugin.new_version_available_download"), version, resourceLink)));
            }
        });
    }

    public static void sendNewVersionNotif(CommandSender sender) {
        if (!sender.isOp() || !sender.hasPermission("itemqualities.admin") || !outdated) return;

        TextComponent msg = new TextComponent(Utils.colorize("%s%s ".formatted(Utils.getConfig().prefix, main.getTranslation("message.plugin.new_version_available_download"))));
        TextComponent link = new TextComponent(Utils.colorize("&7&o(&e&o%s&7&o)".formatted(newVersion)));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, resourceLink));
        msg.addExtra(link);
        sender.spigot().sendMessage(msg);
    }

    static void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(ItemQualities.getInstance(), () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=%d".formatted(resourceId)).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) consumer.accept(scanner.next());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
