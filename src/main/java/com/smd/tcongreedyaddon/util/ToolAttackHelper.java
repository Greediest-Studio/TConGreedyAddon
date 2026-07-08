package com.smd.tcongreedyaddon.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import slimeknights.tconstruct.common.TinkerNetwork;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.util.List;

/**
 * 通用辅助方法（战斗行为）。
 */
public final class ToolAttackHelper {

    /**
     * 使用自定义伤害倍率,伤害类型和伤害源执行攻击。
     *
     * @param stack           工具物品堆
     * @param tool            工具实例
     * @param attacker        攻击者
     * @param targetEntity    目标实体
     * @param damageMultiplier 伤害倍率（最终伤害 = 原计算伤害 × 此倍率）
     * @param damageSource    自定义伤害源（将替代工具默认的伤害源）
     * @return 攻击是否命中（true 表示造成伤害）
     */
    public static boolean attackEntityLeft(ItemStack stack, ToolCore tool,
                                                       EntityLivingBase attacker, Entity targetEntity,
                                                       float damageMultiplier, DamageSource damageSource) {
        if (targetEntity == null || !targetEntity.canBeAttackedWithItem() || targetEntity.hitByEntity(attacker) || !stack.hasTagCompound()) {
            return false;
        }
        if (ToolHelper.isBroken(stack)) {
            return false;
        }
        if (attacker == null) {
            return false;
        }

        EntityLivingBase target = null;
        EntityPlayer player = null;
        if (targetEntity instanceof EntityLivingBase) {
            target = (EntityLivingBase) targetEntity;
        }
        if (attacker instanceof EntityPlayer) {
            player = (EntityPlayer) attacker;
            if (target instanceof EntityPlayer && !player.canAttackPlayer((EntityPlayer) target)) {
                return false;
            }
        }

        List<ITrait> traits = TinkerUtil.getTraitsOrdered(stack);
        float baseDamage = (float) attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        float baseKnockback = attacker.isSprinting() ? 1.0F : 0.0F;
        boolean isCritical = attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isOnLadder()
                && !attacker.isInWater() && !attacker.isPotionActive(MobEffects.BLINDNESS) && !attacker.isRiding();

        for (ITrait trait : traits) {
            if (trait.isCriticalHit(stack, attacker, target)) {
                isCritical = true;
            }
        }

        float damage = baseDamage;
        if (target != null) {
            for (ITrait trait : traits) {
                damage = trait.damage(stack, attacker, target, baseDamage, damage, isCritical);
            }
        }

        if (isCritical) {
            damage *= 1.5F;
        }

        damage = ToolHelper.calcCutoffDamage(damage, tool.damageCutoff());

        float knockback = baseKnockback;
        if (target != null) {
            for (ITrait trait : traits) {
                knockback = trait.knockBack(stack, attacker, target, damage, baseKnockback, knockback, isCritical);
            }
        }

        float oldHP = 0.0F;
        double oldVelX = targetEntity.motionX;
        double oldVelY = targetEntity.motionY;
        double oldVelZ = targetEntity.motionZ;
        if (target != null) {
            oldHP = target.getHealth();
        }

        SoundEvent sound = null;
        if (player != null) {
            float cooldown = player.getCooledAttackStrength(0.5F);
            sound = cooldown > 0.9F ? SoundEvents.ENTITY_PLAYER_ATTACK_STRONG : SoundEvents.ENTITY_PLAYER_ATTACK_WEAK;
            damage *= 0.2F + cooldown * cooldown * 0.8F;
        }

        if (target != null) {
            int hurtResistantTime = target.hurtResistantTime;
            for (ITrait trait : traits) {
                trait.onHit(stack, attacker, target, damage, isCritical);
                target.hurtResistantTime = hurtResistantTime;
            }
        }

        float finalDamage = damage * damageMultiplier;
        boolean hit = targetEntity.attackEntityFrom(damageSource, finalDamage);

        if (hit && target != null) {
            float damageDealt = oldHP - target.getHealth();

            target.motionX = oldVelX + (target.motionX - oldVelX) * (double) tool.knockback();
            target.motionY = oldVelY + (target.motionY - oldVelY) * (double) tool.knockback() / 3.0;
            target.motionZ = oldVelZ + (target.motionZ - oldVelZ) * (double) tool.knockback();
            oldVelX = target.motionX;
            oldVelY = target.motionY;
            oldVelZ = target.motionZ;

            if (knockback > 0.0F) {
                double velX = -Math.sin(attacker.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F;
                double velZ = Math.cos(attacker.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F;
                targetEntity.addVelocity(velX, 0.1, velZ);
                attacker.motionX *= 0.6;
                attacker.motionZ *= 0.6;
                attacker.setSprinting(false);
            }

            if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                TinkerNetwork.sendPacket(targetEntity, new SPacketEntityVelocity(targetEntity));
                targetEntity.velocityChanged = false;
                targetEntity.motionX = oldVelX;
                targetEntity.motionY = oldVelY;
                targetEntity.motionZ = oldVelZ;
            }

            if (player != null) {
                if (isCritical) {
                    player.onCriticalHit(target);
                    sound = SoundEvents.ENTITY_PLAYER_ATTACK_CRIT;
                }
                if (damage > baseDamage) {
                    player.onEnchantmentCritical(targetEntity);
                }
            }

            attacker.setLastAttackedEntity(target);

            for (ITrait trait : traits) {
                trait.afterHit(stack, attacker, target, damageDealt, isCritical, true);
            }

            if (player != null) {
                stack.hitEntity(target, player);
                if (!player.capabilities.isCreativeMode) {
                    tool.reduceDurabilityOnHit(stack, player, finalDamage);
                }
                player.addStat(StatList.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
                player.addExhaustion(0.3F);
                player.resetCooldown();
            } else {
                tool.reduceDurabilityOnHit(stack, null, finalDamage);
            }
        } else {
            sound = SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE;
        }

        if (player != null && sound != null) {
            player.world.playSound(null, player.posX, player.posY, player.posZ, sound, player.getSoundCategory(), 1.0F, 1.0F);
        }

        return true;
    }


    /**
     * 使用自定义伤害倍率,伤害类型和伤害源执行攻击。
     *
     * @param stack           工具物品堆
     * @param tool            工具实例
     * @param attacker        攻击者
     * @param targetEntity    目标实体
     * @param damageMultiplier 伤害倍率（最终伤害 = 原计算伤害 × 此倍率）
     * @param damageSource    自定义伤害源（将替代工具默认的伤害源）
     * @return 攻击是否命中（true 表示造成伤害）
     */
    public static boolean attackEntityRight(ItemStack stack, ToolCore tool,
                                                       EntityLivingBase attacker, Entity targetEntity,
                                                       float damageMultiplier, DamageSource damageSource) {
        if (targetEntity == null || !targetEntity.canBeAttackedWithItem() || targetEntity.hitByEntity(attacker) || !stack.hasTagCompound()) {
            return false;
        }
        if (ToolHelper.isBroken(stack)) {
            return false;
        }
        if (attacker == null) {
            return false;
        }

        EntityLivingBase target = null;
        EntityPlayer player = null;
        if (targetEntity instanceof EntityLivingBase) {
            target = (EntityLivingBase) targetEntity;
        }
        if (attacker instanceof EntityPlayer) {
            player = (EntityPlayer) attacker;
            if (target instanceof EntityPlayer && !player.canAttackPlayer((EntityPlayer) target)) {
                return false;
            }
        }

        List<ITrait> traits = TinkerUtil.getTraitsOrdered(stack);
        float baseDamage = (float) attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        float baseKnockback = attacker.isSprinting() ? 1.0F : 0.0F;
        boolean isCritical = attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isOnLadder()
                && !attacker.isInWater() && !attacker.isPotionActive(MobEffects.BLINDNESS) && !attacker.isRiding();

        for (ITrait trait : traits) {
            if (trait.isCriticalHit(stack, attacker, target)) {
                isCritical = true;
            }
        }

        float damage = baseDamage;
        if (target != null) {
            for (ITrait trait : traits) {
                damage = trait.damage(stack, attacker, target, baseDamage, damage, isCritical);
            }
        }

        if (isCritical) {
            damage *= 1.5F;
        }

        damage = ToolHelper.calcCutoffDamage(damage, tool.damageCutoff());

        float knockback = baseKnockback;
        if (target != null) {
            for (ITrait trait : traits) {
                knockback = trait.knockBack(stack, attacker, target, damage, baseKnockback, knockback, isCritical);
            }
        }

        float oldHP = 0.0F;
        double oldVelX = targetEntity.motionX;
        double oldVelY = targetEntity.motionY;
        double oldVelZ = targetEntity.motionZ;
        if (target != null) {
            oldHP = target.getHealth();
        }

        if (target != null) {
            int hurtResistantTime = target.hurtResistantTime;
            for (ITrait trait : traits) {
                trait.onHit(stack, attacker, target, damage, isCritical);
                target.hurtResistantTime = hurtResistantTime;
            }
        }

        float finalDamage = damage * damageMultiplier;
        boolean hit = targetEntity.attackEntityFrom(damageSource, finalDamage);

        if (hit && target != null) {
            float damageDealt = oldHP - target.getHealth();

            target.motionX = oldVelX + (target.motionX - oldVelX) * (double) tool.knockback();
            target.motionY = oldVelY + (target.motionY - oldVelY) * (double) tool.knockback() / 3.0;
            target.motionZ = oldVelZ + (target.motionZ - oldVelZ) * (double) tool.knockback();
            oldVelX = target.motionX;
            oldVelY = target.motionY;
            oldVelZ = target.motionZ;

            if (knockback > 0.0F) {
                double velX = -Math.sin(attacker.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F;
                double velZ = Math.cos(attacker.rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F;
                targetEntity.addVelocity(velX, 0.1, velZ);
                attacker.motionX *= 0.6;
                attacker.motionZ *= 0.6;
                attacker.setSprinting(false);
            }

            if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                TinkerNetwork.sendPacket(targetEntity, new SPacketEntityVelocity(targetEntity));
                targetEntity.velocityChanged = false;
                targetEntity.motionX = oldVelX;
                targetEntity.motionY = oldVelY;
                targetEntity.motionZ = oldVelZ;
            }

            attacker.setLastAttackedEntity(target);

            for (ITrait trait : traits) {
                trait.afterHit(stack, attacker, target, damageDealt, isCritical, true);
            }

            if (player != null) {
                stack.hitEntity(target, player);
                if (!player.capabilities.isCreativeMode) {
                    tool.reduceDurabilityOnHit(stack, player, finalDamage);
                }
                player.addStat(StatList.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
                player.addExhaustion(0.3F);
            } else {
                tool.reduceDurabilityOnHit(stack, null, finalDamage);
            }
        }

        return true;
    }
}
