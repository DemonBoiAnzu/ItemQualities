package com.abraxas.itemqualities;

import com.abraxas.itemqualities.api.DurabilityManager;
import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.inventories.Inventories;
import com.abraxas.itemqualities.utils.Permissions;
import com.abraxas.itemqualities.utils.UpdateChecker;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.abraxas.itemqualities.utils.Utils.*;

public class Commands {
    static ItemQualities main = ItemQualities.getInstance();

    public static void register() {
        var subCommands = new ArrayList<CommandAPICommand>() {{
            add(new CommandAPICommand("reload")
                    .withPermission(Permissions.RELOAD_PERMISSION)
                    .executes((sender, args) -> {
                        main.loadConfig();
                        QualitiesManager.loadAndRegister();
                        sendMessageWithPrefix(sender, main.getTranslation("message.commands.reloaded"));

                        if (getConfig().newUpdateMessageOnReload) UpdateChecker.sendNewVersionNotif(sender);
                    }));
            add(new CommandAPICommand("resetconfig")
                    .withPermission(Permissions.RESET_CONFIG_PERMISSION)
                    .executes((sender, args) -> {
                        main.resetConfig();
                        QualitiesManager.loadAndRegister();
                        sendMessageWithPrefix(sender, main.getTranslation("message.commands.reloaded_default_config"));
                    }));
            add(new CommandAPICommand("repairitem")
                    .withPermission(Permissions.REPAIR_ITEM_PERMISSION)
                    .executesPlayer((player, args) -> {
                        var item = player.getInventory().getItemInMainHand();
                        if (item.getType().equals(Material.AIR)) {
                            sendMessageWithPrefix(player, main.getTranslation("message.commands.must_hold_item"));
                            return;
                        }
                        var itemMeta = item.getItemMeta();
                        if (!(itemMeta instanceof Damageable)) {
                            sendMessageWithPrefix(player, main.getTranslation("message.commands.item_cant_be_repaired"));
                            return;
                        }
                        String itemName = new TranslatableComponent("item.minecraft.%s".formatted(item.getType().toString().toLowerCase())).toPlainText();
                        DurabilityManager.repairItem(item);
                        sendMessageWithPrefix(player, main.getTranslation("message.commands.item_repaired").formatted(itemName));
                    }));
            add(new CommandAPICommand("setitemquality")
                    .withPermission(Permissions.SET_ITEMS_QUALITY_PERMISSION)
                    .withArguments(new GreedyStringArgument("quality")
                            .replaceSuggestions(ArgumentSuggestions.strings(suggestionInfo -> {
                                String[] qualityNamespaces = new String[Registries.qualitiesRegistry.getRegistry().size() + 1];
                                AtomicInteger integer = new AtomicInteger();
                                Registries.qualitiesRegistry.getRegistry().keySet().forEach(k -> {
                                    qualityNamespaces[integer.getAndIncrement()] = "%s:%s".formatted(k.getNamespace(), k.getKey());
                                });
                                qualityNamespaces[Registries.qualitiesRegistry.getRegistry().size()] = "random";
                                return qualityNamespaces;
                            })))
                    .executesPlayer((player, args) -> {
                        var item = player.getInventory().getItemInMainHand();
                        if (item.getType().equals(Material.AIR)) {
                            sendMessageWithPrefix(player, main.getTranslation("message.commands.must_hold_item"));
                            return;
                        }
                        var qualArgString = (String) args[0];
                        var quality = QualitiesManager.getRandomQuality(QualitiesManager.getQuality(item));
                        var qualArg = qualArgString.split(":");
                        if (qualArgString != "random" && qualArg.length > 1)
                            quality = QualitiesManager.getQualityById(qualArg[1]);

                        if (!QualitiesManager.itemCanHaveQuality(item)) {
                            sendMessageWithPrefix(player, main.getTranslation("message.commands.item_cant_have_quality"));
                            return;
                        }
                        if (QualitiesManager.itemHasQuality(item)) QualitiesManager.removeQualityFromItem(item);

                        QualitiesManager.addQualityToItem(item, quality);
                        DurabilityManager.damageItem(player, item, 0);
                        sendMessageWithPrefix(player, main.getTranslation("message.commands.items_quality_set").formatted(quality.display));
                    }));
            add(new CommandAPICommand("removeitemquality")
                    .withPermission(Permissions.REMOVE_ITEMS_QUALITY_PERMISSION)
                    .executesPlayer((player, args) -> {
                        var item = player.getInventory().getItemInMainHand();
                        if (item.getType().equals(Material.AIR)) {
                            sendMessageWithPrefix(player, main.getTranslation("message.commands.must_hold_item"));
                            return;
                        }
                        if (!QualitiesManager.itemCanHaveQuality(item)) {
                            sendMessageWithPrefix(player, main.getTranslation("message.commands.item_cant_remove_quality"));
                            return;
                        }
                        if (!QualitiesManager.itemHasQuality(item)) {
                            sendMessageWithPrefix(player, main.getTranslation("message.commands.item_has_no_quality"));
                            return;
                        }

                        var itemsQuality = QualitiesManager.getQuality(item);

                        QualitiesManager.removeQualityFromItem(item);
                        DurabilityManager.damageItem(player, item, 0);
                        sendMessageWithPrefix(player, main.getTranslation("message.commands.quality_removed").formatted(itemsQuality.display));
                    }));
            add(new CommandAPICommand("managequalities")
                    .withPermission(Permissions.MANAGE_QUALITIES_PERMISSION)
                    .executesPlayer((player, args) -> {
                        if (Registries.qualitiesRegistry.getRegistry().size() < 1) {
                            sendMessageWithPrefix(player, main.getTranslation("message.plugin.no_qualities_registered"));
                            return;
                        }
                        Inventories.QUALITY_MANAGER_INVENTORY.open(player, 0);
                    }));
        }};

        var mainCommand = new CommandAPICommand("qualities")
                .withPermission(Permissions.ADMIN_PERMISSION)
                .executes((sender, args) -> {
                    var usableCommands = subCommands.stream().filter(sc -> sender.hasPermission(sc.getPermission().toString())).toList();
                    if (usableCommands.size() > 0) {
                        sendMessageWithoutPrefix(sender, main.getTranslation("message.plugin.help.info"));
                        usableCommands.forEach(sc -> {
                            sendMessageWithoutPrefix(sender, main.getTranslation("message.plugin.help.command.%s".formatted(sc.getName())));
                        });
                    } else sendMessageWithPrefix(sender, main.getTranslation("message.plugin.help.no_usable_commands"));
                });

        subCommands.forEach(mainCommand::withSubcommand);

        mainCommand.register();
    }
}
