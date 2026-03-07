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

    public boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target) {
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
}