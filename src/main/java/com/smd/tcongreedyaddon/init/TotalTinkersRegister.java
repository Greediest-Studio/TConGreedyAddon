package com.smd.tcongreedyaddon.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tools.Pattern;
import slimeknights.tconstruct.library.tools.ToolCore;
import com.smd.tcongreedyaddon.TConGreedyAddon;
import slimeknights.tconstruct.library.tools.ToolPart;
import slimeknights.tconstruct.tools.TinkerTools;

public class TotalTinkersRegister {

    public static void initForgeTool(ToolCore core, RegistryEvent.Register<Item> event) {
        event.getRegistry().register(core);
        TinkerRegistry.registerToolForgeCrafting(core);
        TConGreedyAddon.proxy.registerToolModel(core);
    }

    public static void initBaseForgeTool(ToolCore core, RegistryEvent.Register<Item> event){
        initForgeTool(core,event);
        TinkerRegistry.registerToolStationCrafting(core);
    }

    public static ToolPart registerToolPart(RegistryEvent.Register<Item> event,
                                            String name, int cost) {
        ToolPart part = new ToolPart(cost);
        part.setTranslationKey(name).setRegistryName(name);
        event.getRegistry().register(part);
        TinkerRegistry.registerToolPart(part);
        TConGreedyAddon.proxy.registerToolPartModel(part);
        TinkerRegistry.registerStencilTableCrafting(
                Pattern.setTagForPart(new ItemStack(TinkerTools.pattern), part));
        return part;
    }
}