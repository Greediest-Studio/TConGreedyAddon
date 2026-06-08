package com.smd.tcongreedyaddon.util;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public final class TicToolStats {

    private static final float MIN_DRAW_SPEED = 0.01F;

    private TicToolStats() {
    }

    public static boolean setBroken(ItemStack stack, boolean broken) {
        if (!TicToolStacks.isTicTarget(stack)) {
            return false;
        }
        TicToolNbt.getOrCreateStats(stack).setInteger("Broken", broken ? 1 : 0);
        return true;
    }

    public static boolean addMiningSpeed(ItemStack stack, float amount, String token) {
        return addFloatStat(stack, "MiningSpeed", amount, token);
    }

    public static boolean addAttack(ItemStack stack, float amount, String token) {
        return addFloatStat(stack, "Attack", amount, token);
    }

    public static boolean addFreeModifiers(ItemStack stack, int amount, String token) {
        return addIntStat(stack, "FreeModifiers", amount, token);
    }

    public static boolean addToughness(ItemStack stack, float amount, String token) {
        return addFloatStat(stack, "Toughness", amount, token);
    }

    public static boolean addHarvestLevel(ItemStack stack, int amount, String token) {
        return addIntStat(stack, "HarvestLevel", amount, token);
    }

    public static boolean addAttackSpeedMultiplier(ItemStack stack, float amount, String token) {
        return addFloatStat(stack, "AttackSpeedMultiplier", amount, token);
    }

    public static boolean addDefense(ItemStack stack, float amount, String token) {
        if (!canApply(stack, "Defense", token) || !TicToolStacks.isTicArmor(stack)) {
            return false;
        }

        EntityEquipmentSlot slot = TicToolStacks.getArmorSlot(stack);
        float divisor = getDefenseDivisor(slot);
        if (divisor <= 0.0F) {
            return false;
        }

        if (TicToolNbt.hasToken(stack, token)) {
            return true;
        }

        float current = TicToolNbt.getFloatStat(stack, "Defense");
        TicToolNbt.addToken(stack, token);
        TicToolNbt.setFloatStat(stack, "Defense", current + (amount / divisor));
        return true;
    }

    public static boolean addDrawSpeed(ItemStack stack, float amount, String token) {
        if (!canApply(stack, "DrawSpeed", token)) {
            return false;
        }
        if (TicToolNbt.hasToken(stack, token)) {
            return true;
        }

        float current = TicToolNbt.getFloatStat(stack, "DrawSpeed");
        float value = Math.max(MIN_DRAW_SPEED, current - amount);
        TicToolNbt.addToken(stack, token);
        TicToolNbt.setFloatStat(stack, "DrawSpeed", value);
        return true;
    }

    private static boolean addFloatStat(ItemStack stack, String statName, float amount, String token) {
        if (!canApply(stack, statName, token)) {
            return false;
        }
        if (TicToolNbt.hasToken(stack, token)) {
            return true;
        }

        float current = TicToolNbt.getFloatStat(stack, statName);
        TicToolNbt.addToken(stack, token);
        TicToolNbt.setFloatStat(stack, statName, current + amount);
        return true;
    }

    private static boolean addIntStat(ItemStack stack, String statName, int amount, String token) {
        if (!canApply(stack, statName, token)) {
            return false;
        }
        if (TicToolNbt.hasToken(stack, token)) {
            return true;
        }

        int current = TicToolNbt.getIntStat(stack, statName);
        TicToolNbt.addToken(stack, token);
        TicToolNbt.setIntStat(stack, statName, current + amount);
        return true;
    }

    private static boolean canApply(ItemStack stack, String statName, String token) {
        return TicToolStacks.isTicTarget(stack)
                && statName != null
                && token != null
                && !token.trim().isEmpty()
                && TicToolNbt.hasStat(stack, statName);
    }

    private static float getDefenseDivisor(EntityEquipmentSlot slot) {
        if (slot == null) {
            return 0.0F;
        }
        switch (slot) {
            case HEAD:
                return 0.16F;
            case CHEST:
                return 0.4F;
            case LEGS:
                return 0.3F;
            case FEET:
                return 0.14F;
            default:
                return 0.0F;
        }
    }
}
