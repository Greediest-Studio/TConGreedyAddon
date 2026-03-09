package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * 法术执行时的上下文信息。
 */
public class SpellContext {
    public final World world;
    public final EntityPlayer player;
    public final ItemStack bookStack;      // 魔导书物品
    public final ItemStack pageStack;       // 当前书页物品
    public final NBTTagCompound pageData;   // 书页的 NBT 数据
    public final MagicPageItem.SlotType slot; // 书页所在的槽位（左/右）
    public final TriggerSource trigger;      // 触发源
    @Nullable public final Entity target;    // 仅左键点击时可能使用

    public SpellContext(World world, EntityPlayer player, ItemStack bookStack,
                        ItemStack pageStack, NBTTagCompound pageData,
                        MagicPageItem.SlotType slot, TriggerSource trigger,
                        @Nullable Entity target) {
        this.world = world;
        this.player = player;
        this.bookStack = bookStack;
        this.pageStack = pageStack;
        this.pageData = pageData;
        this.slot = slot;
        this.trigger = trigger;
        this.target = target;
    }
}