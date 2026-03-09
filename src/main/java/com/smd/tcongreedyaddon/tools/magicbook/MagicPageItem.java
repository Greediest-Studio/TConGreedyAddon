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

    // 修改：增加 pageStack 参数
    public boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target, NBTTagCompound pageData, ItemStack pageStack) {
        return false;
    }

    // 修改：增加 pageStack 参数
    public boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound modifierData, ItemStack pageStack) {
        return false;
    }

    // 修改：增加 pageStack 参数
    public void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData, SlotType slot, ItemStack pageStack) {
    }

    public int getInitialSpellIndex(ItemStack pageStack) {
        return 0;
    }

    // 可选：增加 pageStack 参数
    public String getCurrentSpellDisplayName(NBTTagCompound pageData, ItemStack pageStack) {
        return "Spell " + (pageData.getInteger("spellIndex") + 1);
    }

    // 可选：增加 pageStack 参数
    public List<String> getAllSpellNames(NBTTagCompound pageData, ItemStack pageStack) {
        return Collections.emptyList();
    }

    public abstract int getSpellCount(SlotType slotType);

    public abstract String getSpellDisplayName(int internalIndex, SlotType slotType);

    public abstract int getSpellCooldownTicks(int internalIndex, SlotType slotType);

}