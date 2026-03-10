package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;

public class RangePulseSpell extends AbstractSpell {

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.range_pulse")
            .icon(new ResourceLocation("minecraft", "textures/item/fire_charge.png"))
            .cooldown(60)
            .build();

    public RangePulseSpell() {
        super(BLUEPRINT);
    }

    @Override
    protected boolean canTriggerInternal(SpellContext context) {
        return context.trigger.isType(TriggerSource.Type.RIGHT_CLICK)
                && context.slot == MagicPageItem.SlotType.RIGHT;
    }

    @Override
    protected boolean executeInternal(SpellContext context) {
        if (context.world.isRemote) {
            return true;
        }
        float range = Math.max(1f, context.getRange());
        int spellSpeed = Math.max(1, context.getSpellSpeed());
        boolean empowered = context.world.rand.nextFloat() < context.getCritChance();
        float baseDamage = 2.5f + spellSpeed;
        AxisAlignedBB area = context.player.getEntityBoundingBox().grow(range);
        for (EntityLivingBase target : context.world.getEntitiesWithinAABB(EntityLivingBase.class,
                area, entity -> entity != context.player && entity.isEntityAlive())) {
            float hitDamage = baseDamage + (empowered ? spellSpeed : 0);
            target.attackEntityFrom(DamageSource.causePlayerDamage(context.player), hitDamage);
            if (empowered) {
                target.setFire(4);
            }
        }
        context.player.sendMessage(new TextComponentString(empowered ? "Range pulse bursts!" : "Range pulse ripples."));
        return true;
    }

    @Override
    protected int computeCooldownTicks(EntityPlayer player, ItemStack bookStack) {
        return Math.max(30, Math.round(player.getHealth() * 3));
    }
}
