package com.smd.tcongreedyaddon.plugin.something;

import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.traits.something.TraitCiallo;
import com.smd.tcongreedyaddon.traits.something.TraitCleverTranslation;
import com.smd.tcongreedyaddon.traits.something.TraitConnection404;
import com.smd.tcongreedyaddon.traits.something.TraitSoundEffect;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public class something implements IModule {

    @Override
    public String getModuleName() { return "something"; }

    public static final AbstractTrait ciallo = new TraitCiallo();
    public static final AbstractTrait soundeffects = new TraitSoundEffect();
    public static final AbstractTrait cleverTranslation = new TraitCleverTranslation();
    public static final AbstractTrait connection404 = new TraitConnection404();

    public static String baiduAppId = "";
    public static String baiduAppKey = "";

    @Override
    public boolean isModAvailable() {
        return Loader.isModLoaded("tconstruct");
    }

    @Override
    public boolean hasDetailedConfig() {
        return true;
    }

    @Override
    public void setupModuleConfig(Configuration config) {
        config.getCategory(getModuleName()).setComment("自定义特性配置");
        config.get(getModuleName(), "baiduAppId", "", "Baidu Translate APP ID");
        config.get(getModuleName(), "baiduAppKey", "", "Baidu Translate APP Key");
    }

    @Override
    public void loadModuleConfig(Configuration config) {
        if (!config.isChild) {
            config.load();
        }

        baiduAppId = config.get(getModuleName(), "baiduAppId", "").getString();
        baiduAppKey = config.get(getModuleName(), "baiduAppKey", "").getString();
    }

    @Override
    public void preInit() {
    }

    @Override
    public void init() {
        TinkerRegistry.addTrait(ciallo);
        TinkerRegistry.addTrait(soundeffects);
        TinkerRegistry.addTrait(cleverTranslation);
        TinkerRegistry.addTrait(connection404);
    }

    @Override
    public void postInit() {
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {
    }
}
