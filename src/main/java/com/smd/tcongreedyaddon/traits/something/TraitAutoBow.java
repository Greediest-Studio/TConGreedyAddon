package com.smd.tcongreedyaddon.traits.something;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.tools.ranged.BowCore;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.ToolHelper;

public class TraitAutoBow extends AbstractTrait {

    public TraitAutoBow() {
        super("autobow", 0xFFAA00);
    }

    @Override
    public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {

        if (world.isRemote) return;

        if (!isSelected) return;

        if (!(entity instanceof EntityLivingBase)) return;

        EntityLivingBase holder = (EntityLivingBase) entity;

        if (!holder.isHandActive() || holder.getActiveItemStack() != tool) return;

        if (ToolHelper.isBroken(tool)) return;

        if (!(tool.getItem() instanceof BowCore)) return;

        BowCore bow = (BowCore) tool.getItem();

        float progress = bow.getDrawbackProgress(tool, holder);
        if (progress >= 1.0f) {

            EnumHand hand = holder.getActiveHand();

            holder.stopActiveHand();

            if (ToolHelper.isBroken(tool)) return;

            boolean hasAmmo;
            if (holder instanceof EntityPlayer && ((EntityPlayer) holder).isCreative()) {
                hasAmmo = true;
            } else {
                ItemStack ammo = bow.findAmmo(tool, holder);
                hasAmmo = !ammo.isEmpty();
            }

            if (hasAmmo) {
                holder.setActiveHand(hand);
            }
        }
    }
}
