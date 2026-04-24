package com.smd.tcongreedyaddon.plugin.something;

import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.plugin.ModuleConfig;
import com.smd.tcongreedyaddon.traits.something.*;
import net.minecraft.item.Item;
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
    public static final AbstractTrait autobow = new TraitAutoBow();
    public static final AbstractTrait overcharge = new TraitOvercharge();
    public static final AbstractTrait deathboost = new TraitDeathBoost();
    public static final AbstractTrait speed = new TraitSpeed();
    public static final AbstractTrait TravelStaff = new TraitTravel();

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
    public void setupModuleConfig(ModuleConfig config) {
        config.addString("baiduAppId", "", "Baidu Translate APP ID");
        config.addString("baiduAppKey", "", "Baidu Translate APP Key");
    }

    @Override
    public void loadModuleConfig(ModuleConfig config) {
        baiduAppId = config.getString("baiduAppId");
        baiduAppKey = config.getString("baiduAppKey");
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
        TinkerRegistry.addTrait(autobow);
        TinkerRegistry.addTrait(overcharge);
        TinkerRegistry.addTrait(deathboost);
        TinkerRegistry.addTrait(speed);
        TinkerRegistry.addTrait(TravelStaff);
    }

    @Override
    public void postInit() {
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {
    }
}
