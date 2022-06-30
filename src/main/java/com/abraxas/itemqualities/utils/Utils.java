package com.abraxas.itemqualities.utils;

import com.abraxas.itemqualities.Config;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

import static com.abraxas.itemqualities.ItemQualities.getInstance;
import static com.abraxas.itemqualities.utils.Permissions.MANAGE_QUALITIES_PERMISSION;
import static com.abraxas.itemqualities.utils.Permissions.USE_REFORGE_PERMISSION;
import static java.util.regex.Pattern.compile;
import static net.md_5.bungee.api.ChatColor.of;
import static net.md_5.bungee.api.ChatColor.translateAlternateColorCodes;
import static org.apache.commons.lang3.text.WordUtils.capitalize;
import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Material.SHIELD;
import static org.bukkit.Tag.*;
import static org.bukkit.inventory.EquipmentSlot.*;

public class Utils {
    static Random random = new Random();

    public static int getReforgeEXPCost(Material mat) {
        return getConfig().reforgeEXPLevelCosts.getOrDefault(mat, -1);
    }

    public static String colorize(String string) {
        var HEX_PATTERN = compile("<#([A-Fa-f0-9]){6}>");
        var matcher = HEX_PATTERN.matcher(string);
        while (matcher.find()) {
            final var hexColor = of(matcher.group().substring(1, matcher.group().length() - 1));
            final var before = string.substring(0, matcher.start());
            final var after = string.substring(matcher.end());
            string = before + hexColor + after;
            matcher = HEX_PATTERN.matcher(string);
        }
        return translateAlternateColorCodes('&', string);
    }

    public static String formalizedString(String string) {
        return capitalize(string.toLowerCase().replace("_", " "));
    }

    public static void registerEvents(Listener listener) {
        getInstance().getServer().getPluginManager().registerEvents(listener, getInstance());
    }

    public static boolean canUseReforge(Player player) {
        var perm = getPluginManager().getPermission(USE_REFORGE_PERMISSION);
        return player.isOp() || (perm != null && player.hasPermission(perm));
    }

    public static boolean canUseQualityManager(Player player) {
        var perm = getPluginManager().getPermission(MANAGE_QUALITIES_PERMISSION);
        return player.isOp() || (perm != null && player.hasPermission(perm));
    }

    public static boolean chanceOf(int chance) {
        return random.nextInt(100) <= chance;
    }

    public static boolean isOre(Block block) {
        return DIAMOND_ORES.isTagged(block.getType()) ||
                LAPIS_ORES.isTagged(block.getType()) ||
                COPPER_ORES.isTagged(block.getType()) ||
                GOLD_ORES.isTagged(block.getType()) ||
                IRON_ORES.isTagged(block.getType()) ||
                EMERALD_ORES.isTagged(block.getType()) ||
                REDSTONE_ORES.isTagged(block.getType()) ||
                COAL_ORES.isTagged(block.getType());
    }

    public static EquipmentSlot getItemsMainSlot(ItemStack itemStack) {
        return (isHelmet(itemStack)) ? HEAD :
                (isChest(itemStack)) ? CHEST :
                        (isLegs(itemStack)) ? LEGS :
                                (isBoots(itemStack)) ? FEET :
                                        (isOffhandItem(itemStack)) ? OFF_HAND : HAND;
    }

    public static boolean isMiningTool(ItemStack itemStack) {
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

    public static boolean isHelmet(ItemStack itemStack) {
        return itemStack.getType().toString().contains("HELMET");
    }

    public static boolean isChest(ItemStack itemStack) {
        return itemStack.getType().toString().contains("CHESTPLATE") ||
                itemStack.getType().toString().contains("TUNIC") ||
                itemStack.getType().toString().contains("ELYTRA");
    }

    public static boolean isLegs(ItemStack itemStack) {
        return itemStack.getType().toString().contains("LEGGINGS");
    }

    public static boolean isBoots(ItemStack itemStack) {
        return itemStack.getType().toString().contains("BOOTS");
    }

    public static boolean isOffhandItem(ItemStack itemStack) {
        return itemStack.getType().equals(SHIELD);
    }

    public static boolean isMeleeWeapon(ItemStack itemStack) {
        return itemStack.getType().toString().contains("SWORD") ||
                itemStack.getType().toString().contains("_AXE") ||
                itemStack.getType().toString().contains("TRIDENT");
    }

    public static boolean isProjectileLauncher(ItemStack itemStack) {
        return itemStack.getType().toString().contains("BOW") ||
                itemStack.getType().toString().contains("TRIDENT");
    }

    public static Random getRandom() {
        return random;
    }

    public static void log(String message) {
        getInstance().getLogger().info(colorize(message));
    }

    public static void sendMessageWithoutPrefix(CommandSender receiver, String message) {
        sendMessage(receiver, message, false);
    }

    public static void sendMessageWithPrefix(CommandSender receiver, String message) {
        sendMessage(receiver, message, true);
    }

    static void sendMessage(CommandSender receiver, String message, boolean withPrefix) {
        receiver.sendMessage(colorize("%s%s".formatted((withPrefix) ? getConfig().prefix : "", message)));
    }

    public static Config getConfig() {
        return getInstance().getConfiguration();
    }

    public static void runTask(Runnable runnable, long delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(getInstance(), delay);
    }
}
