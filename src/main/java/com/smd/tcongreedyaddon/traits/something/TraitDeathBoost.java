package com.smd.tcongreedyaddon.traits.something;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

public class TraitDeathBoost extends AbstractTrait {

    private static final float MAX_BOOST = 4.0f;
    private static final int MAX_DEATHS = 1000;

    public TraitDeathBoost() {
        super("death_boost", 0xAA0000);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {

        if (event.getEntity().getEntityWorld().isRemote) return;

        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;

        EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();
        ItemStack tool = attacker.getHeldItemMainhand();

        if (tool.isEmpty()) return;

        if (!TinkerUtil.hasTrait(tool.getTagCompound(), this.identifier)) return;

        if (!(attacker instanceof EntityPlayerMP)) return;

        EntityPlayerMP serverPlayer = (EntityPlayerMP) attacker;
        int deathCount = serverPlayer.getStatFile().readStat(StatList.DEATHS);
        float multiplier = 1.0f + (Math.min(deathCount, MAX_DEATHS) / (float) MAX_DEATHS) * (MAX_BOOST - 1.0f);

        event.setAmount(event.getAmount() * multiplier);
    }
}
