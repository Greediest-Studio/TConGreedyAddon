package com.smd.tcongreedyaddon.plugin.something;

import com.smd.tcongreedyaddon.init.TraitRegistry;
import com.smd.tcongreedyaddon.item.something.ItemBowClicker;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.plugin.ModuleConfig;
import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.traits.modifiers.base.something.TraitAutoBow;
import com.smd.tcongreedyaddon.traits.modifiers.base.something.TraitEraseCommand;
import com.smd.tcongreedyaddon.traits.modifiers.base.something.TraitLevelingDamage;
import com.smd.tcongreedyaddon.traits.something.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
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
    public static AbstractTrait overcharge;
    public static AbstractTrait deathboost;
    public static AbstractTrait speed;
    public static AbstractTrait TravelStaff;

    public static TraitAutoBow autobow;
    public static TraitLevelingDamage levelingdamage;
    public static TraitEraseCommand erasecommand;
    public static ItemBowClicker bowClicker;

    public static String baiduAppId = "";
    public static String baiduAppKey = "";

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
        overcharge = new TraitOvercharge();
        deathboost = new TraitDeathBoost();
        speed = new TraitSpeed();
        TravelStaff = new TraitTravel();

        TinkerRegistry.addTrait(ciallo);
        TinkerRegistry.addTrait(soundeffects);
        TinkerRegistry.addTrait(cleverTranslation);
        TinkerRegistry.addTrait(connection404);
        TinkerRegistry.addTrait(overcharge);
        TinkerRegistry.addTrait(deathboost);
        TinkerRegistry.addTrait(speed);
        TinkerRegistry.addTrait(TravelStaff);

        autobow = new TraitAutoBow();
        autobow.initItem();
        TraitRegistry.register(autobow);

        levelingdamage = new TraitLevelingDamage();
        levelingdamage.initItem();
        TraitRegistry.register(levelingdamage);
        erasecommand = new TraitEraseCommand();
        erasecommand.initItem();
        TraitRegistry.register(erasecommand);
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {
        bowClicker = new ItemBowClicker();
        event.getRegistry().register(bowClicker);
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(bowClicker, 0,
                new ModelResourceLocation(Tags.MOD_ID + ":bow_clicker", "inventory"));
    }
}
