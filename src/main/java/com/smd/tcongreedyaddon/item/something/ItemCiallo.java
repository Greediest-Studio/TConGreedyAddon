package com.smd.tcongreedyaddon.item.something;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.Tags;
import net.minecraft.item.Item;

public class ItemCiallo extends Item {

    public ItemCiallo() {
        setRegistryName(Tags.MOD_ID, "ciallo");
        setTranslationKey(Tags.MOD_ID + ".ciallo");
        setCreativeTab(TConGreedyAddon.TAB);
        setMaxStackSize(1);
    }
}
