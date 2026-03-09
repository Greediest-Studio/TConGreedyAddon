package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IPassiveSpell {
    void onHeldUpdate(World world, EntityPlayer player, ItemStack toolStack, NBTTagCompound pageData);
    int getInterval();
    boolean runOnClient();
    String getNameKey();
}