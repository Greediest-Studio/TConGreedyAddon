package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.Tags;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public class ModelRegistrationHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        TConGreedyAddon.modulemanager.onModelRegistry(event);
    }
}
