package com.smd.tcongreedyaddon.tools.magicbook.effect;

import com.smd.tcongreedyaddon.Tags;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;

public class ThermalHeatPotion extends Potion {

    public ThermalHeatPotion() {
        super(true, 0xE65A2A);
        setRegistryName(Tags.MOD_ID, "gauss_heat");
        setPotionName("effect.tcongreedyaddon.gauss_heat");
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration > 0 && duration % 20 == 0;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        ThermalSunderRuntime.onHeatPotionTick(entityLivingBaseIn, amplifier);
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn,
                                                    AbstractAttributeMap attributeMapIn, int amplifier) {
        super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
        ThermalSunderRuntime.onHeatEffectRemoved(entityLivingBaseIn);
    }
}
