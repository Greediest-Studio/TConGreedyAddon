package com.smd.tcongreedyaddon.proxy;

import com.smd.tcongreedyaddon.network.NetworkHandler;
import net.minecraft.item.Item;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;

public class CommonProxy {
    public void init() {
    }
    public void initToolGuis() {
    }

    public void registerToolModel(ToolCore tc) {
    }

    public <T extends Item & IToolPart> void registerToolPartModel(T part) {
    }

    public void registerSubscriptions() {
    }

    public void registerBookData() {
    }

    public void preInit() {
        NetworkHandler.register();
    }

}
