package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.bulletapi.api.LaserApi;
import com.smd.bulletapi.api.handle.LaserHandle;
import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.event.FreezeRayControlManager;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IHoldTriggerSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import com.smd.tcongreedyaddon.util.MagicBookHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeRaySpell extends AbstractSpell implements IHoldTriggerSpell {

    private static final float DAMAGE_MULTIPLIER = 0.5F;
    private static final float LASER_THICKNESS = 0.3F;
    private static final int MAX_HOLD_TICKS = 72000;
    private static final Map<UUID, ActiveLaserState> ACTIVE_LASERS = new ConcurrentHashMap<>();

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.freeze_ray")
            .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/6.png"))
            .cooldown(0)
            .build();

    public FreezeRaySpell() {
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

    @Override
    public int getTriggerStartTicks(SpellContext context) {
        return  0;
    }

    @Override
    public int getMaxHoldTicks(SpellContext context) {
        return MAX_HOLD_TICKS;
    }

    @Override
    public boolean onHoldTriggerTick(SpellContext context, int heldTicks) {
        if (context.world.isRemote || !(context.bookStack.getItem() instanceof MagicBook)) {
            return false;
        }

        int laserId = ensureLaser(context);
        if (laserId < 0) {
            return false;
        }

        FreezeRayControlManager.markLaserActive(context.world, laserId);
        return true;
    }

    @Override
    public void onHoldEnd(SpellContext context, int heldTicks, boolean interrupted) {
        if (context.world.isRemote) {
            return;
        }
        cleanupLaser(context);
    }

    private int ensureLaser(SpellContext context) {
        ActiveLaserState activeLaser = ACTIVE_LASERS.get(context.player.getUniqueID());
        int laserId = activeLaser == null ? -1 : activeLaser.laserId;
        LaserHandle handle = laserId >= 0 ? LaserApi.handle(context.world, laserId) : null;
        if (handle != null && handle.exists()) {
            return laserId;
        }

        cleanupLaser(context);

        ItemStack bookStack = context.bookStack;
        MagicBook magicBook = (MagicBook) bookStack.getItem();
        laserId = LaserApi.builder(context.world)
                .maxLength(context.getRange())
                .thickness(LASER_THICKNESS)
                .life(-1)
                .damage(0.0F)
                .color(0x3399FF)
                .rendererType("laser_poly")
                .startOffsetLocal(new Vec3d(0.2D, -0.18D, 0.45D))
                .set("alpha", 0.78F)
                .set("poly_sides", 8)
                .set("core_scale", 0.24F)
                .set("shell_scale", 0.62F)
                .set("pulse_amp", 0.06F)
                .set("pulse_speed", 0.2F)
                .set("core_color", 0x4DA6FF)
                .set("shell_color", 0x3399FF)
                .set("shell_color_end", 0x1E5FFF)
                .set("block_len", 2.1F)
                .set("block_speed", 0.18F)
                .set("block_soft", true)
                .set("block_apply_core", false)
                .set("block_color_a", 0x3399FF)
                .set("block_color_b", 0x1E5FFF)
                .set("twist_speed", 0.35F)
                .set("twist_step", 0.2F)
                .set("jitter_amp", 0.0F)
                .set("jitter_freq", 0.0F)
                .set("deco_on", true)
                .set("deco_scale", 1.45F)
                .set("deco_alpha", 0.22F)
                .set("deco_step", 1.1F)
                .set("deco_scroll", 0.04F)
                .set("deco_rot_speed", 0.08F)
                .set("deco_color", 0x66B8FF)
                .penetrate(false)
                .followShooter(true)
                .blockStops(true)
                .eventIntervalTicks(0)
                .shooter(context.player)
                .shooterHeldItem(bookStack)
                .hitBehavior(ctx -> {
                    EntityLivingBase target = ctx.hitEntity;
                    FreezeRayControlManager.markHit(target);
                    ctx.canceled = true;
                    MagicBookHelper.attackEntityRight(
                            bookStack,
                            magicBook,
                            context.player,
                            target,
                            DAMAGE_MULTIPLIER,
                            DamageSource.causePlayerDamage(context.player)
                    );
                })
                .spawn();

        ACTIVE_LASERS.put(context.player.getUniqueID(),
                new ActiveLaserState(context.world.provider.getDimension(), laserId));
        return laserId;
    }

    private void cleanupLaser(SpellContext context) {
        if (context == null || context.world == null || context.world.isRemote) {
            return;
        }

        ActiveLaserState activeLaser = ACTIVE_LASERS.remove(context.player.getUniqueID());
        int laserId = activeLaser == null ? -1 : activeLaser.laserId;
        int dimension = activeLaser == null ? context.world.provider.getDimension() : activeLaser.dimension;
        if (laserId >= 0 && dimension == context.world.provider.getDimension()) {
            LaserHandle handle = LaserApi.handle(context.world, laserId);
            if (handle.exists()) {
                handle.remove();
            }
            FreezeRayControlManager.unregisterLaser(context.world, laserId);
        }
    }

    private static final class ActiveLaserState {
        private final int dimension;
        private final int laserId;

        private ActiveLaserState(int dimension, int laserId) {
            this.dimension = dimension;
            this.laserId = laserId;
        }
    }
}
