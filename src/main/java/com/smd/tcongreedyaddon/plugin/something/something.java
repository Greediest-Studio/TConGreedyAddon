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
    public String getModuleName() {
        return "something";
    }

    public static AbstractTrait ciallo;
    public static AbstractTrait soundeffects;
    public static AbstractTrait cleverTranslation;
    public static AbstractTrait connection404;
    public static AbstractTrait autobow;
    public static AbstractTrait overcharge;
    public static AbstractTrait deathboost;
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
    public void setupModuleConfig(ModuleConfig config) {
        baiduAppId = config.string("baiduAppId", "", "Baidu Translate APP ID");
        baiduAppKey = config.string("baiduAppKey", "", "Baidu Translate APP Key");
    }

    @Override
    public void init() {
        ciallo = new TraitCiallo();
        soundeffects = new TraitSoundEffect();
        cleverTranslation = new TraitCleverTranslation();
        connection404 = new TraitConnection404();
        autobow = new TraitAutoBow();
        overcharge = new TraitOvercharge();
        deathboost = new TraitDeathBoost();
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
        TraitRegistry.register(levelingdamage);
        erasecommand = new TraitEraseCommand();
        erasecommand.initItem();
        TraitRegistry.register(erasecommand);
    }
}
