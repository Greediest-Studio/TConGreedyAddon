package com.smd.tcongreedyaddon.tools.fishingrod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IFishingRodHook {

    default void onFishingRodHookedEntityTick(ItemStack tool, EntityPlayer player, EntityFishHook hook, Entity target) {
    }

    default void onFishingRodLoot(ItemStack tool, EntityPlayer player, EntityFishHook hook, List<ItemStack> loot) {
    }

}
