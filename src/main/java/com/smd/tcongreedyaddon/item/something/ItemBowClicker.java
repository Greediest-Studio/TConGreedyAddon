package com.smd.tcongreedyaddon.item.something;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.Tags;
import net.minecraft.item.Item;

public class ItemBowClicker extends Item {

    public ItemBowClicker() {
        setRegistryName(Tags.MOD_ID, "bow_clicker");
        setCreativeTab(TConGreedyAddon.TAB);
        setTranslationKey(Tags.MOD_ID + ".bow_clicker");
        setCreativeTab(TConGreedyAddon.TAB);
    }
}
