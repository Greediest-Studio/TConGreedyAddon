package com.smd.tcongreedyaddon;

import com.smd.tcongreedyaddon.init.SoundsHandler;
import com.smd.tcongreedyaddon.plugin.SpecialWeapons.SpecialWeapons;
import com.smd.tcongreedyaddon.plugin.something.something;
import com.smd.tcongreedyaddon.tools.magicbook.gui.MagicBookGuiHandler;
import com.smd.tcongreedyaddon.util.MaterialRenderingDebugHelper;
import com.smd.tcongreedyaddon.plugin.AbyssalCraft.AbyssalCraft;
import com.smd.tcongreedyaddon.plugin.ModuleManager;
import com.smd.tcongreedyaddon.plugin.oldweapons.OldWeapons;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.smd.tcongreedyaddon.proxy.CommonProxy;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod.EventBusSubscriber
@Mod(name = Tags.MOD_NAME, modid = Tags.MOD_ID, version = Tags.VERSION, dependencies = "after:tconstruct;after:plustic;after:tconevo")
public class TConGreedyAddon {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static Configuration config;

    @Mod.Instance(Tags.MOD_ID)
    public static TConGreedyAddon instance;

    @SidedProxy(serverSide = "com.smd.tcongreedyaddon.proxy.CommonProxy",
                clientSide = "com.smd.tcongreedyaddon.proxy.ClientProxy")

    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("TConGreedyAddon Pre-Initialization");
        LOGGER.info("Material Shader Fix Mixin is active!");
        
        config = new Configuration(event.getSuggestedConfigurationFile());


        ModuleManager.registerModule(new OldWeapons());
        ModuleManager.registerModule(new AbyssalCraft());
        ModuleManager.registerModule(new something());
        ModuleManager.registerModule(new SpecialWeapons());

        ModuleManager.setupConfig(config);

        ModuleManager.preInitActiveModules(event);

        proxy.registerSubscriptions();
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("TConGreedyAddon Initialization");
        ModuleManager.initActiveModules(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new MagicBookGuiHandler());
        proxy.initToolGuis();
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("TConGreedyAddon Post-Initialization");
        ModuleManager.postInitActiveModules(event);
        proxy.registerBookData();
        proxy.initToolGuis();
        
        // Log material shader fix summary
        MaterialRenderingDebugHelper.logMaterialShaderFixSummary();
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        SoundsHandler.register(event.getRegistry());
    }
}
