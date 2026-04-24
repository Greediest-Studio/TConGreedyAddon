package com.smd.tcongreedyaddon.tools.magicbook.page.spell;

import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public final class SpellTimingManager {

    private static final String TAG_COOLDOWNS = "cooldowns";
    private static final String TAG_ACTION_LOCKS = "actionLocks";

    public static boolean isOnCooldown(ISpell spell, int rawIndex,
                                       ItemStack pageStack,
                                       World world,
                                       EntityPlayer player,
                                       ItemStack bookStack) {
        int cooldownTicks = spell.getCooldownTicks(player, bookStack);
        if (cooldownTicks <= 0) return false;
        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) return false;
        NBTTagCompound cooldowns = pageData.getCompoundTag(TAG_COOLDOWNS);
        long lastUsed = cooldowns.getLong(String.valueOf(rawIndex));
        long now = world.getTotalWorldTime();
        return now - lastUsed < cooldownTicks;
    }

    public static void applyCooldown(ISpell spell, int rawIndex,
                                     ItemStack pageStack,
                                     World world,
                                     EntityPlayer player,
                                     ItemStack bookStack) {
        int cooldownTicks = spell.getCooldownTicks(player, bookStack);
        if (cooldownTicks <= 0) return;
        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) pageData = new NBTTagCompound();
        NBTTagCompound cooldowns = pageData.getCompoundTag(TAG_COOLDOWNS);
        cooldowns.setLong(String.valueOf(rawIndex), world.getTotalWorldTime());
        pageData.setTag(TAG_COOLDOWNS, cooldowns);
        pageStack.setTagCompound(pageData);
    }

    public static boolean isActionLocked(int rawIndex,
                                         NBTTagCompound pageData,
                                         long worldTick,
                                         int castActionTicks) {
        if (castActionTicks <= 0) return false;
        NBTTagCompound locks = pageData.getCompoundTag(TAG_ACTION_LOCKS);
        long lastCast = locks.getLong(String.valueOf(rawIndex));
        return worldTick < lastCast + castActionTicks;
    }

    public static void applyActionLock(int rawIndex,
                                       NBTTagCompound pageData,
                                       long worldTick,
                                       int castActionTicks) {
        if (castActionTicks <= 0) return;
        NBTTagCompound locks = pageData.getCompoundTag(TAG_ACTION_LOCKS);
        locks.setLong(String.valueOf(rawIndex), worldTick);
        pageData.setTag(TAG_ACTION_LOCKS, locks);
    }
}
