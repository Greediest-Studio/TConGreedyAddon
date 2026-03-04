package com.smd.tcongreedyaddon.proxy;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;
import com.smd.tcongreedyaddon.event.BattleaxeHandler;
import com.smd.tcongreedyaddon.plugin.oldweapons.OldWeapons;

public class CommonProxy {
    public void init() {
        if (OldWeapons.battleaxe != null) {
            MinecraftForge.EVENT_BUS.register(BattleaxeHandler.INSTANCE);
        }
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

}
