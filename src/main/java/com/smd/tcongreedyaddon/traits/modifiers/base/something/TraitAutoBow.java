package com.smd.tcongreedyaddon.traits.modifiers.base.something;

import com.smd.tcongreedyaddon.plugin.something.something;
import com.smd.tcongreedyaddon.traits.ITraitBookProvider;
import com.smd.tcongreedyaddon.util.BookContentBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.book.content.ContentModifier;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.tools.ranged.BowCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

public class TraitAutoBow extends ModifierTrait implements ITraitBookProvider {

    public TraitAutoBow() {
        super("autobow", 0xFFAA00);
    }

    public void initItem() {
        addItem(something.bowClicker);
    }

    @Override
    public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {

        if (world.isRemote) {
            return;
        }

        if (!isSelected) {
            return;
        }

        if (!(entity instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase holder = (EntityLivingBase) entity;

        if (!holder.isHandActive() || holder.getActiveItemStack() != tool) {
            return;
        }

        if (ToolHelper.isBroken(tool)) {
            return;
        }

        if (!(tool.getItem() instanceof BowCore)) {
            return;
        }

        BowCore bow = (BowCore) tool.getItem();

        float progress = bow.getDrawbackProgress(tool, holder);
        if (progress >= 1.0f) {

            EnumHand hand = holder.getActiveHand();

            holder.stopActiveHand();

            if (ToolHelper.isBroken(tool)) {
                return;
            }

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

    @Override
    public ContentModifier getBookContent() {
        return BookContentBuilder.create(this.getIdentifier())
                .addText("自动拉弓")
                .addEffect("蓄满力会自动射击,而不是继续维持拉满的状态")
                .withBowTools()
                .build();
    }
}
