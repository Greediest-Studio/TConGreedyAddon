package com.smd.tcongreedyaddon.plugin;

import net.minecraftforge.common.config.Configuration;

public class ModuleConfig {
    private final String moduleName;
    private final Configuration config;

    public ModuleConfig(String moduleName, Configuration config) {
        this.moduleName = moduleName;
        this.config = config;
    }

    public boolean bool(String key, boolean defaultValue, String comment) {
        ensureCategoryComment();
        return config.get(moduleName, key, defaultValue, comment).getBoolean(defaultValue);
    }

    public int integer(String key, int defaultValue, int minValue, int maxValue, String comment) {
        ensureCategoryComment();
        return config.get(moduleName, key, defaultValue, comment, minValue, maxValue).getInt(defaultValue);
    }

    public double doubleValue(String key, double defaultValue, double minValue, double maxValue, String comment) {
        ensureCategoryComment();
        return config.get(moduleName, key, defaultValue, comment, minValue, maxValue).getDouble(defaultValue);
    }

    public String string(String key, String defaultValue, String comment) {
        ensureCategoryComment();
        return config.get(moduleName, key, defaultValue, comment).getString();
    }

    public String[] stringList(String key, String[] defaultValue, String comment) {
        ensureCategoryComment();
        return config.get(moduleName, key, defaultValue, comment).getStringList();
    }

    public String enumValue(String key, String defaultValue, String[] validValues, String comment) {
        ensureCategoryComment();
        return config.get(moduleName, key, defaultValue, comment, validValues).getString();
    }

    public int boundedInteger(String key, int defaultValue, int min, int max, String comment) {
        ensureCategoryComment();
        return config.get(moduleName, key, defaultValue, comment, min, max).getInt(defaultValue);
    }

    public int percent(String key, int defaultValue, String comment) {
        return boundedInteger(key, defaultValue, 0, 100, comment);
    }

    private void ensureCategoryComment() {
        config.addCustomCategoryComment(moduleName,
                "Configuration for " + moduleName + " module");
    }
}
