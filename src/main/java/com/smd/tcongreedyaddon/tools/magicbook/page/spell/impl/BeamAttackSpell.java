package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.util.List;

public class BeamAttackSpell extends AbstractSpell {

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("beam_attack")
            .icon(new ResourceLocation("minecraft", "textures/items/fireball.png"))
            .cooldown(20)
            .build();

    public BeamAttackSpell() {
        super(BLUEPRINT);
    }

    @Override
    protected boolean canTriggerInternal(SpellContext context) {
        return context.trigger.isType(TriggerSource.Type.RIGHT_CLICK) &&
                context.slot == MagicPageItem.SlotType.RIGHT;
    }

    @Override
    protected boolean executeInternal(SpellContext context) {
        if (context.world.isRemote) {
            return true;
        }
        float range = MagicBook.getBeamRangeFromBook(context.bookStack);
        AxisAlignedBB aabb = context.player.getEntityBoundingBox().grow(range);
        List<EntityLivingBase> targets = context.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb,
                entity -> entity != context.player && entity.isEntityAlive());

        float baseDamage = ToolHelper.getActualAttack(context.bookStack);
        for (EntityLivingBase target : targets) {
            target.attackEntityFrom(DamageSource.causePlayerDamage(context.player), baseDamage);
        }
        return true;
    }
}
