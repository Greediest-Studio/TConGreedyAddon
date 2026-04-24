package com.smd.tcongreedyaddon.init;

import com.smd.tcongreedyaddon.plugin.ModuleManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tools.Pattern;
import slimeknights.tconstruct.library.tools.ToolCore;
import com.smd.tcongreedyaddon.TConGreedyAddon;
import slimeknights.tconstruct.library.tools.ToolPart;
import slimeknights.tconstruct.tools.TinkerTools;

@Mod.EventBusSubscriber
public class TotalTinkersRegister {

    @SubscribeEvent
    public static void initItems(RegistryEvent.Register<Item> event) {
        ModuleManager.initItems(event);
    }

    public static void initForgeTool(ToolCore core, RegistryEvent.Register<Item> event) {
        event.getRegistry().register(core);
        TinkerRegistry.registerToolForgeCrafting(core);
        TConGreedyAddon.proxy.registerToolModel(core);
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