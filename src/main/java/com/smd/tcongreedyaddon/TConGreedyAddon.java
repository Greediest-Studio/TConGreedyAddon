package com.smd.tcongreedyaddon;

import com.smd.tcongreedyaddon.init.SoundsHandler;
import com.smd.tcongreedyaddon.util.MaterialRenderingDebugHelper;
import com.smd.tcongreedyaddon.plugin.ModuleManager;
import com.smd.tcongreedyaddon.plugin.Modules;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.smd.tcongreedyaddon.proxy.CommonProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod.EventBusSubscriber
@Mod(name = Tags.MOD_NAME,
     modid = Tags.MOD_ID,
     version = Tags.VERSION,
     dependencies = "after:tconstruct",
     guiFactory = "com.smd.tcongreedyaddon.gui.TConGuiFactory")
public class TConGreedyAddon {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static Configuration config;

    public static ModuleManager modulemanager;

    @SidedProxy(serverSide = "com.smd.tcongreedyaddon.proxy.CommonProxy",
                clientSide = "com.smd.tcongreedyaddon.proxy.ClientProxy")

    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        config = new Configuration(event.getSuggestedConfigurationFile());

        modulemanager = new ModuleManager(config);

        Modules.registerAll(modulemanager);

        modulemanager.setupConfig();

        modulemanager.preInitActiveModules(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        modulemanager.initActiveModules(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

        modulemanager.postInitActiveModules(event);

        if(Loader.isModLoaded("tconstruct")) {
            proxy.registerBookData();
        }

        MaterialRenderingDebugHelper.logMaterialShaderFixSummary();
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        SoundsHandler.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void initItems(RegistryEvent.Register<Item> event) {
        TConGreedyAddon.modulemanager.initItems(event);
    }
}
