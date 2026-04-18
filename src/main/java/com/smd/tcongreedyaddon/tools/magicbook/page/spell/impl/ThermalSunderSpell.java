package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.effect.ThermalSunderRuntime;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.GestureType;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IKeybindGestureSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;

import java.util.List;

public class ThermalSunderSpell extends AbstractSpell implements IKeybindGestureSpell {

    private static final ResourceLocation ICON = new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/burning_dash.png");

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.thermal_sunder")
            .icon(ICON)
            .selectable(false)
            .renderInOverlay(true)
            .cooldown(0)
            .castActionTicks(3)
            .build();

    public ThermalSunderSpell() {
        super(BLUEPRINT);
    }

    @Override
    protected boolean canTriggerInternal(SpellContext context) {
        return context.slot == MagicPageItem.SlotType.LEFT
                && context.trigger.isType(TriggerSource.Type.KEY_GESTURE)
                && context.gesture != null;
    }

    @Override
    protected boolean executeInternal(SpellContext context) {
        return false;
    }

    @Override
    public boolean supportsGesture(MagicPageItem.SlotType slotType, GestureType gesture) {
        if (slotType != MagicPageItem.SlotType.LEFT || gesture == null) {
            return false;
        }
        return gesture == GestureType.TAP_A || gesture == GestureType.LONG_A;
    }

    @Override
    public GestureResult onGestureTriggered(SpellContext context, GestureType gesture, boolean onCooldown) {
        if (context.world.isRemote) {
            return GestureResult.PASS;
        }

        boolean heatMode;
        if (gesture == GestureType.TAP_A) {
            heatMode = true;
        } else if (gesture == GestureType.LONG_A) {
            heatMode = false;
        } else {
            return GestureResult.PASS;
        }

        List<EntityLivingBase> targets = findHostileTargets(context.player, context.getRange());
        for (EntityLivingBase target : targets) {
            if (heatMode) {
                ThermalSunderRuntime.applyHeat(context.player, context.bookStack, target);
            } else {
                ThermalSunderRuntime.applyCold(target);
            }
            ThermalSunderRuntime.tryFusionDetonation(context.player, target);
        }

        sendModeActionbar(context, heatMode);
        spawnModeParticleRing(context, heatMode);
        return GestureResult.SUCCESS_NO_COOLDOWN;
    }

    private List<EntityLivingBase> findHostileTargets(EntityLivingBase caster, float range) {
        float searchRange = Math.max(1.0F, range);
        AxisAlignedBB area = caster.getEntityBoundingBox().grow(searchRange);
        return caster.world.getEntitiesWithinAABB(EntityLivingBase.class, area,
                entity -> entity != caster
                        && entity.isEntityAlive()
                        && entity instanceof IMob);
    }

    private void sendModeActionbar(SpellContext context, boolean heatMode) {
        String key = heatMode ? "message.gauss_thermal.mode_heat" : "message.gauss_thermal.mode_cold";
        context.player.sendStatusMessage(new TextComponentTranslation(key), true);
    }

    private void spawnModeParticleRing(SpellContext context, boolean heatMode) {
        if (!(context.world instanceof WorldServer)) {
            return;
        }
        WorldServer worldServer = (WorldServer) context.world;
        double radius = Math.max(1.0D, Math.min(16.0D, context.getRange()));
        int points = Math.max(24, (int) (radius * 8.0D));

        spawnCircle(worldServer, context.player.posX, context.player.posY + 0.1D, context.player.posZ,
                radius, points, heatMode);
        spawnCircle(worldServer, context.player.posX, context.player.posY + 0.1D, context.player.posZ,
                radius * 0.6D, Math.max(16, points / 2), heatMode);
    }

    private void spawnCircle(WorldServer worldServer, double centerX, double centerY, double centerZ,
                             double radius, int points, boolean heatMode) {
        double red = heatMode ? 1.0D : 0.2D;
        double green = heatMode ? 0.2D : 0.55D;
        double blue = heatMode ? 0.1D : 1.0D;

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            worldServer.spawnParticle(EnumParticleTypes.REDSTONE, x, centerY, z, 0, red, green, blue, 1.0D);
        }
    }
}
