package com.smd.tcongreedyaddon.event;

import com.smd.tcongreedyaddon.tools.solidarytinker.SoulGe;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

public class SoulGeEventHandler {

    public static final SoulGeEventHandler INSTANCE = new SoulGeEventHandler();

    private static final double EXECUTION_LIFT_HEIGHT = 3.5d;
    private static final double EXECUTION_LIFT_STEP = 1.25d;
    private static final double EXECUTION_DROP_SPEED = -3.2d;

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if (living instanceof EntityPlayer) {
            return;
        }

        NBTTagCompound data = living.getEntityData();
        tickOwnedLink(data);
        if (data.getInteger(SoulGe.ENTITY_TAG_EXECUTION_STAGE) > 0) {
            tickExecutionState(living, data);
        }
    }

    private void tickOwnedLink(NBTTagCompound data) {
        int timer = data.getInteger(SoulGe.ENTITY_TAG_EXECUTION_TIMER);
        if (timer > 0 && data.hasKey(SoulGe.ENTITY_TAG_OWNER) && data.getInteger(SoulGe.ENTITY_TAG_EXECUTION_STAGE) == 0) {
            data.setInteger(SoulGe.ENTITY_TAG_EXECUTION_TIMER, timer - 1);
            if (timer - 1 <= 0) {
                clearLinkOnly(data);
            }
        }
    }

    private void tickExecutionState(EntityLivingBase living, NBTTagCompound data) {
        int stage = data.getInteger(SoulGe.ENTITY_TAG_EXECUTION_STAGE);
        int timer = data.getInteger(SoulGe.ENTITY_TAG_EXECUTION_TIMER);
        double startY = data.hasKey(SoulGe.ENTITY_TAG_EXECUTION_START_Y) ? data.getDouble(SoulGe.ENTITY_TAG_EXECUTION_START_Y) : living.posY;

        switch (stage) {
            case 1:
                living.motionX = 0.0d;
                living.motionZ = 0.0d;
                living.fallDistance = 0.0f;
                living.setPosition(living.posX, Math.min(startY + EXECUTION_LIFT_HEIGHT, living.posY + EXECUTION_LIFT_STEP), living.posZ);
                if (timer <= 0) {
                    data.setInteger(SoulGe.ENTITY_TAG_EXECUTION_STAGE, 2);
                    data.setInteger(SoulGe.ENTITY_TAG_EXECUTION_TIMER, 20);
                    living.motionY = EXECUTION_DROP_SPEED;
                } else {
                    data.setInteger(SoulGe.ENTITY_TAG_EXECUTION_TIMER, timer - 1);
                    living.motionY = 0.0d;
                }
                living.velocityChanged = true;
                break;
            case 2:
                if (living.onGround || timer <= 0) {
                    living.motionX = 0.0d;
                    living.motionY = 0.0d;
                    living.motionZ = 0.0d;
                    living.fallDistance = 0.0f;
                    data.setInteger(SoulGe.ENTITY_TAG_READY_TO_DIE, 1);
                    data.setInteger(SoulGe.ENTITY_TAG_EXECUTION_STAGE, 3);
                } else {
                    data.setInteger(SoulGe.ENTITY_TAG_EXECUTION_TIMER, timer - 1);
                    living.motionX = 0.0d;
                    living.motionY = EXECUTION_DROP_SPEED;
                    living.motionZ = 0.0d;
                    living.fallDistance = 0.0f;
                }
                living.velocityChanged = true;
                break;
            case 3:
                living.motionX = 0.0d;
                living.motionY = 0.0d;
                living.motionZ = 0.0d;
                living.fallDistance = 0.0f;
                living.velocityChanged = true;

                int readyTick = data.getInteger(SoulGe.ENTITY_TAG_READY_TO_DIE);
                data.setInteger(SoulGe.ENTITY_TAG_READY_TO_DIE, readyTick - 1);
                if (readyTick <= 0) {
                    executeKill(living, data);
                }
                break;
            default:
                break;
        }
    }

    private void executeKill(EntityLivingBase living, NBTTagCompound data) {
        if (!data.hasKey(SoulGe.ENTITY_TAG_READY_TO_DIE)) {
            return;
        }
        EntityPlayer killer = findKiller(living, data);
        if (killer != null) {
            living.attackEntityFrom(DamageSource.causePlayerDamage(killer).setDamageBypassesArmor().setMagicDamage(), Float.MAX_VALUE);
        } else {
            living.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
        }
        data.removeTag(SoulGe.ENTITY_TAG_READY_TO_DIE);
        data.removeTag(SoulGe.ENTITY_TAG_TARGETED);
        data.removeTag(SoulGe.ENTITY_TAG_KILLER);
        data.removeTag(SoulGe.ENTITY_TAG_OWNER);
        data.removeTag(SoulGe.ENTITY_TAG_EXECUTION_STAGE);
        data.removeTag(SoulGe.ENTITY_TAG_EXECUTION_TIMER);
        data.removeTag(SoulGe.ENTITY_TAG_EXECUTION_START_Y);
    }

    private EntityPlayer findKiller(EntityLivingBase living, NBTTagCompound data) {
        if (!data.hasKey(SoulGe.ENTITY_TAG_KILLER)) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(data.getString(SoulGe.ENTITY_TAG_KILLER));
            return living.world.getPlayerEntityByUUID(uuid);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void clearLinkOnly(NBTTagCompound data) {
        data.removeTag(SoulGe.ENTITY_TAG_TARGETED);
        data.removeTag(SoulGe.ENTITY_TAG_OWNER);
        data.removeTag(SoulGe.ENTITY_TAG_EXECUTION_TIMER);
        data.removeTag(SoulGe.ENTITY_TAG_EXECUTION_START_Y);
    }
}
