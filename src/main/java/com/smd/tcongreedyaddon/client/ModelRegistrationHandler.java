package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.plugin.SpecialWeapons.SpecialWeapons;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public class ModelRegistrationHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerItemModel(SpecialWeapons.fireballPage);
        registerItemModel(SpecialWeapons.freezeRayPage);
    }

    private static void registerItemModel(Item item) {
        if (item == null) {
            return;
        }
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
