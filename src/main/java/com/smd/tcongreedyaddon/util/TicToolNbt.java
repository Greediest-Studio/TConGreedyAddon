package com.smd.tcongreedyaddon.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

public final class TicToolNbt {

    public static final String[] EMPTY_STRINGS = new String[0];

    private static final int TAG_STRING = 8;
    private static final int TAG_COMPOUND = 10;

    private static final String DATA = "TConGreedyData";
    private static final String APPLIED_TOKENS = "AppliedTokens";

    private TicToolNbt() {
    }

    public static NBTTagCompound getRoot(ItemStack stack) {
        return stack == null ? null : stack.getTagCompound();
    }

    public static NBTTagCompound getOrCreateRoot(ItemStack stack) {
        if (stack == null) {
            return new NBTTagCompound();
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    public static NBTTagCompound getCompound(NBTTagCompound parent, String key) {
        if (parent == null || !parent.hasKey(key, TAG_COMPOUND)) {
            return null;
        }
        return parent.getCompoundTag(key);
    }

    public static NBTTagCompound getOrCreateCompound(NBTTagCompound parent, String key) {
        if (parent == null) {
            return new NBTTagCompound();
        }
        if (!parent.hasKey(key, TAG_COMPOUND)) {
            parent.setTag(key, new NBTTagCompound());
        }
        return parent.getCompoundTag(key);
    }

    public static NBTTagList getStringList(NBTTagCompound parent, String key) {
        if (parent == null || !parent.hasKey(key)) {
            return new NBTTagList();
        }
        return parent.getTagList(key, TAG_STRING);
    }

    public static NBTTagList getCompoundList(NBTTagCompound parent, String key) {
        if (parent == null || !parent.hasKey(key)) {
            return new NBTTagList();
        }
        return parent.getTagList(key, TAG_COMPOUND);
    }

    public static String[] readStringList(NBTTagCompound parent, String key) {
        NBTTagList list = getStringList(parent, key);
        if (list.tagCount() == 0) {
            return EMPTY_STRINGS;
        }
        String[] values = new String[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            values[i] = list.getStringTagAt(i);
        }
        return values;
    }

    public static NBTTagList toStringList(List<String> values) {
        NBTTagList list = new NBTTagList();
        if (values == null) {
            return list;
        }
        for (String value : values) {
            if (value != null) {
                list.appendTag(new net.minecraft.nbt.NBTTagString(value));
            }
        }
        return list;
    }

    public static List<String> toJavaList(NBTTagList list) {
        List<String> values = new ArrayList<>();
        if (list == null) {
            return values;
        }
        for (int i = 0; i < list.tagCount(); i++) {
            values.add(list.getStringTagAt(i));
        }
        return values;
    }

    public static boolean hasStat(ItemStack stack, String name) {
        NBTTagCompound stats = getStats(stack);
        return stats != null && stats.hasKey(name);
    }

    public static NBTTagCompound getStats(ItemStack stack) {
        return getCompound(getRoot(stack), "Stats");
    }

    public static NBTTagCompound getOrCreateStats(ItemStack stack) {
        return getOrCreateCompound(getOrCreateRoot(stack), "Stats");
    }

    public static NBTTagCompound getOrCreateStatsOriginal(ItemStack stack) {
        return getOrCreateCompound(getOrCreateRoot(stack), "StatsOriginal");
    }

    public static NBTTagCompound getTinkerData(ItemStack stack) {
        return getCompound(getRoot(stack), "TinkerData");
    }

    public static float getFloatStat(ItemStack stack, String name) {
        NBTTagCompound stats = getStats(stack);
        return stats == null ? 0.0F : stats.getFloat(name);
    }

    public static int getIntStat(ItemStack stack, String name) {
        NBTTagCompound stats = getStats(stack);
        return stats == null ? 0 : stats.getInteger(name);
    }

    public static void setFloatStat(ItemStack stack, String name, float value) {
        getOrCreateStats(stack).setFloat(name, value);
        getOrCreateStatsOriginal(stack).setFloat(name, value);
    }

    public static void setIntStat(ItemStack stack, String name, int value) {
        getOrCreateStats(stack).setInteger(name, value);
        getOrCreateStatsOriginal(stack).setInteger(name, value);
    }

    public static boolean hasToken(ItemStack stack, String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        for (String existing : getTokens(stack)) {
            if (token.equals(existing)) {
                return true;
            }
        }
        return false;
    }

    public static String[] getTokens(ItemStack stack) {
        NBTTagCompound root = getRoot(stack);
        NBTTagCompound data = getCompound(root, DATA);
        if (data == null) {
            return EMPTY_STRINGS;
        }
        return readStringList(data, APPLIED_TOKENS);
    }

    public static boolean addToken(ItemStack stack, String token) {
        if (stack == null || token == null || token.trim().isEmpty()) {
            return false;
        }
        if (hasToken(stack, token)) {
            return true;
        }

        NBTTagCompound data = getOrCreateCompound(getOrCreateRoot(stack), DATA);
        NBTTagList tokens = getStringList(data, APPLIED_TOKENS);
        tokens.appendTag(new net.minecraft.nbt.NBTTagString(token));
        data.setTag(APPLIED_TOKENS, tokens);
        return true;
    }

    public static NBTTagList copyListWithoutString(NBTTagList source, String value) {
        NBTTagList result = new NBTTagList();
        if (source == null) {
            return result;
        }
        for (int i = 0; i < source.tagCount(); i++) {
            String entry = source.getStringTagAt(i);
            if (!entry.equals(value)) {
                result.appendTag(new net.minecraft.nbt.NBTTagString(entry));
            }
        }
        return result;
    }

    public static NBTTagList copyListAppendingString(NBTTagList source, String value) {
        NBTTagList result = new NBTTagList();
        if (source != null) {
            for (int i = 0; i < source.tagCount(); i++) {
                result.appendTag(new net.minecraft.nbt.NBTTagString(source.getStringTagAt(i)));
            }
        }
        result.appendTag(new net.minecraft.nbt.NBTTagString(value));
        return result;
    }

    public static boolean stringListContains(NBTTagList list, String value) {
        if (list == null || value == null) {
            return false;
        }
        for (int i = 0; i < list.tagCount(); i++) {
            if (value.equals(list.getStringTagAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static NBTTagList copyCompoundListWithoutIdentifier(NBTTagList source, String identifier) {
        NBTTagList result = new NBTTagList();
        if (source == null) {
            return result;
        }
        for (int i = 0; i < source.tagCount(); i++) {
            NBTTagCompound compound = source.getCompoundTagAt(i);
            if (!identifier.equals(compound.getString("identifier"))) {
                result.appendTag(compound.copy());
            }
        }
        return result;
    }

    public static boolean compoundListContainsIdentifier(NBTTagList list, String identifier) {
        if (list == null || identifier == null) {
            return false;
        }
        for (int i = 0; i < list.tagCount(); i++) {
            if (identifier.equals(list.getCompoundTagAt(i).getString("identifier"))) {
                return true;
            }
        }
        return false;
    }

    public static NBTTagList copyCompoundListAppending(NBTTagList source, NBTTagCompound value) {
        NBTTagList result = new NBTTagList();
        if (source != null) {
            for (int i = 0; i < source.tagCount(); i++) {
                NBTBase tag = source.get(i);
                result.appendTag(tag.copy());
            }
        }
        result.appendTag(value);
        return result;
    }
}
