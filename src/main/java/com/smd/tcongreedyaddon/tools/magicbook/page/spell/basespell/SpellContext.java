package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBookToolNBT;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.GestureType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SpellContext {
    public final World world;
    public final EntityPlayer player;
    public final ItemStack bookStack;
    public final ItemStack pageStack;
    public final NBTTagCompound pageData;
    public final MagicPageItem.SlotType slot;
    public final TriggerSource trigger;
    @Nullable public final GestureType gesture;
    @Nullable public final Entity target;

    private MagicBookToolNBT cachedToolStats;

    public SpellContext(World world, EntityPlayer player, ItemStack bookStack,
                        ItemStack pageStack, NBTTagCompound pageData,
                        MagicPageItem.SlotType slot, TriggerSource trigger,
                        @Nullable Entity target) {
        this(world, player, bookStack, pageStack, pageData, slot, trigger, target, null);
    }

    public SpellContext(World world, EntityPlayer player, ItemStack bookStack,
                        ItemStack pageStack, NBTTagCompound pageData,
                        MagicPageItem.SlotType slot, TriggerSource trigger,
                        @Nullable Entity target, @Nullable GestureType gesture) {
        this.world = world;
        this.player = player;
        this.bookStack = bookStack;
        this.pageStack = pageStack;
        this.pageData = pageData;
        this.slot = slot;
        this.trigger = trigger;
        this.target = target;
        this.gesture = gesture;
    }

    public float getRange() {
        return getToolStats().range;
    }

    public float getCritChance() {
        return getToolStats().critChance;
    }

    public int getSpellSpeed() {
        return getToolStats().spellSpeed;
    }

    public int getLeftSlotCount() {
        return getToolStats().leftSlots;
    }

    public int getRightSlotCount() {
        return getToolStats().rightSlots;
    }

    public boolean isLeftSlot() {
        return slot == MagicPageItem.SlotType.LEFT;
    }

    public boolean isRightSlot() {
        return slot == MagicPageItem.SlotType.RIGHT;
    }

    public int getCurrentSlotCount() {
        return isLeftSlot() ? getLeftSlotCount() : getRightSlotCount();
    }

    private MagicBookToolNBT getToolStats() {
        if (cachedToolStats == null) {
            cachedToolStats = MagicBookToolNBT.from(bookStack);
        }
        return cachedToolStats;
    }
}
