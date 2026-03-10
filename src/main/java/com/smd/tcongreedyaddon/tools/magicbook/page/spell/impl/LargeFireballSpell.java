package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class LargeFireballSpell extends AbstractSpell {

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.large_fire_ball")
            .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/charge.png"))
            .cooldown(160)
            .build();

    public LargeFireballSpell() {
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
        Vec3d look = context.player.getLookVec();
        EntityLargeFireball fireball = new EntityLargeFireball(context.world, context.player, look.x, look.y, look.z);
        fireball.setPosition(
                context.player.posX + look.x * 1.5,
                context.player.posY + look.y * 1.5 + context.player.getEyeHeight(),
                context.player.posZ + look.z * 1.5
        );
        fireball.accelerationX = look.x * 0.1;
        fireball.accelerationY = look.y * 0.1;
        fireball.accelerationZ = look.z * 0.1;
        context.world.spawnEntity(fireball);
        context.player.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
        return true;
    }
}
