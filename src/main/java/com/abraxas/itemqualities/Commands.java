package com.abraxas.itemqualities;

import com.abraxas.itemqualities.api.Registries;
import com.abraxas.itemqualities.utils.UpdateChecker;
import com.abraxas.itemqualities.utils.Utils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

import java.util.concurrent.atomic.AtomicInteger;

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
                            Utils.sendMessageWithPrefix(sender, "&aSuccessfully reloaded!");

                            if (getConfig().newUpdateMessageOnReload) UpdateChecker.sendNewVersionNotif(sender);
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
                            var item = player.getInventory().getItemInMainHand();
                            var qualArgString = (String) args[0];
                            var quality = QualitiesManager.getRandomQuality(QualitiesManager.getQuality(item));
                            var qualArg = qualArgString.split(":");
                            if(qualArgString != "random" && qualArg.length > 1) quality = QualitiesManager.getQualityById(qualArg[1]);

                            if(!QualitiesManager.itemCanHaveQuality(item)){
                                Utils.sendMessageWithPrefix(player, "&cThis item cannot have a Quality applied to it.");
                                return;
                            }
                            if(QualitiesManager.itemHasQuality(item)) QualitiesManager.removeQualityFromItem(item);

                            QualitiesManager.addQualityToItem(item, quality);
                            Utils.sendMessageWithPrefix(player, "&aSuccessfully set your items quality to &e%s&a!".formatted(quality.display));
                        }))
                .withSubcommand(new CommandAPICommand("remove")
                        .withPermission("itemqualities.admin")
                        .executesPlayer((player,args) -> {
                            var item = player.getInventory().getItemInMainHand();

                            if(!QualitiesManager.itemCanHaveQuality(item)){
                                Utils.sendMessageWithPrefix(player, "&cThis item cannot have a Quality applied to it, thus you cannot remove a Quality from it.");
                                return;
                            }
                            if(!QualitiesManager.itemHasQuality(item)){
                                Utils.sendMessageWithPrefix(player, "&cThis item does not have a Quality.");
                                return;
                            }

                            var itemsQuality = QualitiesManager.getQuality(item);

                            QualitiesManager.removeQualityFromItem(item);
                            Utils.sendMessageWithPrefix(player, "&aSuccessfully removed &e%s &afrom your item!".formatted(itemsQuality.display));
                        }))
                .register();
    }
}
