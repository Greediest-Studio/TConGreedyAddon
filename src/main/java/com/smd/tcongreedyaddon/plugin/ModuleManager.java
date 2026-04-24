package com.smd.tcongreedyaddon.plugin;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Configuration;
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
    private static final Set<String> activeModules = new HashSet<>();
    private static Configuration config;
    private static final Map<String, ModuleConfig> moduleConfigs = new HashMap<>();

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
            config.get("modules", module.getModuleName(), module.isEnabledByDefault(),
                    "Enable " + module.getModuleName() + " integration");
        }

        for (IModule module : modules.values()) {
            if (module.hasDetailedConfig()) {
                ModuleConfig mc = new ModuleConfig(module.getModuleName(), config);
                moduleConfigs.put(module.getModuleName(), mc);
                module.setupModuleConfig(mc);
            }
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void preInitActiveModules(FMLPreInitializationEvent event) {
        for (IModule module : modules.values()) {
            boolean enabled = config.get("modules", module.getModuleName(),
                    module.isEnabledByDefault()).getBoolean()
                    && module.isModAvailable();

            if (enabled) {
                if (module.hasDetailedConfig()) {
                    ModuleConfig mc = moduleConfigs.get(module.getModuleName());
                    module.loadModuleConfig(mc);
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
        for (String name : activeModules) {
            IModule m = modules.get(name);
            m.init();
            if (isClientSide()) m.initClient(event);
            else m.initServer(event);
        }
        if (config.hasChanged()) config.save();
    }

    public static void postInitActiveModules(FMLPostInitializationEvent event) {
        for (String name : activeModules) {
            IModule m = modules.get(name);
            m.postInit();
            if (isClientSide()) m.postInitClient(event);
            else m.postInitServer(event);
        }
        if (config.hasChanged()) config.save();
    }

    public static void initItems(RegistryEvent.Register<Item> event) {
        for (String name : activeModules) {
            modules.get(name).initItems(event);
        }
    }

    public static void onModelRegistry(ModelRegistryEvent event) {
        for (String name : activeModules) {
            modules.get(name).registerModels(event);
        }
    }

    public static boolean isModuleActive(String name) {
        return activeModules.contains(name);
    }

    public static Configuration getConfig() {
        return config;
    }
}