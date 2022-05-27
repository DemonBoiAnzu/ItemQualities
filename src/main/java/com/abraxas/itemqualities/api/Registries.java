package com.abraxas.itemqualities.api;

import com.abraxas.itemqualities.api.quality.ItemQuality;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

/**
 * The Registries for Custom Elements.
 */
public class Registries {
    public static Registry<ItemQuality> qualitiesRegistry = new Registry<>();

    /**
     * A Registry object.
     *
     * @param <T> The type of the Registry.
     */
    public static class Registry<T> {
        private final Map<NamespacedKey, T> registry = new HashMap<>();

        /**
         * Register the Type into the Registry.
         *
         * @param key   The key for the newly registered type. (NamespacedKey)
         * @param value The type to register. (Type)
         * @return The registered Type.
         */
        public T register(NamespacedKey key, T value) {
            registry.put(key, value);
            return value;
        }

        /**
         * Unregister an Object from the Registry.
         *
         * @param key The key of the Object. (NamespacedKey)
         */
        public void unregister(NamespacedKey key) {
            registry.remove(key);
        }

        /**
         * Get the registered Type by Key.
         *
         * @param key The key of the Object. (NamespacedKey)
         * @return The registered Type. (Null if none found)
         */
        public T get(NamespacedKey key) {
            return registry.get(key);
        }

        /**
         * Get the registered Type by Key.
         *
         * @param key The key of the Object - Excluding the Namespace. (String)
         * @return The registered Type. (Null if none found)
         */
        public T get(String key) {
            for (var it : registry.entrySet()) {
                if (key.contains(":")) {
                    var keySplit = key.split(":");

                    var nameSpace = keySplit[0];
                    var itKey = keySplit[1];
                    if (it.getKey().getNamespace().equals(nameSpace) && it.getKey().getKey().equals(itKey))
                        return it.getValue();
                }
                if (it.getKey().getKey().equals(key)) return it.getValue();
            }
            return null;
        }

        /**
         * Get the entire registry.
         *
         * @return The Registry. (Map, NamespacedKey and Type)
         */
        public Map<NamespacedKey, T> getRegistry() {
            return registry;
        }
    }
}
