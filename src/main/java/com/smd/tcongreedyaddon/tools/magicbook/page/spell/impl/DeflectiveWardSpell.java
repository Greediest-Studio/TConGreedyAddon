package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class DeflectiveWardSpell extends AbstractSpell {

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.deflective_ward")
            .icon(new ResourceLocation("minecraft", "textures/item/iron_axe.png"))
            .selectable(false)
            .renderInOverlay(true)
            .cooldown(40)
            .listeningEvents(LivingAttackEvent.class)
            .build();

    public DeflectiveWardSpell() {
        super(BLUEPRINT);
    }

    @Override
    protected boolean canTriggerInternal(SpellContext context) {
        return context.trigger.isEvent()
                && context.trigger.getEvent() instanceof LivingAttackEvent
                && context.slot == MagicPageItem.SlotType.RIGHT;
    }

    @Override
    protected boolean executeInternal(SpellContext context) {
        if (context.world.isRemote) {
            return true;
        }
        LivingAttackEvent attackEvent = (LivingAttackEvent) context.trigger.getEvent();
        if (attackEvent == null || attackEvent.getSource() == null) {
            return true;
        }
        Entity source = attackEvent.getSource().getTrueSource();
        if (!(source instanceof EntityLivingBase)) {
            return true;
        }
        EntityLivingBase attacker = (EntityLivingBase) source;
        float slotPower = context.getCurrentSlotCount();
        float knockbackStrength = 0.5f + slotPower * 0.2f;
        attacker.attackEntityFrom(DamageSource.causePlayerDamage(context.player), 1.5f * slotPower);
        attacker.knockBack(context.player, knockbackStrength,
                context.player.posX - attacker.posX, context.player.posZ - attacker.posZ);
        context.player.sendMessage(new TextComponentString("Ward deflects for " + slotPower + " power."));
        return true;
    }
}
