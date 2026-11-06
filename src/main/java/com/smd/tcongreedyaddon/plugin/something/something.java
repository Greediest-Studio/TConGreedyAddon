package com.smd.tcongreedyaddon.plugin.something;

import com.smd.tcongreedyaddon.init.TraitRegistry;
import com.smd.tcongreedyaddon.item.something.ItemBowClicker;
import com.smd.tcongreedyaddon.item.something.ItemCiallo;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.plugin.ModuleConfig;
import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.traits.modifiers.base.something.*;
import com.smd.tcongreedyaddon.traits.something.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.traits.AbstractTrait;

import java.util.Locale;

public class something implements IModule {

    @Override
    public String getModuleName() {
        return "something";
    }

    public static TraitCiallo ciallo;
    public static TraitSoundEffect soundeffects;
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
    public static ItemCiallo cialloRecord;

    public static String baiduAppId = "";
    public static String baiduAppKey = "";

    @Override
    public void setupModuleConfig(ModuleConfig config) {
        baiduAppId = config.string("baiduAppId", "", "Baidu Translate APP ID");
        baiduAppKey = config.string("baiduAppKey", "", "Baidu Translate APP Key");
    }

    @Override
    public void init() {
        cleverTranslation = new TraitCleverTranslation();
        connection404 = new TraitConnection404();
        overcharge = new TraitOvercharge();
        deathboost = new TraitDeathBoost();
        speed = new TraitSpeed();
        TravelStaff = new TraitTravel();

        levelingdamage = new TraitLevelingDamage();
        autobow = new TraitAutoBow();
        erasecommand = new TraitEraseCommand();
        soundeffects = new TraitSoundEffect();
        ciallo = new TraitCiallo();

        TinkerRegistry.addTrait(ciallo);
        TinkerRegistry.addTrait(soundeffects);
        TinkerRegistry.addTrait(cleverTranslation);
        TinkerRegistry.addTrait(connection404);
        TinkerRegistry.addTrait(overcharge);
        TinkerRegistry.addTrait(deathboost);
        TinkerRegistry.addTrait(speed);
        TinkerRegistry.addTrait(TravelStaff);

        ciallo.initItem();
        soundeffects.initItem();
        autobow.initItem();
        levelingdamage.initItem();
        erasecommand.initItem();

        TraitRegistry.register(autobow);
        TraitRegistry.register(soundeffects);
        TraitRegistry.register(levelingdamage);
        TraitRegistry.register(erasecommand);
        TraitRegistry.register(ciallo);
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {
        bowClicker = new ItemBowClicker();
        event.getRegistry().register(bowClicker);

        cialloRecord = new ItemCiallo();
        event.getRegistry().register(cialloRecord);
    }

    @Override
    public void initRecipes(RegistryEvent.Register<IRecipe> event) {
        if (cialloRecord == null) {
            return;
        }

        ItemStack jukebox = new ItemStack(Item.getItemFromBlock(Blocks.JUKEBOX));
        for (Item record : ForgeRegistries.ITEMS) {
            if (!(record instanceof ItemRecord)) {
                continue;
            }
            ResourceLocation recordId = record.getRegistryName();
            if (recordId == null) {
                continue;
            }

            NonNullList<Ingredient> ingredients = NonNullList.create();
            ingredients.add(Ingredient.fromStacks(jukebox.copy()));
            ingredients.add(Ingredient.fromStacks(new ItemStack(record)));

            ShapelessRecipes recipe = new ShapelessRecipes(Tags.MOD_ID, new ItemStack(cialloRecord), ingredients);
            String path = ("ciallo_record_" + recordId.getNamespace() + "_" + recordId.getPath())
                    .toLowerCase(Locale.ROOT);
            recipe.setRegistryName(new ResourceLocation(Tags.MOD_ID, path));
            event.getRegistry().register(recipe);
        }
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(bowClicker, 0,
                new ModelResourceLocation(Tags.MOD_ID + ":bow_clicker", "inventory"));
        ModelLoader.setCustomModelResourceLocation(cialloRecord, 0,
                new ModelResourceLocation(Tags.MOD_ID + ":ciallo", "inventory"));
    }
}
