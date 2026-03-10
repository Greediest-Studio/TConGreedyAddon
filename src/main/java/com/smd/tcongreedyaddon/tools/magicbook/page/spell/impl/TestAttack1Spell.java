package com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.AbstractSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.SpellBlueprint;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

public class TestAttack1Spell extends AbstractSpell {

    private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("test_attack_1")
            .icon(new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/1.png"))
            .cooldown(10)
            .build();

    public TestAttack1Spell() {
        super(BLUEPRINT);
    }

    @Override
    protected boolean canTriggerInternal(SpellContext context) {
        return context.trigger.isType(TriggerSource.Type.LEFT_CLICK) &&
                context.slot == MagicPageItem.SlotType.LEFT;
    }

    @Override
    protected boolean executeInternal(SpellContext context) {
        if (context.world.isRemote) {
            return true;
        }
        Entity target = context.target;
        EntityPlayer player = context.player;
        if (target != null) {
            target.attackEntityFrom(DamageSource.causePlayerDamage(player), 1.0f);
            target.setFire(60);
        }
        player.sendMessage(new TextComponentString("Test attack 1 used!"));
        return true;
    }
}
