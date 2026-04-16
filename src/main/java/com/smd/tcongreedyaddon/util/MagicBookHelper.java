package com.smd.tcongreedyaddon.util;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
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
 * 魔导书通用辅助方法（法术检测与战斗行为）。
 */
public final class MagicBookHelper {

    private MagicBookHelper() {} // 禁止实例化

    /**
     * 判断玩家是否持有包含指定法术（注册名）的魔导书。
     * 当玩家为 null 或传入的法术名为空时返回 false。
     * 此方法会检查主手与副手的物品堆，若任一为 MagicBook 且包含该法术则返回 true。
     * @param player 玩家实体，若为 null 则返回 false
     * @param spellRegistryName 法术注册名（非空字符串），例如 "spell.strand_grapple"
     * @return 若任一手持书包含该法术则返回 true，否则返回 false
     */
    public static boolean isHoldingBookWithSpell(EntityPlayer player, String spellRegistryName) {
        if (player == null || spellRegistryName == null || spellRegistryName.trim().isEmpty()) {
            return false;
        }
        String normalizedName = spellRegistryName.trim();
        return hasBookWithSpell(player.getHeldItemMainhand(), normalizedName)
                || hasBookWithSpell(player.getHeldItemOffhand(), normalizedName);
    }

    /**
     * 判断指定物品堆是否为魔导书且包含指定法术（通过注册名）。
     * 允许传入 null 或空堆，遇到非魔导书时直接返回 false。
     * @param stack 要检查的物品堆，允许为 null 或空堆
     * @param spellRegistryName 已规范化的法术注册名
     * @return 若为 MagicBook 且包含该法术返回 true，否则返回 false
     */
    private static boolean hasBookWithSpell(ItemStack stack, String spellRegistryName) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof MagicBook)) {
            return false;
        }
        MagicBook book = (MagicBook) stack.getItem();
        return book.hasSpell(stack, spellRegistryName);
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

            oldVelX = target.motionX = oldVelX + (target.motionX - oldVelX) * (double) tool.knockback();
            oldVelY = target.motionY = oldVelY + (target.motionY - oldVelY) * (double) tool.knockback() / 3.0;
            oldVelZ = target.motionZ = oldVelZ + (target.motionZ - oldVelZ) * (double) tool.knockback();

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

            oldVelX = target.motionX = oldVelX + (target.motionX - oldVelX) * (double) tool.knockback();
            oldVelY = target.motionY = oldVelY + (target.motionY - oldVelY) * (double) tool.knockback() / 3.0;
            oldVelZ = target.motionZ = oldVelZ + (target.motionZ - oldVelZ) * (double) tool.knockback();

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
