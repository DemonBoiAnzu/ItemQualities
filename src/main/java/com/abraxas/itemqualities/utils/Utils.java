package com.abraxas.itemqualities.utils;

import com.abraxas.itemqualities.Config;
import com.abraxas.itemqualities.ItemQualities;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    static Random random = new Random();

    public static String colorize(String string) {
        Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]){6}>");
        Matcher matcher = HEX_PATTERN.matcher(string);
        while (matcher.find()) {
            final net.md_5.bungee.api.ChatColor hexColor = net.md_5.bungee.api.ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = string.substring(0, matcher.start());
            final String after = string.substring(matcher.end());
            string = before + hexColor + after;
            matcher = HEX_PATTERN.matcher(string);
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String formalizedString(String string) {
        return WordUtils.capitalize(string.toLowerCase().replace("_", " "));
    }

    public static void registerEvents(Listener listener) {
        ItemQualities.getInstance().getServer().getPluginManager().registerEvents(listener, ItemQualities.getInstance());
    }

    public static boolean chanceOf(int chance) {
        return random.nextInt(100) <= chance;
    }

    public static boolean isMiningTool(ItemStack itemStack){
        return itemStack.getType().toString().contains("PICKAXE") ||
                itemStack.getType().toString().contains("SHOVEL") ||
                itemStack.getType().toString().contains("HOE") ||
                itemStack.getType().toString().contains("AXE");
    }

    public static boolean isArmor(ItemStack itemStack) {
        return itemStack.getType().toString().contains("HELMET") ||
                itemStack.getType().toString().contains("CHESTPLATE") ||
                itemStack.getType().toString().contains("TUNIC") ||
                itemStack.getType().toString().contains("ELYTRA") ||
                itemStack.getType().toString().contains("LEGGINGS") ||
                itemStack.getType().toString().contains("BOOTS");
    }

    public static boolean isMeleeWeapon(ItemStack itemStack) {
        return itemStack.getType().toString().contains("SWORD") ||
                itemStack.getType().toString().contains("_AXE");
    }

    public static boolean isProjectileLauncher(ItemStack itemStack) {
        return itemStack.getType().toString().contains("BOW") ||
                itemStack.getType().toString().contains("TRIDENT");
    }

    public static Random getRandom() {
        return random;
    }

    public static void log(String message) {
        ItemQualities.getInstance().getLogger().info(colorize(message));
    }

    public static Config getConfig() {
        return ItemQualities.getInstance().getConfiguration();
    }
}
