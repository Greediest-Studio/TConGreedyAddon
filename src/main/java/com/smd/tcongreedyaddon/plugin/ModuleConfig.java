package com.smd.tcongreedyaddon.plugin;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import java.util.HashMap;
import java.util.Map;

public class ModuleConfig {
    private final String moduleName;
    private final Map<String, Property> properties = new HashMap<>();
    private final Configuration config;

    public ModuleConfig(String moduleName, Configuration config) {
        this.moduleName = moduleName;
        this.config = config;
        config.addCustomCategoryComment(moduleName,
                "Configuration for " + moduleName + " module");
    }

    public Property addBoolean(String key, boolean defaultValue, String comment) {
        Property prop = config.get(moduleName, key, defaultValue, comment);
        properties.put(key, prop);
        return prop;
    }

    public Property addInteger(String key, int defaultValue, int minValue, int maxValue, String comment) {
        Property prop = config.get(moduleName, key, defaultValue, comment, minValue, maxValue);
        properties.put(key, prop);
        return prop;
    }

    public Property addDouble(String key, double defaultValue, double minValue, double maxValue, String comment) {
        Property prop = config.get(moduleName, key, defaultValue, comment, minValue, maxValue);
        properties.put(key, prop);
        return prop;
    }

    public Property addString(String key, String defaultValue, String comment) {
        Property prop = config.get(moduleName, key, defaultValue, comment);
        properties.put(key, prop);
        return prop;
    }

    public Property addStringList(String key, String[] defaultValue, String comment) {
        Property prop = config.get(moduleName, key, defaultValue, comment);
        properties.put(key, prop);
        return prop;
    }

    public boolean getBoolean(String key) {
        Property prop = properties.get(key);
        return prop != null ? prop.getBoolean() : false;
    }

    public int getInteger(String key) {
        Property prop = properties.get(key);
        return prop != null ? prop.getInt() : 0;
    }

    public double getDouble(String key) {
        Property prop = properties.get(key);
        return prop != null ? prop.getDouble() : 0.0;
    }

    public String getString(String key) {
        Property prop = properties.get(key);
        return prop != null ? prop.getString() : "";
    }

    public String[] getStringList(String key) {
        Property prop = properties.get(key);
        return prop != null ? prop.getStringList() : new String[0];
    }

    public void save() {
        if (config.hasChanged()) {
            config.save();
        }
    }
}