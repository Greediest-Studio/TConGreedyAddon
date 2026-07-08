package com.smd.tcongreedyaddon.plugin.solidarytinker;

import com.smd.tcongreedyaddon.client.SoulGeRenderHandler;
import com.smd.tcongreedyaddon.event.SoulGeEventHandler;
import com.smd.tcongreedyaddon.init.TotalTinkersRegister;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.tools.solidarytinker.SoulGe;
import com.smd.tcongreedyaddon.tools.solidarytinker.SoulGeTypes;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.ToolPart;

public class solidarytinker implements IModule {

    public static ToolPart soulgeHeart;
    public static SoulGe soulge;

    @Override
    public String getModuleName() {
        return "solidarytinker";
    }

    @Override
    public boolean isEnabledByDefault(){
        return false;
    }

    @Override
    public void preInit() {
        SoulGeTypes.init();
    }

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(SoulGeEventHandler.INSTANCE);
    }

    @Override
    public void initClient(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(SoulGeRenderHandler.INSTANCE);
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {
        soulgeHeart = TotalTinkersRegister.registerToolPart(event, "soulge_heart", Material.VALUE_Ingot * 8);
        soulge = new SoulGe();
        TotalTinkersRegister.initForgeTool(soulge, event);
    }

    @Override
    public void postInitClient(FMLPostInitializationEvent event) {
        if (solidarytinker.soulge != null) {
            ToolBuildGuiInfo soulgeInfo = new ToolBuildGuiInfo(solidarytinker.soulge);
            soulgeInfo.addSlotPosition(45, 46); // small blade
            soulgeInfo.addSlotPosition(25, 46); // soulge heart
            soulgeInfo.addSlotPosition(45, 26); // small blade
            soulgeInfo.addSlotPosition(25, 26); // broad blade
            soulgeInfo.addSlotPosition(7, 62);  // tough handle
            TinkerRegistryClient.addToolBuilding(soulgeInfo);
        }
    }
}
