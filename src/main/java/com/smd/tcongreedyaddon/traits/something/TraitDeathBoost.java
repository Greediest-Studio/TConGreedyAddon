package com.smd.tcongreedyaddon.traits.something;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public class TraitDeathBoost extends AbstractTrait {

    private static final int TRAIT_COLOR = 0xAA0000;
    private static final float MAX_BOOST = 4.0f;
    private static final int MAX_DEATHS = 1000;

    public TraitDeathBoost() {
        super("death_boost", TRAIT_COLOR);
    }

    @Override
    public float damage(ItemStack tool, EntityLivingBase player,
                        EntityLivingBase target, float damage, float newDamage,
                        boolean isCritical) {

        if (!(player instanceof EntityPlayerMP)) {
            return newDamage;
        }

        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        int deathCount = serverPlayer.getStatFile().readStat(StatList.DEATHS);

        float multiplier = 1.0f + (Math.min(deathCount, MAX_DEATHS) / (float) MAX_DEATHS)
                * (MAX_BOOST - 1.0f);

        return newDamage * multiplier;
    }
}