package com.smd.tcongreedyaddon.tools.magicbook.effect;

import com.smd.tcongreedyaddon.init.ModPotions;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.util.MagicBookHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.UUID;

public final class ThermalSunderRuntime {

    public static final int HEAT_DURATION_TICKS = 160;
    public static final int COLD_DURATION_TICKS = 160;
    public static final int MAX_HEAT_STACKS = 5;

    private static final String TAG_ROOT = "tcongreedyaddon_gauss_heat";
    private static final String KEY_SRC_UUID = "src_uuid";
    private static final String KEY_BOOK_SNAPSHOT = "book_snapshot";
    private static final String KEY_ATTACK_BASE = "attack_base";

    private static final UUID ARMOR_SHATTER_UUID = UUID.fromString("43cbf299-c3b9-4cb9-8e8d-567f083ff7e5");
    private static final String ARMOR_SHATTER_NAME = "tcongreedyaddon.gauss_armor_shatter";

    private ThermalSunderRuntime() {
    }

    public static void applyHeat(EntityPlayer caster, ItemStack bookStack, EntityLivingBase target) {
        if (target == null || !target.isEntityAlive()) {
            return;
        }

        int stacks = 1;
        PotionEffect existing = target.getActivePotionEffect(ModPotions.GAUSS_HEAT);
        if (existing != null) {
            stacks = Math.min(MAX_HEAT_STACKS, existing.getAmplifier() + 2);
        }

        target.addPotionEffect(new PotionEffect(ModPotions.GAUSS_HEAT, HEAT_DURATION_TICKS, stacks - 1));
        target.setFire(5);
        writeSnapshot(caster, bookStack, target);
    }

