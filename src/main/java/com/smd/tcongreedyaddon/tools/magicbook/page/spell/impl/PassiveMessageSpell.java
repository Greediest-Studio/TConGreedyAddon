package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

public class PassiveMessageSpell extends AbstractSpell {

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.6666")
            .selectable(false)
            .renderInOverlay(true)
            .cooldown(0)
            .build();

    public PassiveMessageSpell() {
        super(BLUEPRINT);
    }

    @Override
    protected boolean canTriggerInternal(SpellContext context) {
        return context.trigger.isType(TriggerSource.Type.TICK) &&
                context.player.ticksExisted % 40 == 0;
    }

    @Override
    protected boolean executeInternal(SpellContext context) {
        if (context.world.isRemote) {
            return true;
        }
        context.player.sendMessage(new TextComponentString("Passive effect triggered!"));
        return true;
    }

    @Override
    public ResourceLocation getIcon() {
        return null;
    }
}
