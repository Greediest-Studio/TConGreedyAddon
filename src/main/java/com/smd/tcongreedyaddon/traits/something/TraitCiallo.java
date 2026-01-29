package com.smd.tcongreedyaddon.traits.something;

import com.smd.tcongreedyaddon.init.SoundsHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public class TraitCiallo extends AbstractTrait {

    public TraitCiallo() {
        super("ciallo", 0xffffff);
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, boolean isCritical) {
        target.world.playSound(
                null,
                target.posX, target.posY, target.posZ,
                SoundsHandler.CIALLO,
                SoundCategory.PLAYERS,
                2.0F, 1.0F
        );
    }

    @Override
    public void onPlayerHurt(ItemStack tool, EntityPlayer player, EntityLivingBase attacker, LivingHurtEvent event) {
        attacker.world.playSound(
                null,
                attacker.posX, attacker.posY, attacker.posZ,
                SoundsHandler.CIALLO,
                SoundCategory.PLAYERS,
                2.0F, 1.0F
        );
    }
}
