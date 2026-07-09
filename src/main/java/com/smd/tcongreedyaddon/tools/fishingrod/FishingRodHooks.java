package com.smd.tcongreedyaddon.tools.fishingrod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import java.util.Collections;
import java.util.List;

public final class FishingRodHooks {

    private static final String TAG_HOOK_HIT_SPEED = "tcongreedyaddon_hook_hit_speed";

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

    public static void recordHitSpeed(EntityFishHook hook) {
        hook.getEntityData().setDouble(TAG_HOOK_HIT_SPEED, getMotionSpeed(hook));
    }

    public static double getHitSpeed(EntityFishHook hook) {
        return hook.getEntityData().getDouble(TAG_HOOK_HIT_SPEED);
    }

    private static double getMotionSpeed(Entity entity) {
        return Math.sqrt(entity.motionX * entity.motionX
                + entity.motionY * entity.motionY
                + entity.motionZ * entity.motionZ);
    }

    /**
     * 每 tick 调用，处理鱼钩钩住实体时的逻辑。
     * 现在首先发布 Forge 事件，然后仍执行旧的回调接口（用于兼容）。
     * 注意：仅服务端执行，客户端不触发。
     */
    public static void onHookedEntityTick(ItemStack tool, EntityPlayer player, EntityFishHook hook, Entity target) {
        if (player.world.isRemote) {
            return;
        }

        FishingRodHookedEntityTickEvent event = new FishingRodHookedEntityTickEvent(player, tool, hook, target, hook.ticksExisted);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }

        for (IFishingRodHook callback : callbacks(tool)) {
            callback.onFishingRodHookedEntityTick(tool, player, hook, target);
        }
    }

    /**
     * 钓鱼收获战利品时的回调
     */
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
