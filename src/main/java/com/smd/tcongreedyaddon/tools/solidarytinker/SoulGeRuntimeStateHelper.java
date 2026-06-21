package com.smd.tcongreedyaddon.tools.solidarytinker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

public final class SoulGeRuntimeStateHelper {

    private static final String ROOT_TAG = "TConGreedySoulGeRuntime";
    private static final String MAIN_HAND_TAG = "MainHand";
    private static final String OFF_HAND_TAG = "OffHand";
    private static final String TAG_TEMPERATURE_RISE_TICK = "TemperatureRiseTick";
    private static final String TAG_TEMPERATURE_COOLDOWN_TICK = "TemperatureCooldownTick";
    private static final String TAG_ITEM_KEY = "ItemKey";

    private SoulGeRuntimeStateHelper() {
    }

    public static int getTemperatureRiseTick(EntityPlayer player, EnumHand hand, ItemStack stack) {
        return getHandState(player, hand, stack).getInteger(TAG_TEMPERATURE_RISE_TICK);
    }

    public static void setTemperatureRiseTick(EntityPlayer player, EnumHand hand, ItemStack stack, int value) {
        getHandState(player, hand, stack).setInteger(TAG_TEMPERATURE_RISE_TICK, Math.max(0, value));
    }

    public static int getTemperatureCooldownTick(EntityPlayer player, EnumHand hand, ItemStack stack) {
        return getHandState(player, hand, stack).getInteger(TAG_TEMPERATURE_COOLDOWN_TICK);
    }

    public static void setTemperatureCooldownTick(EntityPlayer player, EnumHand hand, ItemStack stack, int value) {
        getHandState(player, hand, stack).setInteger(TAG_TEMPERATURE_COOLDOWN_TICK, Math.max(0, value));
    }

    public static void clear(EntityPlayer player, EnumHand hand) {
        NBTTagCompound root = getRoot(player);
        root.removeTag(hand == EnumHand.MAIN_HAND ? MAIN_HAND_TAG : OFF_HAND_TAG);
    }

    private static NBTTagCompound getHandState(EntityPlayer player, EnumHand hand, ItemStack stack) {
        NBTTagCompound root = getRoot(player);
        String handKey = hand == EnumHand.MAIN_HAND ? MAIN_HAND_TAG : OFF_HAND_TAG;
        if (!root.hasKey(handKey, 10)) {
            root.setTag(handKey, new NBTTagCompound());
        }
        NBTTagCompound state = root.getCompoundTag(handKey);
        String itemKey = buildItemKey(stack);
        if (!itemKey.equals(state.getString(TAG_ITEM_KEY))) {
            state = new NBTTagCompound();
            state.setString(TAG_ITEM_KEY, itemKey);
            root.setTag(handKey, state);
        }
        return state;
    }

    private static NBTTagCompound getRoot(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(ROOT_TAG, 10)) {
            data.setTag(ROOT_TAG, new NBTTagCompound());
        }
        return data.getCompoundTag(ROOT_TAG);
    }

    private static String buildItemKey(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() == null || stack.getItem().getRegistryName() == null) {
            return "empty";
        }
        return stack.getItem().getRegistryName().toString();
    }
}
