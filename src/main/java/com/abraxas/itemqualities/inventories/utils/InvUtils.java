package com.abraxas.itemqualities.inventories.utils;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.function.Consumer;

import static com.abraxas.itemqualities.utils.Utils.colorize;

public class InvUtils {
    public static final Consumer<InventoryClickEvent> PREVENT_PICKUP = inventoryClickEvent ->
            inventoryClickEvent.setCancelled(true);
    public static final Consumer<InventoryClickEvent> CLOSE_GUI = inventoryClickEvent -> {
        inventoryClickEvent.setCancelled(true);
        inventoryClickEvent.getWhoClicked().closeInventory();
    };

    public static final ItemStack closeBtn;
    public static final ItemStack arrowRightBtn;
    public static final ItemStack arrowLeftBtn;
    public static final ItemStack blankItem;
    public static final ItemStack blankItemSecondary;

    static {
        closeBtn = new ItemStack(Material.LIGHT_GRAY_BANNER);
        var closeBtnMeta = (BannerMeta) closeBtn.getItemMeta();
        closeBtnMeta.addPattern(new Pattern(DyeColor.RED, PatternType.STRIPE_DOWNRIGHT));
        closeBtnMeta.addPattern(new Pattern(DyeColor.RED, PatternType.STRIPE_DOWNLEFT));
        closeBtnMeta.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.CURLY_BORDER));
        closeBtnMeta.setDisplayName(colorize("&cClose"));
        closeBtnMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); // Wtf mojang
        closeBtn.setItemMeta(closeBtnMeta);

        arrowRightBtn = new ItemStack(Material.LIGHT_GRAY_BANNER);
        var arrowRightBtnMeta = (BannerMeta) arrowRightBtn.getItemMeta();
        arrowRightBtnMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_MIDDLE));
        arrowRightBtnMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_RIGHT));
        arrowRightBtnMeta.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_TOP));
        arrowRightBtnMeta.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM));
        arrowRightBtnMeta.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.CURLY_BORDER));
        arrowRightBtnMeta.setDisplayName(colorize("&7Next"));
        arrowRightBtnMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); // Wtf mojang
        arrowRightBtn.setItemMeta(arrowRightBtnMeta);

        arrowLeftBtn = new ItemStack(Material.LIGHT_GRAY_BANNER);
        var arrowLeftBtnMeta = (BannerMeta) arrowLeftBtn.getItemMeta();
        arrowLeftBtnMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_LEFT));
        arrowLeftBtnMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_MIDDLE));
        arrowLeftBtnMeta.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_TOP));
        arrowLeftBtnMeta.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM));
        arrowLeftBtnMeta.addPattern(new Pattern(DyeColor.LIGHT_GRAY, PatternType.CURLY_BORDER));
        arrowLeftBtnMeta.setDisplayName(colorize("&7Back"));
        arrowLeftBtnMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); // Wtf mojang
        arrowLeftBtn.setItemMeta(arrowLeftBtnMeta);

        blankItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        var blankItemMeta = blankItem.getItemMeta();
        blankItemMeta.setDisplayName(" ");
        blankItem.setItemMeta(blankItemMeta);

        blankItemSecondary = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var blankItemSecondaryMeta = blankItemSecondary.getItemMeta();
        blankItemSecondaryMeta.setDisplayName(" ");
        blankItemSecondary.setItemMeta(blankItemSecondaryMeta);
    }
}
