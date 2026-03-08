package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public interface IRightClickSpell {
    String getNameKey();
    int getCooldownTicks();
    ResourceLocation getIcon();
    boolean onRightClick(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData);
}
