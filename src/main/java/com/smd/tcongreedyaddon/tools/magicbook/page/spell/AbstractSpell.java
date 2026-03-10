package com.smd.tcongreedyaddon.tools.magicbook.page.spell;

import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

public abstract class AbstractSpell implements ISpell {

    private final SpellBlueprint blueprint;

    protected AbstractSpell(SpellBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    @Override
    public boolean canTrigger(SpellContext context) {
        return canTriggerInternal(context);
    }

    protected boolean canTriggerInternal(SpellContext context) {
        return true;
    }

    @Override
    public final boolean execute(SpellContext context) {
        if (!canTrigger(context)) {
            return false;
        }
        return executeInternal(context);
    }

    protected abstract boolean executeInternal(SpellContext context);

    @Override
    public boolean isSelectable() {
        return blueprint.isSelectable();
    }

    @Override
    public boolean shouldRenderInOverlay() {
        return blueprint.shouldRenderInOverlay();
    }

    @Override
    public String getNameKey() {
        return blueprint.getNameKey();
    }

    @Override
    public ResourceLocation getIcon() {
        return blueprint.getIcon();
    }

    @Override
    public int getCooldownTicks() {
        return blueprint.getCooldownTicks();
    }

    @Override
    public int getCooldownTicks(EntityPlayer player, ItemStack bookStack) {
        return computeCooldownTicks(player, bookStack);
    }

    protected int computeCooldownTicks(EntityPlayer player, ItemStack bookStack) {
        return blueprint.getCooldownTicks();
    }

    @Override
    public List<Class<? extends Event>> getListeningEvents() {
        return blueprint.getListeningEvents();
    }
}
