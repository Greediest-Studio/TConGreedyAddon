package com.smd.tcongreedyaddon.traits.something;

import com.smd.tcongreedyaddon.init.SoundsHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public class TraitSoundEffect extends AbstractTrait {
    public TraitSoundEffect() {
        super("soundeffects", 0xffffff);
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, boolean isCritical) {
        SoundEvent[] sounds = {
                SoundsHandler.SUCAI_1,
                SoundsHandler.SUCAI_2,
                SoundsHandler.SUCAI_3,
                SoundsHandler.SUCAI_4,
                SoundsHandler.SUCAI_5,
                SoundsHandler.SUCAI_6,
                SoundsHandler.SUCAI_7
        };
        SoundEvent randomSound = sounds[target.world.rand.nextInt(sounds.length)];
        target.world.playSound(
                null,
                target.posX, target.posY, target.posZ,
                randomSound,
                SoundCategory.PLAYERS,
                2.0F, 1.0F
        );
    }

    @Override
    public void onPlayerHurt(ItemStack tool, EntityPlayer player, EntityLivingBase attacker, LivingHurtEvent event) {
        SoundEvent[] sounds = {
                SoundsHandler.SUCAI_1,
                SoundsHandler.SUCAI_2,
                SoundsHandler.SUCAI_3,
                SoundsHandler.SUCAI_4,
                SoundsHandler.SUCAI_5,
                SoundsHandler.SUCAI_6,
                SoundsHandler.SUCAI_7
        };
        SoundEvent randomSound = sounds[attacker.world.rand.nextInt(sounds.length)];
        attacker.world.playSound(
                null,
                attacker.posX, attacker.posY, attacker.posZ,
                randomSound,
                SoundCategory.PLAYERS,
                2.0F, 1.0F
        );
    }

}
