package com.smd.tcongreedyaddon.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class TicToolTraits {

    private static final int DEFAULT_COLOR = 0xffffff;
    private static final int DEFAULT_LEVEL = 1;

    private TicToolTraits() {
    }

    public static String[] getTraits(ItemStack stack) {
        if (!TicToolStacks.isTicTarget(stack)) {
            return TicToolNbt.EMPTY_STRINGS;
        }
        return TicToolNbt.readStringList(TicToolNbt.getRoot(stack), "Traits");
    }

    public static boolean hasTrait(ItemStack stack, String traitId) {
        if (traitId == null || traitId.trim().isEmpty()) {
            return false;
        }
        for (String trait : getTraits(stack)) {
            if (traitId.equals(trait)) {
                return true;
            }
        }
        return false;
    }

    public static int getTraitColor(ItemStack stack, String traitId) {
        NBTTagCompound modifier = getModifierTag(stack, traitId);
        return modifier == null ? DEFAULT_COLOR : modifier.getInteger("color");
    }

    public static int getTraitLevel(ItemStack stack, String traitId) {
        NBTTagCompound modifier = getModifierTag(stack, traitId);
        return modifier == null ? DEFAULT_LEVEL : Math.max(DEFAULT_LEVEL, modifier.getInteger("level"));
    }

    public static boolean addTrait(ItemStack stack, String traitId, int color, int level) {
        if (!isValidTraitOperation(stack, traitId) || hasTrait(stack, traitId)) {
            return false;
        }

        NBTTagCompound root = TicToolNbt.getOrCreateRoot(stack);
        NBTTagList modifiers = TicToolNbt.getCompoundList(root, "Modifiers");
        NBTTagCompound modifier = new NBTTagCompound();
        modifier.setString("identifier", traitId);
        modifier.setInteger("color", color);
        modifier.setInteger("level", Math.max(DEFAULT_LEVEL, level));
        root.setTag("Modifiers", TicToolNbt.copyCompoundListAppending(modifiers, modifier));

        NBTTagList traits = TicToolNbt.getStringList(root, "Traits");
        root.setTag("Traits", TicToolNbt.copyListAppendingString(traits, traitId));
        syncTinkerDataModifier(root, traitId, true);
        return true;
    }

    public static boolean removeTrait(ItemStack stack, String traitId) {
        if (!isValidTraitOperation(stack, traitId)) {
            return false;
        }

        NBTTagCompound root = TicToolNbt.getRoot(stack);
        if (root == null) {
            return false;
        }

        boolean removed = false;
        NBTTagList modifiers = TicToolNbt.getCompoundList(root, "Modifiers");
        if (TicToolNbt.compoundListContainsIdentifier(modifiers, traitId)) {
            root.setTag("Modifiers", TicToolNbt.copyCompoundListWithoutIdentifier(modifiers, traitId));
            removed = true;
        }

        NBTTagList traits = TicToolNbt.getStringList(root, "Traits");
        if (TicToolNbt.stringListContains(traits, traitId)) {
            root.setTag("Traits", TicToolNbt.copyListWithoutString(traits, traitId));
            removed = true;
        }

        if (syncTinkerDataModifier(root, traitId, false)) {
            removed = true;
        }

        return removed;
    }

    public static ItemStack withTrait(ItemStack stack, String traitId, int color, int level) {
        if (TicToolStacks.isEmpty(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        addTrait(copy, traitId, color, level);
        return copy;
    }

    public static ItemStack withoutTrait(ItemStack stack, String traitId) {
        if (TicToolStacks.isEmpty(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        removeTrait(copy, traitId);
        return copy;
    }

    private static NBTTagCompound getModifierTag(ItemStack stack, String traitId) {
        if (!TicToolStacks.isTicTarget(stack) || traitId == null || traitId.trim().isEmpty()) {
            return null;
        }
        NBTTagList modifiers = TicToolNbt.getCompoundList(TicToolNbt.getRoot(stack), "Modifiers");
        for (int i = 0; i < modifiers.tagCount(); i++) {
            NBTTagCompound modifier = modifiers.getCompoundTagAt(i);
            if (traitId.equals(modifier.getString("identifier"))) {
                return modifier;
            }
        }
        return null;
    }

    private static boolean isValidTraitOperation(ItemStack stack, String traitId) {
        return TicToolStacks.isTicTarget(stack) && traitId != null && !traitId.trim().isEmpty();
    }

    private static boolean syncTinkerDataModifier(NBTTagCompound root, String traitId, boolean add) {
        NBTTagCompound tinkerData = TicToolNbt.getCompound(root, "TinkerData");
        if (tinkerData == null || !tinkerData.hasKey("Modifiers")) {
            return false;
        }

        NBTTagList modifiers = TicToolNbt.getStringList(tinkerData, "Modifiers");
        if (add) {
            if (!TicToolNbt.stringListContains(modifiers, traitId)) {
                tinkerData.setTag("Modifiers", TicToolNbt.copyListAppendingString(modifiers, traitId));
                return true;
            }
            return false;
        }

        if (TicToolNbt.stringListContains(modifiers, traitId)) {
            tinkerData.setTag("Modifiers", TicToolNbt.copyListWithoutString(modifiers, traitId));
            return true;
        }
        return false;
    }
}
