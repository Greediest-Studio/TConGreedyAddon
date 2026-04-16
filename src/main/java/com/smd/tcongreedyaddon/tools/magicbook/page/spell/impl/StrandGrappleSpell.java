package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.event.StrandConnectionManager;
import com.smd.tcongreedyaddon.event.StrandNodeManager;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.GestureType;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IKeybindGestureSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class StrandGrappleSpell extends AbstractSpell implements IKeybindGestureSpell {
    private static final double ANCHOR_OFFSET = 0.18D;
    private static final ResourceLocation GRAPPLE_ICON = new ResourceLocation("minecraft", "textures/items/fishing_rod_uncast.png");
    private static final ResourceLocation MELEE_ICON = new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/charge.png");

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.strand_grapple")
            .icon(GRAPPLE_ICON)
            .selectable(false)
            .renderInOverlay(true)
            .cooldown(60)
            .build();

    public StrandGrappleSpell() {
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
    public ResourceLocation getDisplayIcon(net.minecraft.nbt.NBTTagCompound pageData, int rawIndex) {
        return StrandConnectionManager.isClientMeleeReady() ? MELEE_ICON : GRAPPLE_ICON;
    }

    @Override
    public boolean supportsGesture(MagicPageItem.SlotType slotType, GestureType gesture) {
        if (slotType != MagicPageItem.SlotType.LEFT || gesture == null) {
            return false;
        }
        return gesture == GestureType.PRESS_A
                || gesture == GestureType.RELEASE_A
                || gesture == GestureType.PRESS_B
                || gesture == GestureType.TAP_B
                || gesture == GestureType.HOLD_A_TAP_B;
    }

    @Override
    public GestureResult onGestureTriggered(SpellContext context, GestureType gesture, boolean onCooldown) {
        if (context.world.isRemote) {
            return GestureResult.PASS;
        }

        if (gesture == GestureType.RELEASE_A) {
            return StrandConnectionManager.disconnect(context.player, false)
                    ? GestureResult.SUCCESS_NO_COOLDOWN
                    : GestureResult.PASS;
        }

        if (gesture == GestureType.PRESS_B
                || gesture == GestureType.TAP_B
                || gesture == GestureType.HOLD_A_TAP_B) {
            return StrandConnectionManager.tryStartMeleeAttack(context.player)
                    ? GestureResult.SUCCESS_NO_COOLDOWN
                    : GestureResult.PASS;
        }

        if (gesture != GestureType.PRESS_A) {
            return GestureResult.PASS;
        }

        if (StrandConnectionManager.hasConnection(context.player)) {
            return GestureResult.PASS;
        }
        if (StrandConnectionManager.hasReuseCooldown(context.player)) {
            return GestureResult.PASS;
        }

        StrandNodeManager.StrandNode reusable = StrandNodeManager.findReusableNode(
                context.world, context.player, context.getRange());
        if (reusable != null) {
            StrandNodeManager.refreshNode(context.world, reusable);
            boolean connected = StrandConnectionManager.connect(
                    context.player, context.bookStack, reusable, context.getRange(), context.getSpellSpeed(), true);
            return connected ? GestureResult.SUCCESS_NO_COOLDOWN : GestureResult.PASS;
        }

        if (onCooldown) {
            return GestureResult.PASS;
        }

        Vec3d anchorPos = resolveAnchorPos(context);
        StrandNodeManager.StrandNode node = StrandNodeManager.createNode(context.world, context.player, anchorPos);
        if (node == null) {
            return GestureResult.PASS;
        }

        boolean connected = StrandConnectionManager.connect(
                context.player, context.bookStack, node, context.getRange(), context.getSpellSpeed(), false);
        return connected ? GestureResult.SUCCESS_APPLY_COOLDOWN : GestureResult.PASS;
    }

    private Vec3d resolveAnchorPos(SpellContext context) {
        RayTraceResult rayTrace = context.player.rayTrace(context.getRange(), 1.0F);
        if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && rayTrace.sideHit != null) {
            Vec3i normal = rayTrace.sideHit.getDirectionVec();
            return rayTrace.hitVec.add(new Vec3d(
                    normal.getX() * ANCHOR_OFFSET,
                    normal.getY() * ANCHOR_OFFSET,
                    normal.getZ() * ANCHOR_OFFSET
            ));
        }

        Vec3d eye = context.player.getPositionEyes(1.0F);
        Vec3d look = context.player.getLookVec().normalize();
        return eye.add(look.scale(context.getRange()));
    }
}
