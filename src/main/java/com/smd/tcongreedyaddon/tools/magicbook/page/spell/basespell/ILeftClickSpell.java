package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface ILeftClickSpell {
    String getNameKey();
    int getCooldownTicks();
    ResourceLocation getIcon();
    boolean onLeftClick(ItemStack toolStack, EntityPlayer player, Entity target);
}
