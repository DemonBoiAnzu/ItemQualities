package com.abraxas.itemqualities.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class APIUtils {
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Gson getGson() {
        return gson;
    }
}
