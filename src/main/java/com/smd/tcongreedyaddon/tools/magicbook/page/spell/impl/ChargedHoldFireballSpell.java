package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IHoldTriggerSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class ChargedHoldFireballSpell extends AbstractSpell implements IHoldTriggerSpell {

    private static final int CHARGE_TICKS = 100; // 5 seconds at 20 TPS
    private static final int MAX_SUSTAIN_TICKS = 100; // 5 seconds sustained fire after charging
    private static final int FIRE_INTERVAL = 4;

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.hold_fireball")
            .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/charge.png"))
            .cooldown(100)
            .build();

    public ChargedHoldFireballSpell() {
        super(BLUEPRINT);
    }

    @Override
    protected boolean canTriggerInternal(SpellContext context) {
        return (context.trigger.isType(TriggerSource.Type.HOLD_TICK) || context.trigger.isType(TriggerSource.Type.HOLD_RELEASE))
                && context.slot == MagicPageItem.SlotType.RIGHT;
    }

    @Override
    protected boolean executeInternal(SpellContext context) {
        return false;
    }

    public int getRequiredHoldTicks() {
        return CHARGE_TICKS;
    }

    public int getMaxSustainTicks() {
        return MAX_SUSTAIN_TICKS;
    }

    public int getFireIntervalTicks() {
        return FIRE_INTERVAL;
    }

    @Override
    public int getTriggerStartTicks(SpellContext context) {
        return getRequiredHoldTicks();
    }

    @Override
    public int getMaxHoldTicks(SpellContext context) {
        return getRequiredHoldTicks() + getMaxSustainTicks();
    }

    @Override
    public boolean onHoldTriggerTick(SpellContext context, int heldTicks) {
        if (context.world.isRemote) {
            return false;
        }

        int activeTicks = heldTicks - getRequiredHoldTicks();
        if (activeTicks < 0 || activeTicks % getFireIntervalTicks() != 0) {
            return false;
        }

        Vec3d look = context.player.getLookVec();
        EntitySmallFireball fireball = new EntitySmallFireball(context.world, context.player, look.x, look.y, look.z);
        fireball.setPosition(
                context.player.posX + look.x * 1.5,
                context.player.posY + context.player.getEyeHeight() + look.y * 1.5,
                context.player.posZ + look.z * 1.5
        );
        fireball.accelerationX = look.x * 0.35;
        fireball.accelerationY = look.y * 0.35;
        fireball.accelerationZ = look.z * 0.35;
        context.world.spawnEntity(fireball);

        if (activeTicks % 8 == 0) {
            context.player.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 0.8F, 1.1F);
        }
        return true;
    }
}
