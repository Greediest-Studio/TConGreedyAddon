package com.smd.tcongreedyaddon.tools.fishingrod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * 钓鱼竿钩住实体时的每帧更新事件（在服务端 tick 中触发）。
 * 监听此事件可以干预钩住实体的行为，如修改速度、施加效果等。
 */
@Cancelable
public class FishingRodHookedEntityTickEvent extends PlayerEvent {
    private final ItemStack fishingRod;
    private final EntityFishHook hook;
    private final Entity target;
    private final int ticksExisted;

    public FishingRodHookedEntityTickEvent(EntityPlayer player, ItemStack rod, EntityFishHook hook, Entity target, int ticks) {
        super(player);
        this.fishingRod = rod;
        this.hook = hook;
        this.target = target;
        this.ticksExisted = ticks;
    }

    public ItemStack getFishingRod() {
        return fishingRod;
    }

    public EntityFishHook getHook() {
        return hook;
    }

    public Entity getTarget() {
        return target;
    }

    /**
     * 鱼钩已存在的 tick 数（从抛出开始计算）。
     */
    public int getTicksExisted() {
        return ticksExisted;
    }

    /**
     * 检查是否应该在此 tick 执行逻辑（例如每 5 tick 一次）。
     * 实际判断由监听者自行决定，这里只是提供工具方法。
     */
    public boolean shouldExecute(int interval) {
        return ticksExisted % interval == 0;
    }
}