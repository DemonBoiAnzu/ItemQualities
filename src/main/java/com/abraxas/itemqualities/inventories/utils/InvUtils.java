package com.abraxas.itemqualities.inventories.utils;

import org.bukkit.block.banner.Pattern;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.function.Consumer;

import static com.abraxas.itemqualities.utils.Utils.colorize;
import static org.bukkit.DyeColor.*;
import static org.bukkit.Material.*;
import static org.bukkit.block.banner.PatternType.*;
import static org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS;

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
        closeBtn = new ItemStack(LIGHT_GRAY_BANNER);
        var closeBtnMeta = (BannerMeta) closeBtn.getItemMeta();
        closeBtnMeta.addPattern(new Pattern(RED, STRIPE_DOWNRIGHT));
        closeBtnMeta.addPattern(new Pattern(RED, STRIPE_DOWNLEFT));
        closeBtnMeta.addPattern(new Pattern(LIGHT_GRAY, CURLY_BORDER));
        closeBtnMeta.setDisplayName(colorize("&cClose"));
        closeBtnMeta.addItemFlags(HIDE_POTION_EFFECTS); // Wtf mojang
        closeBtn.setItemMeta(closeBtnMeta);

        arrowRightBtn = new ItemStack(LIGHT_GRAY_BANNER);
        var arrowRightBtnMeta = (BannerMeta) arrowRightBtn.getItemMeta();
        arrowRightBtnMeta.addPattern(new Pattern(WHITE, STRIPE_MIDDLE));
        arrowRightBtnMeta.addPattern(new Pattern(WHITE, STRIPE_RIGHT));
        arrowRightBtnMeta.addPattern(new Pattern(LIGHT_GRAY, STRIPE_TOP));
        arrowRightBtnMeta.addPattern(new Pattern(LIGHT_GRAY, STRIPE_BOTTOM));
        arrowRightBtnMeta.addPattern(new Pattern(LIGHT_GRAY, CURLY_BORDER));
        arrowRightBtnMeta.setDisplayName(colorize("&7Next"));
        arrowRightBtnMeta.addItemFlags(HIDE_POTION_EFFECTS); // Wtf mojang
        arrowRightBtn.setItemMeta(arrowRightBtnMeta);

        arrowLeftBtn = new ItemStack(LIGHT_GRAY_BANNER);
        var arrowLeftBtnMeta = (BannerMeta) arrowLeftBtn.getItemMeta();
        arrowLeftBtnMeta.addPattern(new Pattern(WHITE, STRIPE_LEFT));
        arrowLeftBtnMeta.addPattern(new Pattern(WHITE, STRIPE_MIDDLE));
        arrowLeftBtnMeta.addPattern(new Pattern(LIGHT_GRAY, STRIPE_TOP));
        arrowLeftBtnMeta.addPattern(new Pattern(LIGHT_GRAY, STRIPE_BOTTOM));
        arrowLeftBtnMeta.addPattern(new Pattern(LIGHT_GRAY, CURLY_BORDER));
        arrowLeftBtnMeta.setDisplayName(colorize("&7Back"));
        arrowLeftBtnMeta.addItemFlags(HIDE_POTION_EFFECTS); // Wtf mojang
        arrowLeftBtn.setItemMeta(arrowLeftBtnMeta);

        blankItem = new ItemStack(BLACK_STAINED_GLASS_PANE);
        var blankItemMeta = blankItem.getItemMeta();
        blankItemMeta.setDisplayName(" ");
        blankItem.setItemMeta(blankItemMeta);

        blankItemSecondary = new ItemStack(GRAY_STAINED_GLASS_PANE);
        var blankItemSecondaryMeta = blankItemSecondary.getItemMeta();
        blankItemSecondaryMeta.setDisplayName(" ");
        blankItemSecondary.setItemMeta(blankItemSecondaryMeta);
    }
}
