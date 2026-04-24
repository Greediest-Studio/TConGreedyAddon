package com.smd.tcongreedyaddon.plugin.oldweapons;

import com.smd.tcongreedyaddon.event.BattleaxeHandler;
import com.smd.tcongreedyaddon.init.TraitRegistry;
import com.smd.tcongreedyaddon.plugin.ModuleConfig;
import com.smd.tcongreedyaddon.tools.oldweapons.*;
import com.smd.tcongreedyaddon.traits.modifiers.base.ModTest;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.ToolPart;
import slimeknights.tconstruct.tools.ranged.TinkerRangedWeapons;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.init.TotalTinkersRegister;

public class OldWeapons implements IModule {

    private boolean enabledrag;
    private boolean enableBattleAxe;
    private boolean enableGreatblade;
    private boolean enableAllInOneTool;

    @Override
    public String getModuleName() { return "OldWeapons"; }

    public static ToolPart greatbladeCore;
    public static WeaponBattleAxe battleaxe;
    public static WeaponDagger dagger;
    public static WeaponGreatblade greatblade;
    public static AllInOneTool allinonetool;
    public static ModTest test;

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

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
        config.addBoolean("enabledrag", true, "是否启用短剑");
        config.addBoolean("enableBattleAxe", true, "是否启用战斧");
        config.addBoolean("enableGreatblade", true, "是否启用巨剑");
        config.addBoolean("enableAllInOneTool", true, "是否启用多功能工具");
    }

    @Override
    public void loadModuleConfig(ModuleConfig config) {
        enabledrag = config.getBoolean("enabledrag");
        enableBattleAxe = config.getBoolean("enableBattleAxe");
        enableGreatblade = config.getBoolean("enableGreatblade");
        enableAllInOneTool = config.getBoolean("enableAllInOneTool");
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {

        greatbladeCore = TotalTinkersRegister.registerToolPart(event, "greatbladeCore", Material.VALUE_Ingot * 24);

        if (enableBattleAxe) {
            OldWeapons.battleaxe = new WeaponBattleAxe();
            TotalTinkersRegister.initForgeTool(OldWeapons.battleaxe, event);
        }

        if (enableAllInOneTool) {
            OldWeapons.allinonetool = new AllInOneTool();
            TotalTinkersRegister.initForgeTool(OldWeapons.allinonetool, event);
        }

        if (enabledrag) {
            OldWeapons.dagger = new WeaponDagger();
            event.getRegistry().register(OldWeapons.dagger);
            TinkerRegistry.registerToolForgeCrafting(OldWeapons.dagger);
            TinkerRegistry.registerToolStationCrafting(OldWeapons.dagger);
            TConGreedyAddon.proxy.registerToolModel(OldWeapons.dagger);
        }

        TinkerRangedWeapons.crossBow = new WeaponCrossbowOveride();
        TotalTinkersRegister.initForgeTool(TinkerRangedWeapons.crossBow, event);


        if (enableGreatblade) {
            greatblade = new WeaponGreatblade();
            TotalTinkersRegister.initForgeTool(greatblade, event);
        }
    }

    @Override
    public void init() {
        test = new ModTest();
        TraitRegistry.REGISTERED_TRAITS.add(test);
        test.addItem("stone", 1, 1);

        if (enableBattleAxe && battleaxe != null) {
            MinecraftForge.EVENT_BUS.register(BattleaxeHandler.INSTANCE);
        }
    }
}