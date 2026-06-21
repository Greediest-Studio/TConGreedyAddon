package com.smd.tcongreedyaddon.tools.solidarytinker;

import com.smd.tcongreedyaddon.init.SoundsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class SoulGeClientState {

    private static final ClientRuntimeState MAIN = new ClientRuntimeState();
    private static final ClientRuntimeState OFF = new ClientRuntimeState();

    private SoulGeClientState() {
    }

    public static void updateSingleMode(EntityPlayer player, EnumHand hand, ItemStack stack,
                                        EntityLivingBase target, boolean singleMode) {
        ClientRuntimeState state = getState(hand);
        long worldTime = player.world.getTotalWorldTime();
        state.itemKey = buildItemKey(stack);
        state.singleMode = singleMode;
        state.lastSeenWorldTick = worldTime;
        state.targets.clear();
        if (target != null && target.isEntityAlive()) {
            state.targets.add(target.getUniqueID());
        }

        if (singleMode && target != null && target.isEntityAlive()) {
            if (player.ticksExisted % 5 == 0) {
                state.temperatureRiseTick++;
                state.temperatureCooldownTick = 12;
                maybePlayThresholdSound(player, state);
            }
        } else {
            state.temperatureRiseTick = 0;
            state.temperatureCooldownTick = 0;
        }
    }

    public static void updateMultiMode(EntityPlayer player, EnumHand hand, ItemStack stack,
                                       EntityLivingBase pointed, List<EntityLivingBase> markedTargets, boolean singleMode) {
        ClientRuntimeState state = getState(hand);
        long worldTime = player.world.getTotalWorldTime();
        state.itemKey = buildItemKey(stack);
        state.singleMode = singleMode;
        state.lastSeenWorldTick = worldTime;
        state.temperatureRiseTick = 0;
        state.temperatureCooldownTick = 0;
        state.targets.clear();

        if (pointed != null && pointed.isEntityAlive()) {
            state.targets.add(pointed.getUniqueID());
        }
        for (EntityLivingBase target : markedTargets) {
            if (target != null && target.isEntityAlive()) {
                UUID uuid = target.getUniqueID();
                if (!state.targets.contains(uuid)) {
                    state.targets.add(uuid);
                }
            }
        }
    }

    public static void tickPassive(EntityPlayer player, EnumHand hand, ItemStack stack) {
        ClientRuntimeState state = getState(hand);
        if (!buildItemKey(stack).equals(state.itemKey)) {
            reset(state);
            return;
        }
        if (player.ticksExisted % 5 == 0) {
            if (state.temperatureCooldownTick > 0) {
                state.temperatureCooldownTick--;
            } else {
                state.temperatureRiseTick = 0;
            }
        }
        if (player.world.getTotalWorldTime() - state.lastSeenWorldTick > 4) {
            state.targets.clear();
            if (!state.singleMode) {
                state.temperatureRiseTick = 0;
                state.temperatureCooldownTick = 0;
            }
        }
    }

    public static void clear(EntityPlayer player, EnumHand hand) {
        reset(getState(hand));
    }

    public static List<EntityLivingBase> getRenderTargets(EntityPlayer player, EnumHand hand) {
        ClientRuntimeState state = getState(hand);
        List<EntityLivingBase> targets = new ArrayList<>();
        Iterator<UUID> iterator = state.targets.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            EntityLivingBase target = findLiving(player, uuid);
            if (target == null || !target.isEntityAlive()) {
                iterator.remove();
                continue;
            }
            targets.add(target);
        }
        return targets;
    }

    public static boolean isExecuting(EntityPlayer player, EnumHand hand, EntityLivingBase target) {
        if (target == null) {
            return false;
        }
        return target.getEntityData().getInteger(SoulGe.ENTITY_TAG_EXECUTION_STAGE) > 0;
    }

    public static boolean isSingleMode(EntityPlayer player, EnumHand hand) {
        return getState(hand).singleMode;
    }

    public static int getTemperatureRiseTick(EntityPlayer player, EnumHand hand) {
        return getState(hand).temperatureRiseTick;
    }

    private static ClientRuntimeState getState(EnumHand hand) {
        return hand == EnumHand.OFF_HAND ? OFF : MAIN;
    }

    private static void reset(ClientRuntimeState state) {
        state.itemKey = "";
        state.temperatureRiseTick = 0;
        state.temperatureCooldownTick = 0;
        state.lastSeenWorldTick = 0;
        state.singleMode = false;
        state.targets.clear();
    }

    private static String buildItemKey(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() == null || stack.getItem().getRegistryName() == null) {
            return "empty";
        }
        return stack.getItem().getRegistryName().toString();
    }

    private static EntityLivingBase findLiving(EntityPlayer player, UUID uuid) {
        for (EntityLivingBase living : player.world.getEntitiesWithinAABB(EntityLivingBase.class,
                player.getEntityBoundingBox().grow(80.0d))) {
            if (uuid.equals(living.getUniqueID())) {
                return living;
            }
        }
        return null;
    }

    private static void maybePlayThresholdSound(EntityPlayer player, ClientRuntimeState state) {
        if (state.temperatureRiseTick == 8 || state.temperatureRiseTick == 24) {
            player.world.playSound(player, player.posX, player.posY, player.posZ,
                    SoundsHandler.SOULGE_BEAM_UP != null ? SoundsHandler.SOULGE_BEAM_UP : net.minecraft.init.SoundEvents.BLOCK_FIRE_AMBIENT,
                    SoundCategory.PLAYERS, 0.9f, state.temperatureRiseTick >= 24 ? 1.35f : 1.0f);
        }
    }

    private static final class ClientRuntimeState {
        private String itemKey = "";
        private int temperatureRiseTick;
        private int temperatureCooldownTick;
        private long lastSeenWorldTick;
        private boolean singleMode;
        private final List<UUID> targets = new ArrayList<>();
    }
}
