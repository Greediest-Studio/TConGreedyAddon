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
import org.apache.logging.log4j.LogManager;

import java.util.*;
import org.apache.logging.log4j.Logger;

public class ModuleManager {
    private final Map<String, IModule> modules = new LinkedHashMap<>();
    private final Set<String> activeModules = new LinkedHashSet<>();
    private final Configuration config;
    private final Map<String, ModuleConfig> moduleConfigs = new HashMap<>();
    private final Logger logger = LogManager.getLogger("ModuleManager");

    public ModuleManager(Configuration config) {
        this.config = config;
    }

    public void registerModule(IModule module) {
        modules.put(module.getModuleName(), module);
    }

    public void setupConfig() {
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

    public void reloadConfig() {
        config.load();
        for (String name : activeModules) {
            IModule module = modules.get(name);
            if (module == null) {
                continue;
            }
            if (module.hasDetailedConfig()) {
                ModuleConfig mc = moduleConfigs.get(module.getModuleName());
                if (mc != null) {
                    module.loadModuleConfig(mc);
                    module.onConfigReload();
                }
            }
        }
    }

    public void preInitActiveModules(FMLPreInitializationEvent event) {
        List<IModule> sorted = new ArrayList<>(modules.values());
        sorted.sort(Comparator.comparingInt(IModule::priority).reversed()); // 可以自定义顺序
        for (IModule module : sorted) {
            try {
                boolean enabled = config.get("modules", module.getModuleName(),
                        module.isEnabledByDefault()).getBoolean() && module.isModAvailable();
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
            } catch (Exception e) {
                logger.error("Failed to preInit module: {}", module.getModuleName(), e);
            }
        }
    }

    public void initActiveModules(FMLInitializationEvent event) {
        for (String name : activeModules) {
            IModule m = modules.get(name);
            try {
                m.init();
                if (isClientSide()) {
                    m.initClient(event);
                } else {
                    m.initServer(event);
                }
            } catch (Exception e) {
                logger.error("Failed to init module: {}", name, e);
            }
        }
        if (config.hasChanged()) {
            config.save();
        }
    }

    public void postInitActiveModules(FMLPostInitializationEvent event) {
        for (String name : activeModules) {
            IModule m = modules.get(name);
            try {
                m.postInit();
                if (isClientSide()) {
                    m.postInitClient(event);
                } else {
                    m.postInitServer(event);
                }
            } catch (Exception e) {
                logger.error("Failed to postInit module: {}", name, e);
            }
        }
        if (config.hasChanged()) {
            config.save();
        }
    }

    public void initItems(RegistryEvent.Register<Item> event) {
        for (String name : activeModules) {
            try {
                modules.get(name).initItems(event);
            } catch (Exception e) {
                logger.error("Failed to register items for module: {}", name, e);
            }
        }
    }

    public void onModelRegistry(ModelRegistryEvent event) {
        for (String name : activeModules) {
            try {
                modules.get(name).registerModels(event);
            } catch (Exception e) {
                logger.error("Failed to register models for module: {}", name, e);
            }
        }
    }

    public boolean isModuleActive(String name) {
        return activeModules.contains(name);
    }

    public Configuration getConfig() {
        return config;
    }

    private static boolean isClientSide() {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }
}