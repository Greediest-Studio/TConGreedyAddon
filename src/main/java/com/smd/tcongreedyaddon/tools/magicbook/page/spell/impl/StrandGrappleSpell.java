package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.event.StrandConnectionManager;
import com.smd.tcongreedyaddon.event.StrandNodeManager;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IKeybindSkillSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class StrandGrappleSpell extends AbstractSpell implements IKeybindSkillSpell {
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
        return context.slot == MagicPageItem.SlotType.RIGHT
                && (context.trigger.isType(TriggerSource.Type.SKILL_PRESS)
                || context.trigger.isType(TriggerSource.Type.SKILL_RELEASE));
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
    public KeybindResult onKeybindTriggered(SpellContext context, KeyAction action, boolean onCooldown) {
        if (context.world.isRemote) {
            return KeybindResult.PASS;
        }
        if (action == KeyAction.RELEASE) {
            return StrandConnectionManager.disconnect(context.player, true)
                    ? KeybindResult.SUCCESS_NO_COOLDOWN
                    : KeybindResult.PASS;
        }
        if (StrandConnectionManager.hasReuseCooldown(context.player)) {
            return KeybindResult.PASS;
        }
        if (StrandConnectionManager.hasConnection(context.player)) {
            return KeybindResult.PASS;
        }

        StrandNodeManager.StrandNode reusable = StrandNodeManager.findReusableNode(
                context.world, context.player, context.getRange());
        if (reusable != null) {
            StrandNodeManager.refreshNode(context.world, reusable);
            boolean connected = StrandConnectionManager.connect(
                    context.player, context.bookStack, reusable, context.getRange(), context.getSpellSpeed(), true);
            return connected ? KeybindResult.SUCCESS_NO_COOLDOWN : KeybindResult.PASS;
        }

        if (onCooldown) {
            return KeybindResult.PASS;
        }

        Vec3d anchorPos = resolveAnchorPos(context);
        StrandNodeManager.StrandNode node = StrandNodeManager.createNode(context.world, context.player, anchorPos);
        if (node == null) {
            return KeybindResult.PASS;
        }

        boolean connected = StrandConnectionManager.connect(
                context.player, context.bookStack, node, context.getRange(), context.getSpellSpeed(), false);
        return connected ? KeybindResult.SUCCESS_APPLY_COOLDOWN : KeybindResult.PASS;
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
