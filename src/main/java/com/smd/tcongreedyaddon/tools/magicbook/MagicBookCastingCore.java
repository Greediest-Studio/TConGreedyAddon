package com.smd.tcongreedyaddon.tools.magicbook;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.event.SpellUseEvent;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.GestureType;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindAction;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindChannel;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindSide;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IChannelReleaseSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IHoldTriggerSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IKeybindGestureSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.tconstruct.library.utils.ToolHelper;

import javax.annotation.Nullable;

final class MagicBookCastingCore {

    private final MagicBook book;
    private final MagicBookStateHelper stateHelper;

    MagicBookCastingCore(MagicBook book, MagicBookStateHelper stateHelper) {
        this.book = book;
        this.stateHelper = stateHelper;
    }

    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity target) {
        if (player.isSneaking() || ToolHelper.isBroken(stack)) {
            return true;
        }
        castSelectedSpell(stack, player, MagicPageItem.SlotType.LEFT, TriggerSource.leftClick(), target);
        return true;
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            if (!world.isRemote) {
                player.openGui(TConGreedyAddon.instance, 0, world, hand.ordinal(), 0, 0);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (ToolHelper.isBroken(stack)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        MagicBookStateHelper.ResolvedSpellTarget holdTarget = stateHelper.resolveSelectedHoldSpell(stack, MagicPageItem.SlotType.RIGHT);
        if (holdTarget != null) {
            MagicBookStateHelper.HoldCastModel model = stateHelper.getHoldCastModel(holdTarget.spell);
            if (model != null) {
                return beginHoldCast(world, player, hand, stack, holdTarget, model);
            }
        }

        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        castSelectedSpell(stack, player, MagicPageItem.SlotType.RIGHT, TriggerSource.rightClick(), null);
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        if (living.world.isRemote || !(living instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) living;
        if (!stateHelper.isStillUsingThisBook(player, stack, EnumHand.MAIN_HAND)) {
            stateHelper.clearHoldState(player, EnumHand.MAIN_HAND, player.world);
            return;
        }

        MagicBookStateHelper.HoldRuntimeState state = stateHelper.getMainHandHoldState(player, player.world);
        if (state == null) {
            return;
        }

        MagicBookStateHelper.ResolvedSpellTarget target = stateHelper.resolveHoldSpellFromState(stack, state);
        if (target == null) {
            stateHelper.clearHoldState(player, EnumHand.MAIN_HAND, player.world);
            return;
        }

        state.heldTicks = (int) Math.max(0L, player.world.getTotalWorldTime() - state.startWorldTick + 1L);
        SpellContext context = new SpellContext(
                player.world,
                player,
                stack,
                target.pageStack,
                target.pageData,
                MagicPageItem.SlotType.RIGHT,
                TriggerSource.holdTick(),
                null
        );

        switch (state.model) {
            case CHANNEL_RELEASE:
                castChannelTick(target, context, state);
                break;
            case HOLD_TRIGGER:
                castHoldTriggerTick(stack, player, target, context, state);
                break;
            default:
                break;
        }

        target.pageStack.setTagCompound(context.pageData);
        stateHelper.savePageData(stack, target);
    }

    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        if (world.isRemote) {
            if (living instanceof EntityPlayer) {
                stateHelper.clearHoldState((EntityPlayer) living, EnumHand.MAIN_HAND, world);
            }
            return;
        }
        if (!(living instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) living;
        MagicBookStateHelper.HoldRuntimeState state = stateHelper.getMainHandHoldState(player, world);
        if (state == null) {
            return;
        }

        MagicBookStateHelper.ResolvedSpellTarget target = stateHelper.resolveHoldSpellFromState(stack, state);
        if (target == null) {
            stateHelper.clearHoldState(player, EnumHand.MAIN_HAND, world);
            return;
        }

        SpellContext finishContext = new SpellContext(
                world,
                player,
                stack,
                target.pageStack,
                target.pageData,
                MagicPageItem.SlotType.RIGHT,
                TriggerSource.holdRelease(),
                null
        );

        switch (state.model) {
            case CHANNEL_RELEASE:
                finishChannelRelease(stack, player, target, finishContext, state);
                break;
            case HOLD_TRIGGER:
                finishHoldTrigger(stack, player, target, finishContext, state);
                break;
            default:
                break;
        }

        target.pageStack.setTagCompound(finishContext.pageData);
        stateHelper.savePageData(stack, target);
        stateHelper.clearHoldState(player, EnumHand.MAIN_HAND, world);
    }

    public boolean handleKeybindInput(ItemStack bookStack, EntityPlayer player, int sequence,
                                      KeybindSide side, KeybindChannel channel,
                                      KeybindAction action, int clientTick) {
        if (ToolHelper.isBroken(bookStack) || player == null || side == null || channel == null || action == null) {
            return false;
        }

        java.util.List<GestureType> gestures = stateHelper.consumeKeybindGestures(
                player,
                player.world,
                sequence,
                side,
                channel,
                action,
                clientTick
        );
        if (gestures.isEmpty()) {
            return false;
        }

        boolean anySuccess = false;
        for (GestureType gesture : gestures) {
            MagicBookStateHelper.ResolvedSpellTarget target = stateHelper.resolveGestureSpell(
                    bookStack,
                    side.toSlotType(),
                    gesture
            );
            if (target == null) {
                continue;
            }
            TriggerSource triggerSource = TriggerSource.keyGesture();
            if (!canCastKeybind(bookStack, player, target, triggerSource)) {
                continue;
            }
            boolean success = castKeybindGesture(bookStack, player, target, gesture, triggerSource);
            postSpellUsePost(player, bookStack, target, triggerSource, success);
            anySuccess |= success;
        }
        return anySuccess;
    }

    private boolean canCastSelectedSpell(ItemStack bookStack, EntityPlayer player,
                                         MagicBookStateHelper.ResolvedSpellTarget target,
                                         TriggerSource triggerSource) {
        return target != null
                && !ToolHelper.isBroken(bookStack)
                && postSpellUsePre(player, bookStack, target, triggerSource);
    }

    private boolean castSelectedSpell(ItemStack bookStack, EntityPlayer player, MagicPageItem.SlotType slotType,
                                      TriggerSource triggerSource, @Nullable Entity castTarget) {
        MagicBookStateHelper.ResolvedSpellTarget target = stateHelper.resolveSelectedSpellTarget(bookStack, slotType);
        if (!canCastSelectedSpell(bookStack, player, target, triggerSource)) {
            return false;
        }

        SpellContext context = new SpellContext(
                player.world,
                player,
                bookStack,
                target.pageStack,
                target.pageData,
                slotType,
                triggerSource,
                castTarget
        );

        boolean success = target.page.executeRawSpell(target.rawIndex, context);
        if (success) {
            stateHelper.savePageData(bookStack, target);
            ToolHelper.damageTool(bookStack, MagicBook.DURABILITY_COST, player);
        }
        postSpellUsePost(player, bookStack, target, triggerSource, success);
        return success;
    }

    private boolean canCastKeybind(ItemStack bookStack, EntityPlayer player,
                                   MagicBookStateHelper.ResolvedSpellTarget target,
                                   TriggerSource triggerSource) {
        return target != null
                && !ToolHelper.isBroken(bookStack)
                && postSpellUsePre(player, bookStack, target, triggerSource);
    }

    private boolean castKeybindGesture(ItemStack bookStack, EntityPlayer player,
                                       MagicBookStateHelper.ResolvedSpellTarget target,
                                       GestureType gesture,
                                       TriggerSource triggerSource) {
        if (!(target.spell instanceof IKeybindGestureSpell)) {
            return false;
        }

        boolean onCooldown = target.page.isRawSpellOnCooldown(
                target.pageStack, target.rawIndex, player.world, player, bookStack);
        int castActionTicks = resolveCastActionTicks(target.spell, player, bookStack);
        if (castActionTicks < 0) {
            return false;
        }
        if (isActionLocked(target.pageData, target.rawIndex, player.world.getTotalWorldTime(), castActionTicks)) {
            return false;
        }

        SpellContext context = new SpellContext(
                player.world,
                player,
                bookStack,
                target.pageStack,
                target.pageData,
                target.slotType,
                triggerSource,
                null,
                gesture
        );

        IKeybindGestureSpell.GestureResult result = ((IKeybindGestureSpell) target.spell).onGestureTriggered(context, gesture, onCooldown);
        if (!result.isSuccess()) {
            return false;
        }

        if (result.shouldApplyCooldown()) {
            target.page.applyRawSpellCooldown(target.pageStack, target.rawIndex, player.world, player, bookStack);
        }
        applyActionLock(target.pageData, target.rawIndex, player.world.getTotalWorldTime(), castActionTicks);
        stateHelper.savePageData(bookStack, target);
        ToolHelper.damageTool(bookStack, MagicBook.DURABILITY_COST, player);
        return true;
    }

    private int resolveCastActionTicks(ISpell spell, EntityPlayer player, ItemStack bookStack) {
        int cooldownTicks = Math.max(0, spell.getCooldownTicks(player, bookStack));
        int castActionTicks = Math.max(0, spell.getCastActionTicks(player, bookStack));
        if (cooldownTicks <= 0 && castActionTicks <= 0) {
            TConGreedyAddon.LOGGER.warn("Keybind spell {} has zero cooldown but no castActionTicks, rejecting cast.", spell.getNameKey());
            return -1;
        }
        return castActionTicks;
    }

    private boolean isActionLocked(NBTTagCompound pageData, int rawIndex, long worldTick, int castActionTicks) {
        if (castActionTicks <= 0) {
            return false;
        }
        NBTTagCompound locks = pageData.getCompoundTag(MagicBookKeys.TAG_ACTION_LOCKS);
        long lastCast = locks.getLong(String.valueOf(rawIndex));
        return worldTick < lastCast + castActionTicks;
    }

    private void applyActionLock(NBTTagCompound pageData, int rawIndex, long worldTick, int castActionTicks) {
        if (castActionTicks <= 0) {
            return;
        }
        NBTTagCompound locks = pageData.getCompoundTag(MagicBookKeys.TAG_ACTION_LOCKS);
        locks.setLong(String.valueOf(rawIndex), worldTick);
        pageData.setTag(MagicBookKeys.TAG_ACTION_LOCKS, locks);
    }

    private ActionResult<ItemStack> beginHoldCast(World world, EntityPlayer player, EnumHand hand, ItemStack stack,
                                                  MagicBookStateHelper.ResolvedSpellTarget target,
                                                  MagicBookStateHelper.HoldCastModel model) {
        if (world.isRemote && stateHelper.isSelectedSpellOnCooldown(stack, player, MagicPageItem.SlotType.RIGHT)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (!world.isRemote && target.page.isRawSpellOnCooldown(target.pageStack, target.rawIndex, world, player, stack)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        player.setActiveHand(hand);
        MagicBookStateHelper.HoldRuntimeState state = stateHelper.getHoldState(player, hand, world);
        if (!stateHelper.isSameHoldState(state, target, model)) {
            stateHelper.startHoldState(player, target, model, hand, world);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private void castChannelTick(MagicBookStateHelper.ResolvedSpellTarget target, SpellContext context,
                                 MagicBookStateHelper.HoldRuntimeState state) {
        ((IChannelReleaseSpell) target.spell).onChannelTick(context, state.heldTicks);
    }

    private void castHoldTriggerTick(ItemStack bookStack, EntityPlayer player,
                                     MagicBookStateHelper.ResolvedSpellTarget target, SpellContext context,
                                     MagicBookStateHelper.HoldRuntimeState state) {
        IHoldTriggerSpell holdSpell = (IHoldTriggerSpell) target.spell;
        int startTicks = Math.max(0, holdSpell.getTriggerStartTicks(context));
        if (state.heldTicks >= startTicks) {
            if (!state.activationEvaluated) {
                state.activationEvaluated = true;
                state.activationApproved = canActivateHoldTrigger(bookStack, player, target);
                if (!state.activationApproved) {
                    player.stopActiveHand();
                    return;
                }
            }
            if (state.activationApproved) {
                state.hasProducedEffect |= holdSpell.onHoldTriggerTick(context, state.heldTicks);
            }
        }

        int maxHoldTicks = holdSpell.getMaxHoldTicks(context);
        if (maxHoldTicks > 0 && state.heldTicks >= maxHoldTicks) {
            player.stopActiveHand();
        }
    }

    private boolean canActivateHoldTrigger(ItemStack bookStack, EntityPlayer player,
                                           MagicBookStateHelper.ResolvedSpellTarget target) {
        return postSpellUsePre(player, bookStack, target, TriggerSource.holdTick());
    }

    private void finishChannelRelease(ItemStack bookStack, EntityPlayer player,
                                      MagicBookStateHelper.ResolvedSpellTarget target,
                                      SpellContext finishContext,
                                      MagicBookStateHelper.HoldRuntimeState state) {
        IChannelReleaseSpell channelSpell = (IChannelReleaseSpell) target.spell;
        int minTicks = Math.max(0, channelSpell.getMinChannelTicks(finishContext));
        boolean interrupted = state.heldTicks < minTicks;
        boolean canceled = false;
        boolean success = false;

        if (interrupted) {
            channelSpell.onChannelInterrupted(finishContext, state.heldTicks);
        } else if (!canReleaseChannel(bookStack, player, target)) {
            canceled = true;
        } else {
            success = castChannelRelease(bookStack, player, target, finishContext, state);
            postSpellUsePost(player, bookStack, target, TriggerSource.holdRelease(), success);
        }

        postSpellUseFinish(player, bookStack, target, success, state.heldTicks, interrupted, canceled);
    }

    private boolean canReleaseChannel(ItemStack bookStack, EntityPlayer player,
                                      MagicBookStateHelper.ResolvedSpellTarget target) {
        return postSpellUsePre(player, bookStack, target, TriggerSource.holdRelease());
    }

    private boolean castChannelRelease(ItemStack bookStack, EntityPlayer player,
                                       MagicBookStateHelper.ResolvedSpellTarget target,
                                       SpellContext finishContext,
                                       MagicBookStateHelper.HoldRuntimeState state) {
        boolean success = ((IChannelReleaseSpell) target.spell).onChannelRelease(finishContext, state.heldTicks, true);
        if (success) {
            target.page.applyRawSpellCooldown(target.pageStack, target.rawIndex, player.world, player, bookStack);
            ToolHelper.damageTool(bookStack, MagicBook.DURABILITY_COST, player);
        }
        return success;
    }

    private void finishHoldTrigger(ItemStack bookStack, EntityPlayer player,
                                   MagicBookStateHelper.ResolvedSpellTarget target,
                                   SpellContext finishContext,
                                   MagicBookStateHelper.HoldRuntimeState state) {
        IHoldTriggerSpell holdSpell = (IHoldTriggerSpell) target.spell;
        int startTicks = Math.max(0, holdSpell.getTriggerStartTicks(finishContext));
        boolean interrupted = state.heldTicks < startTicks;
        boolean canceled = state.activationEvaluated && !state.activationApproved;
        boolean logicalInterrupted = interrupted || canceled;

        holdSpell.onHoldEnd(finishContext, state.heldTicks, logicalInterrupted);
        if (state.activationApproved) {
            if (!interrupted) {
                target.page.applyRawSpellCooldown(target.pageStack, target.rawIndex, player.world, player, bookStack);
            }
            postSpellUsePost(player, bookStack, target, TriggerSource.holdRelease(), state.hasProducedEffect);
        }
        postSpellUseFinish(player, bookStack, target, state.hasProducedEffect, state.heldTicks, logicalInterrupted, canceled);
    }

    private boolean postSpellUsePre(EntityPlayer player, ItemStack bookStack,
                                    MagicBookStateHelper.ResolvedSpellTarget target,
                                    TriggerSource triggerSource) {
        return !MinecraftForge.EVENT_BUS.post(new SpellUseEvent.Pre(
                player,
                bookStack,
                target.pageStack,
                target.slotType,
                target.bookmarkId,
                target.rawIndex,
                target.spell.getNameKey(),
                triggerSource
        ));
    }

    private void postSpellUsePost(EntityPlayer player, ItemStack bookStack,
                                  MagicBookStateHelper.ResolvedSpellTarget target,
                                  TriggerSource triggerSource, boolean success) {
        MinecraftForge.EVENT_BUS.post(new SpellUseEvent.Post(
                player,
                bookStack,
                target.pageStack,
                target.slotType,
                target.bookmarkId,
                target.rawIndex,
                target.spell.getNameKey(),
                triggerSource,
                success
        ));
    }

    private void postSpellUseFinish(EntityPlayer player, ItemStack bookStack,
                                    MagicBookStateHelper.ResolvedSpellTarget target,
                                    boolean success, int heldTicks,
                                    boolean interrupted, boolean canceled) {
        MinecraftForge.EVENT_BUS.post(new SpellUseEvent.Finish(
                player,
                bookStack,
                target.pageStack,
                target.slotType,
                target.bookmarkId,
                target.rawIndex,
                target.spell.getNameKey(),
                TriggerSource.holdRelease(),
                success,
                heldTicks,
                interrupted,
                canceled
        ));
    }
}
