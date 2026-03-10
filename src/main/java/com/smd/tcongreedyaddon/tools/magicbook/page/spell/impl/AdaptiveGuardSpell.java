package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public class AdaptiveGuardSpell extends AbstractSpell {

    private static final String ICON_STATE_KEY = "adaptive_guard_icon";
    private static final String STATE_ATTACK = "attack";
    private static final String STATE_JUMP = "jump";

    private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("minecraft", "textures/items/shield.png");
    private static final ResourceLocation ATTACK_ICON = new ResourceLocation("minecraft", "textures/items/iron_sword.png");
    private static final ResourceLocation JUMP_ICON = new ResourceLocation("minecraft", "textures/items/feather.png");

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.adaptive_guard")
            .icon(DEFAULT_ICON)
            .selectable(false)
            .renderInOverlay(true)
            .cooldown(80)
            .listeningEvents(LivingAttackEvent.class, LivingEvent.LivingJumpEvent.class)
            .build();

    public AdaptiveGuardSpell() {
        super(BLUEPRINT);
    }

    @Override
    protected boolean canTriggerInternal(SpellContext context) {
        if (!context.trigger.isEvent()) {
            return false;
        }
        return (context.trigger.getEvent() instanceof LivingAttackEvent || context.trigger.getEvent() instanceof LivingEvent.LivingJumpEvent)
                && context.slot == MagicPageItem.SlotType.RIGHT;
    }

    @Override
    protected boolean executeInternal(SpellContext context) {
        if (context.world.isRemote) {
            return true;
        }
        if (context.trigger.getEvent() instanceof LivingAttackEvent) {
            LivingAttackEvent attackEvent = (LivingAttackEvent) context.trigger.getEvent();
            attackEvent.setCanceled(true);
            setIconState(context.pageData, STATE_ATTACK);
            context.player.sendMessage(new TextComponentString("Adaptive Guard absorbed the strike."));
            return true;
        }
        if (context.trigger.getEvent() instanceof LivingEvent.LivingJumpEvent) {
            context.player.motionY += 0.2;
            context.player.velocityChanged = true;
            setIconState(context.pageData, STATE_JUMP);
            context.player.sendMessage(new TextComponentString("Adaptive Guard boosts your jump."));
            return true;
        }
        return false;
    }

    private void setIconState(NBTTagCompound data, String state) {
        if (data != null) {
            data.setString(ICON_STATE_KEY, state);
        }
    }

    private String getIconState(NBTTagCompound data) {
        return data != null && data.hasKey(ICON_STATE_KEY) ? data.getString(ICON_STATE_KEY) : null;
    }

    @Override
    public ResourceLocation getDisplayIcon(NBTTagCompound pageData, int rawIndex) {
        String state = getIconState(pageData);
        if (STATE_JUMP.equals(state)) {
            return JUMP_ICON;
        }
        if (STATE_ATTACK.equals(state)) {
            return ATTACK_ICON;
        }
        return DEFAULT_ICON;
    }
}
