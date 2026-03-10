package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class SmallFireballSpell extends AbstractSpell {

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.small_fire_ball")
            .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/burning_dash.png"))
            .cooldown(20)
            .build();

    public SmallFireballSpell() {
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
        EntitySmallFireball fireball = new EntitySmallFireball(context.world, context.player, 1, 1, 1);
        fireball.setPosition(
                context.player.posX + look.x * 1.5,
                context.player.posY + look.y * 1.5 + context.player.getEyeHeight(),
                context.player.posZ + look.z * 1.5
        );
        fireball.accelerationX = look.x * 0.3;
        fireball.accelerationY = look.y * 0.3;
        fireball.accelerationZ = look.z * 0.3;
        context.world.spawnEntity(fireball);
        context.player.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
        return true;
    }
}
