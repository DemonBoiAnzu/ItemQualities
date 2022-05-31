package com.abraxas.itemqualities;

import com.abraxas.itemqualities.api.Registries;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

import java.util.concurrent.atomic.AtomicInteger;

import static com.abraxas.itemqualities.utils.Utils.colorize;
import static com.abraxas.itemqualities.utils.Utils.getConfig;

public class Commands {
    static ItemQualities main = ItemQualities.getInstance();

    public static void register(){
        new CommandAPICommand("qualities")
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission("itemqualities.admin")
                        .executes((sender, args) -> {
                            main.loadConfig();
                            QualitiesManager.loadAndRegister();
                            sender.sendMessage(colorize("%s&aSuccessfully reloaded!".formatted(getConfig().prefix)));
                        }))
                .withSubcommand(new CommandAPICommand("set")
                        .withPermission("itemqualities.admin")
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
                        .executesPlayer((player,args) -> {
                            var qualArgString = (String)args[0];
                            var quality = QualitiesManager.getRandomQuality();
                            var qualArg = qualArgString.split(":");
                            if(qualArgString != "random" && qualArg.length > 1) quality = QualitiesManager.getQualityById(qualArg[1]);
                            var item = player.getInventory().getItemInMainHand();

                            if(!QualitiesManager.itemCanHaveQuality(item)){
                                player.sendMessage(colorize("%s&cThis item cannot have a Quality applied to it.".formatted(getConfig().prefix)));
                                return;
                            }
                            if(QualitiesManager.itemHasQuality(item)) QualitiesManager.removeQualityFromItem(item);

                            QualitiesManager.addQualityToItem(item, quality);
                            player.sendMessage(colorize("%s&aSuccessfully set your items quality to &e%s&a!".formatted(getConfig().prefix, quality.display)));
                        }))
                .withSubcommand(new CommandAPICommand("remove")
                        .withPermission("itemqualities.admin")
                        .executesPlayer((player,args) -> {
                            var item = player.getInventory().getItemInMainHand();

                            if(!QualitiesManager.itemCanHaveQuality(item)){
                                player.sendMessage(colorize("%s&cThis item cannot have a Quality applied to it, thus you cannot remove a Quality from it.".formatted(getConfig().prefix)));
                                return;
                            }
                            if(!QualitiesManager.itemHasQuality(item)){
                                player.sendMessage(colorize("%s&cThis item does not have a Quality.".formatted(getConfig().prefix)));
                                return;
                            }

                            var itemsQuality = QualitiesManager.getQuality(item);

                            QualitiesManager.removeQualityFromItem(item);
                            player.sendMessage(colorize("%s&aSuccessfully removed &e%s &afrom your item!".formatted(getConfig().prefix, itemsQuality.display)));
                        }))
                .register();
    }
}
