package com.abraxas.itemqualities.utils;

import com.abraxas.itemqualities.ItemQualities;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Consumer;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import static com.abraxas.itemqualities.utils.Utils.*;
import static java.lang.Integer.parseInt;
import static net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;

public class UpdateChecker {
    private static final String resourceLink = "https://www.spigotmc.org/resources/item-qualities.102350/";
    private static final int resourceId = 102350;
    static ItemQualities main = ItemQualities.getInstance();
    private static boolean outdated;
    private static String newVersion;

    public static void checkForNewVersion() {
        log(colorize(main.getTranslation("message.plugin.checking_for_update")));
        getVersion(version -> {
            var latestVersion = parseInt(version.replaceAll("\\.", ""));
            var currentVersion = parseInt(main.getDescription().getVersion().replaceAll("\\.", ""));

            outdated = false;
            if (currentVersion == latestVersion)
                log(colorize(main.getTranslation("message.plugin.on_latest_version").formatted(main.getDescription().getVersion())));
            else if (currentVersion > latestVersion)
                log(colorize(main.getTranslation("message.plugin.on_indev_version").formatted(main.getDescription().getVersion())));
            else {
                outdated = true;
                newVersion = version;
                log(colorize("%s (&e%s - %s&7)".formatted(main.getTranslation("message.plugin.new_version_available_download"), version, resourceLink)));
            }
        });
    }

    public static void sendNewVersionNotif(CommandSender sender) {
        if (!sender.isOp() || !sender.hasPermission("itemqualities.admin") || !outdated) return;

        var msg = new TextComponent(colorize("%s%s ".formatted(getConfig().prefix, main.getTranslation("message.plugin.new_version_available_download"))));
        var link = new TextComponent(colorize("&7&o(&e&o%s&7&o)".formatted(newVersion)));
        link.setClickEvent(new ClickEvent(OPEN_URL, resourceLink));
        msg.addExtra(link);
        sender.spigot().sendMessage(msg);
    }

    static void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(ItemQualities.getInstance(), () -> {
            try (var inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=%d".formatted(resourceId)).openStream(); var scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) consumer.accept(scanner.next());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
