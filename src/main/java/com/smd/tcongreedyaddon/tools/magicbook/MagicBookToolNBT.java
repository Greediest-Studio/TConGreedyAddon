package com.smd.tcongreedyaddon.tools.magicbook;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;

public final class MagicBookToolNBT extends ToolNBT {

    private static final String TAG_RANGE = "MagicBookRange";
    private static final String TAG_CRIT_CHANCE = "MagicBookCritChance";
    private static final String TAG_SPELL_SPEED = "MagicBookSpellSpeed";
    private static final String TAG_LEFT_SLOTS = "MagicBookLeftSlots";
    private static final String TAG_RIGHT_SLOTS = "MagicBookRightSlots";

    public float range;
    public float critChance;
    public int spellSpeed;
    public int leftSlots;
    public int rightSlots;

    public MagicBookToolNBT() {
    }

    public MagicBookToolNBT(NBTTagCompound tag) {
        super(tag);
    }

    public static MagicBookToolNBT from(ItemStack stack) {
        return new MagicBookToolNBT(TagUtil.getToolTag(stack));
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);
        requireKey(tag, TAG_RANGE, Constants.NBT.TAG_FLOAT);
        requireKey(tag, TAG_CRIT_CHANCE, Constants.NBT.TAG_FLOAT);
        requireKey(tag, TAG_SPELL_SPEED, Constants.NBT.TAG_INT);
        requireKey(tag, TAG_LEFT_SLOTS, Constants.NBT.TAG_INT);
        requireKey(tag, TAG_RIGHT_SLOTS, Constants.NBT.TAG_INT);

        range = tag.getFloat(TAG_RANGE);
        critChance = tag.getFloat(TAG_CRIT_CHANCE);
        spellSpeed = tag.getInteger(TAG_SPELL_SPEED);
        leftSlots = tag.getInteger(TAG_LEFT_SLOTS);
        rightSlots = tag.getInteger(TAG_RIGHT_SLOTS);
    }

    @Override
    public void write(NBTTagCompound tag) {
        super.write(tag);
        tag.setFloat(TAG_RANGE, range);
        tag.setFloat(TAG_CRIT_CHANCE, critChance);
        tag.setInteger(TAG_SPELL_SPEED, spellSpeed);
        tag.setInteger(TAG_LEFT_SLOTS, leftSlots);
        tag.setInteger(TAG_RIGHT_SLOTS, rightSlots);
    }

    private static void requireKey(NBTTagCompound tag, String key, int type) {
        if (!tag.hasKey(key, type)) {
            throw new IllegalStateException("Missing magicbook tool nbt key: " + key);
        }
    }
}
