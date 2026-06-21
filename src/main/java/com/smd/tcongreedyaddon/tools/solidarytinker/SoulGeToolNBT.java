package com.smd.tcongreedyaddon.tools.solidarytinker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;

public final class SoulGeToolNBT extends ToolNBT {

    private static final String TAG_DETECTION_RANGE = "SoulGeDetectionRange";
    private static final String TAG_EXERT_TIMES = "SoulGeExertTimes";
    private static final String TAG_ATTACK_FREQUENCY = "SoulGeAttackFrequency";
    private static final String TAG_KILL_THRESHOLD = "SoulGeKillThreshold";

    public float detectionRange;
    public int exertTimes;
    public int attackFrequency;
    public float killThreshold;

    public SoulGeToolNBT() {
    }

    public SoulGeToolNBT(NBTTagCompound tag) {
        super(tag);
    }

    public static SoulGeToolNBT from(ItemStack stack) {
        return new SoulGeToolNBT(TagUtil.getToolTag(stack));
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);
        requireKey(tag, TAG_DETECTION_RANGE, Constants.NBT.TAG_FLOAT);
        requireKey(tag, TAG_EXERT_TIMES, Constants.NBT.TAG_INT);
        requireKey(tag, TAG_ATTACK_FREQUENCY, Constants.NBT.TAG_INT);
        requireKey(tag, TAG_KILL_THRESHOLD, Constants.NBT.TAG_FLOAT);

        detectionRange = tag.getFloat(TAG_DETECTION_RANGE);
        exertTimes = tag.getInteger(TAG_EXERT_TIMES);
        attackFrequency = tag.getInteger(TAG_ATTACK_FREQUENCY);
        killThreshold = tag.getFloat(TAG_KILL_THRESHOLD);
    }

    @Override
    public void write(NBTTagCompound tag) {
        super.write(tag);
        tag.setFloat(TAG_DETECTION_RANGE, detectionRange);
        tag.setInteger(TAG_EXERT_TIMES, exertTimes);
        tag.setInteger(TAG_ATTACK_FREQUENCY, attackFrequency);
        tag.setFloat(TAG_KILL_THRESHOLD, killThreshold);
    }

    private static void requireKey(NBTTagCompound tag, String key, int type) {
        if (!tag.hasKey(key, type)) {
            throw new IllegalStateException("Missing SoulGe tool nbt key: " + key);
        }
    }
}
