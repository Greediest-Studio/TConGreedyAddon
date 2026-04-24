package com.smd.tcongreedyaddon.plugin;

import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IModule {
    String getModuleName();
    boolean isModAvailable();
    default void preInit() {};
    default void init() {}
    default void postInit() {}
    void initItems(RegistryEvent.Register<Item> event);
    default boolean isEnabledByDefault() {return true;}
    default void setupModuleConfig(ModuleConfig config) {}
    default void loadModuleConfig(ModuleConfig config) {}
    default boolean hasDetailedConfig() { return false; }
    default void preInitClient(FMLPreInitializationEvent event) {}
    default void preInitServer(FMLPreInitializationEvent event) {}
    default void initClient(FMLInitializationEvent event) {}
    default void initServer(FMLInitializationEvent event) {}
    default void postInitClient(FMLPostInitializationEvent event) {}
    default void postInitServer(FMLPostInitializationEvent event) {}
}
