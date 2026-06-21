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
import net.minecraftforge.fml.common.Loader;
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
    public boolean isModAvailable() {
        return Loader.isModLoaded("tconstruct");
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
    public void initClient(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(SoulGeRenderHandler.INSTANCE);
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {
        soulgeHeart = TotalTinkersRegister.registerToolPart(event, "soulge_heart", Material.VALUE_Ingot * 8);
        soulge = new SoulGe();
        TotalTinkersRegister.initForgeTool(soulge, event);
    }
}
