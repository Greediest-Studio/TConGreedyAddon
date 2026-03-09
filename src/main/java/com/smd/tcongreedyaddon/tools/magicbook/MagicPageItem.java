package com.smd.tcongreedyaddon.tools.magicbook;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.TinkerRegistry;

import java.util.Collections;
import java.util.List;

public abstract class MagicPageItem extends Item {

    public static final String TAG_COOLDOWNS = "cooldowns";
    public static final String TAG_LAST_USED_PREFIX = "lastUsed_";

    public MagicPageItem() {
        setCreativeTab(TinkerRegistry.tabParts);
        setMaxStackSize(1);
    }

    public enum SlotType {
        LEFT,
        RIGHT
    }

    public abstract SlotType getSlotType();

    public String getPageIdentifier() {
        if (getRegistryName() == null) {
            throw new IllegalStateException("Page item not registered yet!");
        }
        return getRegistryName().toString();
    }

    public int getSpellCooldownTicks(int spellIndex) {
        return 0;
    }

    public boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target, NBTTagCompound pageData) {
        return false;
    }

    public boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound modifierData) {
        return false;
    }

    public void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData, SlotType slot) {
    }

    public void nextSpell(ItemStack toolStack, NBTTagCompound modifierData) {}

    public int getInitialSpellIndex(ItemStack pageStack) {
        return 0;
    }

    public String getCurrentSpellDisplayName(NBTTagCompound pageData) {
        return "Spell " + (pageData.getInteger("spellIndex") + 1);
    }

    public List<String> getAllSpellNames(NBTTagCompound pageData) {
        return Collections.emptyList();
    }


    public abstract int getSpellCount(SlotType slotType);

    public abstract String getSpellDisplayName(int internalIndex, SlotType slotType);

    public abstract int getSpellCooldownTicks(int internalIndex, SlotType slotType);

    /**
     * 检查指定法术是否处于冷却中
     * @param pageStack     当前书页物品
     * @param internalIndex 法术在页面内的索引
     * @param world         世界（用于获取当前时间）
     * @return true 如果冷却尚未结束
     */
    public boolean isSpellOnCooldown(ItemStack pageStack, int internalIndex, World world) {
        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) return false;

        NBTTagCompound cooldowns = pageData.getCompoundTag(TAG_COOLDOWNS);
        long lastUsed = cooldowns.getLong(String.valueOf(internalIndex));
        int cooldownTicks = getSpellCooldownTicks(internalIndex, getSlotType());
        if (cooldownTicks <= 0) return false;

        long now = world.getTotalWorldTime();
        return now - lastUsed < cooldownTicks;
    }

    /**
     * 设置法术的冷却开始时间（当前时间）
     * @param pageStack     当前书页物品
     * @param internalIndex 法术在页面内的索引
     * @param world         世界
     */
    public void setSpellCooldown(ItemStack pageStack, int internalIndex, World world) {
        int cooldownTicks = getSpellCooldownTicks(internalIndex, getSlotType());
        if (cooldownTicks <= 0) return;

        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) pageData = new NBTTagCompound();

        NBTTagCompound cooldowns = pageData.getCompoundTag(TAG_COOLDOWNS);
        cooldowns.setLong(String.valueOf(internalIndex), world.getTotalWorldTime());
        pageData.setTag(TAG_COOLDOWNS, cooldowns);
        pageStack.setTagCompound(pageData);
    }

    /**
     * 获取指定法术剩余的冷却刻数（0 表示可用）
     * @param pageStack     当前书页物品
     * @param internalIndex 法术在页面内的索引
     * @param world         世界
     * @return 剩余冷却刻数，如果未冷却则返回 0
     */
    public int getSpellCooldownRemaining(ItemStack pageStack, int internalIndex, World world) {
        NBTTagCompound pageData = pageStack.getTagCompound();
        if (pageData == null) return 0;

        NBTTagCompound cooldowns = pageData.getCompoundTag(TAG_COOLDOWNS);
        long lastUsed = cooldowns.getLong(String.valueOf(internalIndex));
        int cooldownTicks = getSpellCooldownTicks(internalIndex, getSlotType());
        if (cooldownTicks <= 0) return 0;

        long now = world.getTotalWorldTime();
        long elapsed = now - lastUsed;
        return elapsed >= cooldownTicks ? 0 : (int)(cooldownTicks - elapsed);
    }
}