package com.smd.tcongreedyaddon.proxy;

import net.minecraft.item.Item;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;

public class CommonProxy {

    public void registerToolModel(ToolCore tc) {
    }

    public <T extends Item & IToolPart> void registerToolPartModel(T part) {
    }

    public void registerBookData() {
    }
}
