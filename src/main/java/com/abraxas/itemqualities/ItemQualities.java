package com.abraxas.itemqualities;

import com.abraxas.itemqualities.listeners.ItemListeners;
import com.abraxas.itemqualities.utils.Utils;
import com.google.gson.JsonParser;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.abraxas.itemqualities.utils.Utils.log;

public final class ItemQualities extends JavaPlugin {
    private static ItemQualities instance;

    Config config;
    String configPath = "%s/config.json".formatted(getDataFolder());

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIConfig().silentLogs(true));
    }

    @Override
    public void onEnable() {
        instance = this;
        CommandAPI.onEnable(instance);

        loadConfig();
        QualitiesManager.loadAndRegister();

        Commands.register();

        Utils.registerEvents(new ItemListeners());

        log("Successfully enabled.");
    }

    @Override
    public void onDisable() {
        log("Successfully disabled.");
    }

    void loadConfig() {
        try {
            if (!Files.exists(Path.of(getConfigPath()))) {
                saveResource("config.json", true);
                loadConfig();
                return;
            }
            var file = new File(configPath);
            var contents = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            var json = JsonParser.parseString(contents);

            config = Config.deserialize(String.valueOf(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config getConfiguration() {
        return config;
    }

    public String getConfigPath() {
        return configPath;
    }

    public static ItemQualities getInstance() {
        return instance;
    }
}