    public static void applyCold(EntityLivingBase target) {
        if (target == null || !target.isEntityAlive()) {
            return;
        }
        target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, COLD_DURATION_TICKS, 0));
    }

    public static boolean tryFusionDetonation(EntityPlayer caster, EntityLivingBase target) {
        if (target == null || target.world.isRemote || !target.isEntityAlive()) {
            return false;
        }

        PotionEffect heat = target.getActivePotionEffect(ModPotions.GAUSS_HEAT);
        PotionEffect cold = target.getActivePotionEffect(MobEffects.SLOWNESS);
        if (heat == null || cold == null) {
            return false;
        }

        int stacks = clampStacks(heat.getAmplifier() + 1);
        int remainingTicks = Math.max(0, heat.getDuration());
        float detonationMultiplier = getDetonationMultiplier(stacks, remainingTicks);

        playFusionEffect(caster, target);
        applySnapshotDamage(target, detonationMultiplier, caster);
        target.removePotionEffect(ModPotions.GAUSS_HEAT);
        target.removePotionEffect(MobEffects.SLOWNESS);
        clearHeatSnapshot(target);
        applyArmorShatter(target);
        return true;
    }

    public static void onHeatPotionTick(EntityLivingBase target, int amplifier) {
        if (target == null || target.world.isRemote || !target.isEntityAlive()) {
            return;
        }
        int stacks = clampStacks(amplifier + 1);
        float dotMultiplier = getHeatPerSecondMultiplier(stacks);
        applySnapshotDamage(target, dotMultiplier, null);
    }

    public static void onHeatEffectRemoved(EntityLivingBase target) {
        clearHeatSnapshot(target);
    }

    public static float getHeatPerSecondMultiplier(int stacks) {
        int clamped = clampStacks(stacks);
        return 0.1F * (float) Math.pow(2.0D, clamped - 1);
    }

    public static float getDetonationMultiplier(int stacks, int remainingTicks) {
        float seconds = Math.max(0, remainingTicks) / 20.0F;
        return getHeatPerSecondMultiplier(stacks) * seconds;
    }

    private static void playFusionEffect(@Nullable EntityPlayer caster, EntityLivingBase target) {
        if (target.world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) target.world;
            worldServer.spawnParticle(
                    EnumParticleTypes.EXPLOSION_LARGE,
                    target.posX,
                    target.posY + target.height * 0.5D,
                    target.posZ,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
        target.world.playSound(
                null,
                target.posX,
                target.posY,
                target.posZ,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );
    }

    private static void applyArmorShatter(EntityLivingBase target) {
        if (target instanceof EntityPlayer) {
            return;
        }
        IAttributeInstance armor = target.getEntityAttribute(SharedMonsterAttributes.ARMOR);
        if (armor == null) {
            return;
        }
        if (armor.getModifier(ARMOR_SHATTER_UUID) != null) {
            return;
        }
        armor.applyModifier(new AttributeModifier(ARMOR_SHATTER_UUID, ARMOR_SHATTER_NAME, -0.5D, 2));
    }

    private static boolean applySnapshotDamage(EntityLivingBase target, float multiplier, @Nullable EntityPlayer preferredSource) {
        if (multiplier <= 0.0F || target == null || !target.isEntityAlive()) {
            return false;
        }

        SnapshotData snapshot = readSnapshot(target);
        EntityPlayer source = resolveSourcePlayer(target, preferredSource, snapshot.sourceUuid);
        if (source != null && !snapshot.bookSnapshot.isEmpty() && snapshot.bookSnapshot.getItem() instanceof MagicBook) {
            try {
                return MagicBookHelper.attackEntityRight(
                        snapshot.bookSnapshot,
                        (MagicBook) snapshot.bookSnapshot.getItem(),
                        source,
                        target,
                        multiplier,
                        DamageSource.causePlayerDamage(source)
                );
            } catch (Exception ignored) {
                // 快照真实攻击失败时回退到纯伤害，避免持续效果中断。
            }
        }

        float fallbackDamage = (float) Math.max(0.0D, snapshot.attackBase) * multiplier;
        if (fallbackDamage <= 0.0F) {
            return false;
        }
        return target.attackEntityFrom(DamageSource.MAGIC, fallbackDamage);
    }

    @Nullable
    private static EntityPlayer resolveSourcePlayer(EntityLivingBase target, @Nullable EntityPlayer preferredSource,
                                                    @Nullable UUID snapshotUuid) {
        if (preferredSource != null
                && !preferredSource.isDead
                && preferredSource.world.provider.getDimension() == target.world.provider.getDimension()) {
            return preferredSource;
        }
        if (snapshotUuid == null) {
            return null;
        }
        EntityPlayer player = target.world.getPlayerEntityByUUID(snapshotUuid);
        if (player == null || player.isDead) {
            return null;
        }
        if (player.world.provider.getDimension() != target.world.provider.getDimension()) {
            return null;
        }
        return player;
    }

    private static void writeSnapshot(EntityPlayer caster, ItemStack bookStack, EntityLivingBase target) {
        NBTTagCompound root = new NBTTagCompound();
        if (caster != null) {
            root.setString(KEY_SRC_UUID, caster.getUniqueID().toString());
            IAttributeInstance attackAttribute = caster.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            double attackBase = attackAttribute == null ? 1.0D : attackAttribute.getAttributeValue();
            root.setDouble(KEY_ATTACK_BASE, attackBase);
        }
        ItemStack snapshotStack = bookStack == null ? ItemStack.EMPTY : bookStack.copy();
        if (!snapshotStack.isEmpty()) {
            root.setTag(KEY_BOOK_SNAPSHOT, snapshotStack.writeToNBT(new NBTTagCompound()));
        }
        target.getEntityData().setTag(TAG_ROOT, root);
    }

    private static SnapshotData readSnapshot(EntityLivingBase target) {
        SnapshotData data = new SnapshotData();
        NBTTagCompound root = target.getEntityData().getCompoundTag(TAG_ROOT);
        if (root.hasKey(KEY_SRC_UUID, 8)) {
            try {
                data.sourceUuid = UUID.fromString(root.getString(KEY_SRC_UUID));
            } catch (IllegalArgumentException ignored) {
                data.sourceUuid = null;
            }
        }
        if (root.hasKey(KEY_BOOK_SNAPSHOT, 10)) {
            data.bookSnapshot = new ItemStack(root.getCompoundTag(KEY_BOOK_SNAPSHOT));
        }
        if (root.hasKey(KEY_ATTACK_BASE, 6)) {
            data.attackBase = root.getDouble(KEY_ATTACK_BASE);
        }
        return data;
    }

    private static void clearHeatSnapshot(EntityLivingBase target) {
        if (target == null) {
            return;
        }
        target.getEntityData().removeTag(TAG_ROOT);
    }

    private static int clampStacks(int stacks) {
        return Math.max(1, Math.min(MAX_HEAT_STACKS, stacks));
    }

    private static final class SnapshotData {
        private UUID sourceUuid;
        private ItemStack bookSnapshot = ItemStack.EMPTY;
        private double attackBase = 1.0D;
    }
}
