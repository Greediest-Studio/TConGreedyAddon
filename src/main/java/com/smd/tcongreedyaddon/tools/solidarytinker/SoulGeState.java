package com.smd.tcongreedyaddon.tools.solidarytinker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.utils.TagUtil;

public final class SoulGeState {

    private static final String TAG_IS_SINGLE_MODE = "SoulGeIsSingleMode";

    private SoulGeState() {
    }

    public static boolean isSingleMode(ItemStack stack) {
        return getStateTag(stack).getBoolean(TAG_IS_SINGLE_MODE);
    }

    public static void setSingleMode(ItemStack stack, boolean singleMode) {
        getStateTag(stack).setBoolean(TAG_IS_SINGLE_MODE, singleMode);
    }

    private static NBTTagCompound getStateTag(ItemStack stack) {
        NBTTagCompound root = TagUtil.getTagSafe(stack);
        if (!root.hasKey("TConGreedySoulGeState", 10)) {
            root.setTag("TConGreedySoulGeState", new NBTTagCompound());
        }
        return root.getCompoundTag("TConGreedySoulGeState");
    }
}
