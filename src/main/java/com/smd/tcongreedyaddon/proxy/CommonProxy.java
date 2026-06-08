package com.smd.tcongreedyaddon.proxy;

import com.smd.tcongreedyaddon.network.NetworkHandler;
import com.smd.tcongreedyaddon.util.TicArmorTraitCache;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;

public class CommonProxy {

    public void initToolGuis() {
    }

    public void registerToolModel(ToolCore tc) {
    }

    public <T extends Item & IToolPart> void registerToolPartModel(T part) {
    }

    public void registerBookData() {
    }

    public void preInit() {
        NetworkHandler.register();
        MinecraftForge.EVENT_BUS.register(TicArmorTraitCache.INSTANCE);
    }

}
