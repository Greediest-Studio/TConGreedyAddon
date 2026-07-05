package com.smd.tcongreedyaddon.plugin.oldweapons;

import com.smd.tcongreedyaddon.event.BattleaxeHandler;
import com.smd.tcongreedyaddon.init.TraitRegistry;
import com.smd.tcongreedyaddon.plugin.ModuleConfig;
import com.smd.tcongreedyaddon.tools.oldweapons.*;
import com.smd.tcongreedyaddon.traits.modifiers.base.ModTest;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.ToolPart;
import slimeknights.tconstruct.tools.ranged.TinkerRangedWeapons;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.init.TotalTinkersRegister;

public class oldweapons implements IModule {

    private boolean enabledrag;
    private boolean enableBattleAxe;
    private boolean enableGreatblade;
    private boolean enableAllInOneTool;
    private boolean enablecrossbowoveride;

    @Override
    public String getModuleName() {
        return "oldweapons";
    }

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
        enablecrossbowoveride = config.bool("enablecrossbowoveride",true,"是否启用十字弩自动装填");
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {

        greatbladeCore = TotalTinkersRegister.registerToolPart(event, "greatbladeCore", Material.VALUE_Ingot * 24);

        if (enableBattleAxe) {
            battleaxe = new WeaponBattleAxe();
            TotalTinkersRegister.initForgeTool(battleaxe, event);
        }

        if (enableAllInOneTool) {
            allinonetool = new AllInOneTool();
            TotalTinkersRegister.initForgeTool(allinonetool, event);
        }

        if (enabledrag) {
            dagger = new WeaponDagger();
            TotalTinkersRegister.initBaseForgeTool(dagger,event);
        }

        if(enablecrossbowoveride) {
            TinkerRangedWeapons.crossBow = new WeaponCrossbowOveride();
            TotalTinkersRegister.initForgeTool(TinkerRangedWeapons.crossBow, event);
        }

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

        if (enableBattleAxe) {
            MinecraftForge.EVENT_BUS.register(BattleaxeHandler.INSTANCE);
        }
    }

    @Override
    public void postInitClient(FMLPostInitializationEvent event) {
        if (enableGreatblade) {
            ToolBuildGuiInfo greatbladeInfo = new ToolBuildGuiInfo(greatblade);
            greatbladeInfo.addSlotPosition(33 - 10 - 14, 42 + 10 + 12); // handle
            greatbladeInfo.addSlotPosition(33 - 8 + 6, 42 - 10 + 4 - 4); // head
            greatbladeInfo.addSlotPosition(33 + 14 + 6, 42 - 10 - 2 - 4); // head 2
            greatbladeInfo.addSlotPosition(33 + 10 - 10, 42 + 10 + 6); //core
            greatbladeInfo.addSlotPosition( 33 - 10 - 12, 42); //guard
            TinkerRegistryClient.addToolBuilding(greatbladeInfo);
        }
        if (enableBattleAxe) {
            ToolBuildGuiInfo battleaxeInfo = new ToolBuildGuiInfo(battleaxe);
            battleaxeInfo.addSlotPosition(33 - 10 - 2, 42 + 10); // handle
            battleaxeInfo.addSlotPosition(33 + 10 + 16 - 2, 42 - 10 + 16); // head 1
            battleaxeInfo.addSlotPosition(33 + 10 - 16 - 2, 42 - 10 - 16); // head 2
            battleaxeInfo.addSlotPosition(33 + 13 - 2, 42 - 13); // binding
            TinkerRegistryClient.addToolBuilding(battleaxeInfo);
        }
        if (enabledrag) {
            ToolBuildGuiInfo daggerInfo = new ToolBuildGuiInfo(dagger);
            daggerInfo.addSlotPosition(33 - 20 - 1, 42 + 20); // handle
            daggerInfo.addSlotPosition(33 + 20 - 5, 42 - 20 + 4); // blade
            daggerInfo.addSlotPosition(33 - 2 - 1, 42 + 2); // guard
            TinkerRegistryClient.addToolBuilding(daggerInfo);
        }
        if (enableAllInOneTool) {
            ToolBuildGuiInfo allinonetoolInfo = new ToolBuildGuiInfo(allinonetool);
            allinonetoolInfo.addSlotPosition(33 - 10 - 14, 42 + 10 + 12); // handle
            allinonetoolInfo.addSlotPosition(33 - 8 + 6, 42 - 10 + 4 - 4); // head
            allinonetoolInfo.addSlotPosition(33 + 14 + 6, 42 - 10 - 2 - 4); // head 2
            allinonetoolInfo.addSlotPosition(33 + 10 - 10, 42 + 10 + 6); //core
            allinonetoolInfo.addSlotPosition( 33 - 10 - 12, 42); //guard
            TinkerRegistryClient.addToolBuilding(allinonetoolInfo);
        }
    }
}
