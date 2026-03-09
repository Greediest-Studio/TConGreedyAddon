package com.smd.tcongreedyaddon.traits.something;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.events.ProjectileEvent;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

public class TraitSpeed extends AbstractTrait {

    private static final float MAX_BOOST = 3.0f;
    private static final double MAX_SPEED = 0.7;
    private static final double BASE_SPEED = 0.1;
    private static final double MAX_SPEED_PROJECTILE = 40.0;

    private static final String TAG_LAUNCH_SPEED = "speed_launch_speed";

    public TraitSpeed() {
        super("speed", 0xFFAA00);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected){

        if (world.isRemote || !(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;

        if (!isSelected) return;

        Potion speedPotion = Potion.getPotionById(1);

        if (speedPotion == null) return;

        PotionEffect existing = player.getActivePotionEffect(speedPotion);
        if (existing != null && existing.getDuration() > 20) {
            return;
        }

        player.addPotionEffect(new PotionEffect(speedPotion, 200, 0));
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {

        if (event.getEntity().getEntityWorld().isRemote) return;

        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;

        EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();
        ItemStack tool = attacker.getHeldItemMainhand();

        if (tool.isEmpty()) return;

        if (!TinkerUtil.hasTrait(tool.getTagCompound(), this.identifier)) return;

        IAttributeInstance speedAttr = attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        double currentSpeed = speedAttr.getAttributeValue();

        double clampedSpeed = Math.max(BASE_SPEED, Math.min(currentSpeed, MAX_SPEED));
        float multiplier = 1.0f + (float)((clampedSpeed - BASE_SPEED) / (MAX_SPEED - BASE_SPEED)) * (MAX_BOOST - 1.0f);

        event.setAmount(event.getAmount() * multiplier);
    }

    @SubscribeEvent
    public void onProjectileLaunch(ProjectileEvent.OnLaunch event) {

        ItemStack launcher = event.launcher;
        if (launcher.isEmpty()) return;

        if (!TinkerUtil.hasTrait(launcher.getTagCompound(), this.identifier)) return;

        if (!(event.projectileEntity instanceof Entity)) return;
        Entity projectile = event.projectileEntity;

        projectile.motionX *= 1.1;
        projectile.motionY *= 1.1;
        projectile.motionZ *= 1.1;

        double speed = Math.sqrt(
                projectile.motionX * projectile.motionX +
                        projectile.motionY * projectile.motionY +
                        projectile.motionZ * projectile.motionZ
        );

        projectile.getEntityData().setDouble(TAG_LAUNCH_SPEED, speed);
    }

    @SubscribeEvent
    public void onProjectileHit(LivingHurtEvent event) {

        if (event.getEntity().getEntityWorld().isRemote) return;

        Entity immediateSource = event.getSource().getImmediateSource();
        if (immediateSource == null) return;

        NBTTagCompound data = immediateSource.getEntityData();
        if (!data.hasKey(TAG_LAUNCH_SPEED)) return;

        double launchSpeed = data.getDouble(TAG_LAUNCH_SPEED);
        float multiplier = computeMultiplier(launchSpeed);

        event.setAmount(event.getAmount() * multiplier);
    }

    private float computeMultiplier(double speed) {
        double clamped = Math.max(BASE_SPEED, Math.min(speed, MAX_SPEED_PROJECTILE));
        double t = (clamped - BASE_SPEED) / (MAX_SPEED_PROJECTILE - BASE_SPEED);
        return 1.0f + (float) t * (MAX_BOOST - 1.0f);
    }
}
