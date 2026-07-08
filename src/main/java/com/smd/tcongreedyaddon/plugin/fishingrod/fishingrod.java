package com.smd.tcongreedyaddon.plugin.fishingrod;

import com.smd.tcongreedyaddon.init.TotalTinkersRegister;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.plugin.ModuleConfig;
import com.smd.tcongreedyaddon.tools.fishingrod.TinkerFishingRod;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;

public class fishingrod implements IModule {

    public static TinkerFishingRod fishingRod;

    private boolean enableFishingRod;
    private boolean enableCombatDamage;

    @Override
    public String getModuleName() {
        return "fishingrod";
    }

    @Override
    public void setupModuleConfig(ModuleConfig config) {
        enableFishingRod = config.bool("enableFishingRod", true, "是否启用匠魂钓鱼竿");
        enableCombatDamage = config.bool("enableCombatDamage", true, "是否启用钓鱼竿收竿时对钩中实体造成伤害");
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {
        if (enableFishingRod) {
            fishingRod = new TinkerFishingRod(enableCombatDamage);
            TotalTinkersRegister.initForgeTool(fishingRod, event);
        }
    }

    @Override
    public void postInitClient(FMLPostInitializationEvent event) {
        if (enableFishingRod) {
            ToolBuildGuiInfo info = new ToolBuildGuiInfo(fishingRod);
            info.addSlotPosition(28, 38); // bow limb
            info.addSlotPosition(50, 38); // bowstring
            info.addSlotPosition(50, 58); // hook
            TinkerRegistryClient.addToolBuilding(info);
        }
    }
}
