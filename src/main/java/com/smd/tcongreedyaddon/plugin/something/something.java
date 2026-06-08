package com.smd.tcongreedyaddon.plugin.something;

import com.smd.tcongreedyaddon.init.TraitRegistry;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.plugin.ModuleConfig;
import com.smd.tcongreedyaddon.traits.modifiers.base.something.TraitEraseCommand;
import com.smd.tcongreedyaddon.traits.modifiers.base.something.TraitLevelingDamage;
import com.smd.tcongreedyaddon.traits.something.*;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public class something implements IModule {

    @Override
    public String getModuleName() { return "something"; }

    public static AbstractTrait ciallo;
    public static final AbstractTrait soundeffects = new TraitSoundEffect();
    public static final AbstractTrait cleverTranslation = new TraitCleverTranslation();
    public static final AbstractTrait connection404 = new TraitConnection404();
    public static final AbstractTrait autobow = new TraitAutoBow();
    public static AbstractTrait overcharge;
    public static final AbstractTrait deathboost = new TraitDeathBoost();
    public static AbstractTrait speed;
    public static AbstractTrait TravelStaff;

    public static TraitLevelingDamage levelingdamage;
    public static TraitEraseCommand erasecommand;

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
    public void init() {
        ciallo = new TraitCiallo();
        overcharge = new TraitOvercharge();
        speed = new TraitSpeed();
        TravelStaff = new TraitTravel();

        TinkerRegistry.addTrait(ciallo);
        TinkerRegistry.addTrait(soundeffects);
        TinkerRegistry.addTrait(cleverTranslation);
        TinkerRegistry.addTrait(connection404);
        TinkerRegistry.addTrait(autobow);
        TinkerRegistry.addTrait(overcharge);
        TinkerRegistry.addTrait(deathboost);
        TinkerRegistry.addTrait(speed);
        TinkerRegistry.addTrait(TravelStaff);

        levelingdamage = new TraitLevelingDamage();
        levelingdamage.initItem();
        TraitRegistry.REGISTERED_TRAITS.add(levelingdamage);
        erasecommand = new TraitEraseCommand();
        erasecommand.initItem();
        TraitRegistry.REGISTERED_TRAITS.add(erasecommand);
    }
}
