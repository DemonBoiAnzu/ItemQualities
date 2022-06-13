package com.abraxas.itemqualities;

import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.utils.Permissions;
import com.abraxas.itemqualities.utils.UpdateChecker;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.abraxas.itemqualities.utils.Utils.*;

public class Commands {
    static ItemQualities main = ItemQualities.getInstance();

    public static void register() {
        var subCommands = new ArrayList<CommandAPICommand>() {{
            add(new CommandAPICommand("reload")
                    .withPermission(Permissions.RELOAD_PERMISSION)
                    .withShortDescription("Reload the plugins config and item qualities.")
                    .executes((sender, args) -> {
                        main.loadConfig();
                        QualitiesManager.loadAndRegister();
                        sendMessageWithPrefix(sender, main.getTranslation("message.commands.reloaded"));

                        if (getConfig().newUpdateMessageOnReload) UpdateChecker.sendNewVersionNotif(sender);
                    }));
            add(new CommandAPICommand("resetconfig")
                    .withPermission(Permissions.RESET_CONFIG_PERMISSION)
                    .withShortDescription("Reset the config to the plugins default and reload the plugin.")
                    .executes((sender, args) -> {
                        main.resetConfig();
                        QualitiesManager.loadAndRegister();
                        sendMessageWithPrefix(sender, main.getTranslation("message.commands.reloaded_default_config"));
                    }));
            add(new CommandAPICommand("setitemquality")
                    .withPermission(Permissions.SET_ITEMS_QUALITY_PERMISSION)
                    .withShortDescription("Set your held items quality.")
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
                        sendMessageWithPrefix(player, main.getTranslation("message.commands.items_quality_set").formatted(quality.display));
                    }));
            add(new CommandAPICommand("removeitemquality")
                    .withPermission(Permissions.REMOVE_ITEMS_QUALITY_PERMISSION)
                    .withShortDescription("Remove the quality from your held item.")
                    .executesPlayer((player, args) -> {
                        var item = player.getInventory().getItemInMainHand();

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
                        sendMessageWithPrefix(player, main.getTranslation("message.commands.quality_removed").formatted(itemsQuality.display));
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
