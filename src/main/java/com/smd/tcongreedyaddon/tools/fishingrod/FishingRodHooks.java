package com.smd.tcongreedyaddon.tools.fishingrod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import java.util.Collections;
import java.util.List;

public final class FishingRodHooks {

    private FishingRodHooks() {
    }

    public static boolean isFishingRod(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof TinkerFishingRod;
    }

    public static ItemStack findRod(EntityPlayer player) {
        ItemStack main = player.getHeldItemMainhand();
        if (isFishingRod(main)) {
            return main;
        }

        ItemStack offhand = player.getHeldItemOffhand();
        return isFishingRod(offhand) ? offhand : ItemStack.EMPTY;
    }

    public static void onHookedEntityTick(ItemStack tool, EntityPlayer player, EntityFishHook hook, Entity target) {
        for (IFishingRodHook callback : callbacks(tool)) {
            callback.onFishingRodHookedEntityTick(tool, player, hook, target);
        }
    }

    public static void onLoot(ItemStack tool, EntityPlayer player, EntityFishHook hook, List<ItemStack> loot) {
        for (IFishingRodHook callback : callbacks(tool)) {
            callback.onFishingRodLoot(tool, player, hook, loot);
        }
    }

    private static Iterable<IFishingRodHook> callbacks(ItemStack tool) {
        if (tool.isEmpty()) {
            return Collections.emptyList();
        }

        java.util.ArrayList<IFishingRodHook> callbacks = new java.util.ArrayList<>();
        for (ITrait trait : TinkerUtil.getTraitsOrdered(tool)) {
            if (trait instanceof IFishingRodHook) {
                callbacks.add((IFishingRodHook) trait);
            }
        }
        for (IModifier modifier : TinkerUtil.getModifiers(tool)) {
            if (modifier instanceof IFishingRodHook && !callbacks.contains(modifier)) {
                callbacks.add((IFishingRodHook) modifier);
            }
        }
        return callbacks;
    }
}
