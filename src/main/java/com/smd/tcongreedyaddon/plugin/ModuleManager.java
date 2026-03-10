package com.smd.tcongreedyaddon.plugin;

import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModuleManager {
    private static final Map<String, IModule> modules = new HashMap<>();
    private static final Map<String, ModuleConfig> moduleConfigs = new HashMap<>();
    private static final Set<String> activeModules = new HashSet<>();
    private static Configuration config;

    private static boolean isClientSide() {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    public static void registerModule(IModule module) {
        modules.put(module.getModuleName(), module);
    }

    public static void setupConfig(Configuration config) {
        ModuleManager.config = config;
        config.addCustomCategoryComment("modules", "Enable/disable integration modules");

        config.load();

        for (IModule module : modules.values()) {
            Property prop = config.get(
                    "modules",
                    module.getModuleName(),
                    module.isEnabledByDefault(),
                    "Enable " + module.getModuleName() + " integration"
            );
            prop.setComment("Enable " + module.getModuleName() + " integration");
        }

        for (IModule module : modules.values()) {
            if (module.hasDetailedConfig()) {
                module.setupModuleConfig(config);
            }
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void preInitActiveModules(FMLPreInitializationEvent event) {
        for (IModule module : modules.values()) {
            // 检查模块是否启用
            boolean enabled = config.get(
                    "modules",
                    module.getModuleName(),
                    module.isEnabledByDefault()
            ).getBoolean() && module.isModAvailable();

            if (enabled) {
                if (module.hasDetailedConfig()) {
                    module.loadModuleConfig(config);
                }

                module.preInit();
                if (isClientSide()) {
                    module.preInitClient(event);
                } else {
                    module.preInitServer(event);
                }
                activeModules.add(module.getModuleName());
            }
        }
    }

    public static void initActiveModules(FMLInitializationEvent event) {
        for (IModule module : modules.values()) {
            boolean enabled = config.get(
                    "modules",
                    module.getModuleName(),
                    module.isEnabledByDefault()
            ).getBoolean() && module.isModAvailable();

            if (enabled) {

                if (module.hasDetailedConfig()) {
                    module.loadModuleConfig(config);
                }

                module.init();
                if (isClientSide()) {
                    module.initClient(event);
                } else {
                    module.initServer(event);
                }
            }
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void postInitActiveModules(FMLPostInitializationEvent event) {
        for (IModule module : modules.values()) {
            boolean enabled = config.get(
                    "modules",
                    module.getModuleName(),
                    module.isEnabledByDefault()
            ).getBoolean() && module.isModAvailable();

            if (enabled) {
                module.postInit();
                if (isClientSide()) {
                    module.postInitClient(event);
                } else {
                    module.postInitServer(event);
                }
            }
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void initItems(RegistryEvent.Register<Item> event) {
        for (IModule module : modules.values()) {
            boolean enabled = config.get(
                    "modules",
                    module.getModuleName(),
                    module.isEnabledByDefault()
            ).getBoolean() && module.isModAvailable();

            if (enabled) {
                module.initItems(event);
            }
        }
    }

    public static boolean isModuleActive(String name) {
        return activeModules.contains(name);
    }

    public static ModuleConfig getModuleConfig(String moduleName) {
        return moduleConfigs.get(moduleName);
    }

    public static void saveAllConfigs() {
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static Configuration getConfig() {
        return config;
    }
}
