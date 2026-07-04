package com.smd.tcongreedyaddon.plugin.oldweapons;

import com.smd.tcongreedyaddon.event.BattleaxeHandler;
import com.smd.tcongreedyaddon.init.TraitRegistry;
import com.smd.tcongreedyaddon.plugin.ModuleConfig;
import com.smd.tcongreedyaddon.tools.oldweapons.*;
import com.smd.tcongreedyaddon.traits.modifiers.base.ModTest;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
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
    public void setupModuleConfig(ModuleConfig config) {
        enabledrag = config.bool("enabledrag", true, "是否启用短剑");
        enableBattleAxe = config.bool("enableBattleAxe", true, "是否启用战斧");
        enableGreatblade = config.bool("enableGreatblade", true, "是否启用巨剑");
        enableAllInOneTool = config.bool("enableAllInOneTool", true, "是否启用多功能工具");
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
        TraitRegistry.register(test);
        test.addItem("stone", 1, 1);

        if (enableBattleAxe && battleaxe != null) {
            MinecraftForge.EVENT_BUS.register(BattleaxeHandler.INSTANCE);
        }
    }
}
