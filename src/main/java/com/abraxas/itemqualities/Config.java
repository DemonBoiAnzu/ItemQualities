package com.abraxas.itemqualities;

import com.abraxas.itemqualities.api.APIUtils;

public class Config {
    public boolean debugMode = true;
    public String prefix = "&bItemQualities &7» ";
    public String itemQualityDisplayFormat = "{QUALITY} &r{ITEM}";
    public boolean exampleItemQualitiesEnabled = true;
    public boolean displayQualityInLore = false;
    public boolean newUpdateMessageOnReload = false;
    public boolean newUpdateMessageOnJoin = false;

    public static Config deserialize(String json) {
        return APIUtils.getGson().fromJson(json, Config.class);
    }

    public static String serialize(Config item) {
        return APIUtils.getGson().toJson(item, Config.class);
    }
}
