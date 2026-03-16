package com.smd.tcongreedyaddon.event;

import com.smd.bulletapi.api.LaserApi;
import com.smd.bulletapi.api.handle.LaserHandle;
import com.smd.tcongreedyaddon.Tags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FreezeRayControlManager {

    private static final UUID FREEZE_RAY_SLOW_UUID = UUID.fromString("8b3df6ff-37e6-4f1c-a8b8-19e704db4740");
    private static final String FREEZE_RAY_SLOW_NAME = "tcongreedyaddon.freeze_ray_slow";
    private static final int SLOW_30_THRESHOLD = 40;
    private static final int SLOW_70_THRESHOLD = 100;
    private static final int FREEZE_THRESHOLD = 160;
    private static final int FREEZE_TICKS = 60;
    private static final double SLOW_30_AMOUNT = -0.3D;
    private static final double SLOW_70_AMOUNT = -0.7D;
    private static final double FREEZE_AMOUNT = -0.999D;

    private static final Map<Integer, Map<Integer, TargetState>> WORLD_TARGET_STATES = new HashMap<>();
    private static final Map<Integer, Map<Integer, Long>> WORLD_ACTIVE_LASERS = new HashMap<>();

    private FreezeRayControlManager() {
    }

    public static void markHit(EntityLivingBase target) {
        if (target == null || target.world == null || target.world.isRemote || !target.isEntityAlive()) {
            return;
        }

        Map<Integer, TargetState> targetStates = WORLD_TARGET_STATES.computeIfAbsent(
                target.world.provider.getDimension(), dim -> new HashMap<>());
        TargetState state = targetStates.computeIfAbsent(target.getEntityId(), id -> new TargetState(id));
        state.hitThisTick = true;
    }

    public static void markLaserActive(World world, int laserId) {
        if (world == null || world.isRemote || laserId < 0) {
            return;
        }

        WORLD_ACTIVE_LASERS
                .computeIfAbsent(world.provider.getDimension(), dim -> new HashMap<>())
                .put(laserId, world.getTotalWorldTime());
    }

    public static void unregisterLaser(World world, int laserId) {
        if (world == null || world.isRemote) {
            return;
        }

        Map<Integer, Long> lasers = WORLD_ACTIVE_LASERS.get(world.provider.getDimension());
        if (lasers != null) {
            lasers.remove(laserId);
            if (lasers.isEmpty()) {
                WORLD_ACTIVE_LASERS.remove(world.provider.getDimension());
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (world.isRemote || event.phase != TickEvent.Phase.END) {
            return;
        }

        updateTargets(world);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!(event.getWorld() instanceof World)) {
            return;
        }

        World world = (World) event.getWorld();
        int dimension = world.provider.getDimension();

        Map<Integer, TargetState> states = WORLD_TARGET_STATES.remove(dimension);
        if (states != null) {
            for (TargetState state : states.values()) {
                Entity entity = world.getEntityByID(state.entityId);
                if (entity instanceof EntityLivingBase) {
                    removeSlowModifier((EntityLivingBase) entity);
                }
            }
        }

        Map<Integer, Long> lasers = WORLD_ACTIVE_LASERS.remove(dimension);
        if (lasers != null) {
            for (Integer laserId : lasers.keySet()) {
                LaserHandle handle = LaserApi.handle(world, laserId);
                if (handle.exists()) {
                    handle.remove();
                }
            }
        }
    }

    private static void updateTargets(World world) {
        Map<Integer, TargetState> states = WORLD_TARGET_STATES.get(world.provider.getDimension());
        if (states == null || states.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<Integer, TargetState>> iterator = states.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, TargetState> entry = iterator.next();
            TargetState state = entry.getValue();
            Entity entity = world.getEntityByID(state.entityId);
            if (!(entity instanceof EntityLivingBase) || entity.isDead) {
                if (entity instanceof EntityLivingBase) {
                    removeSlowModifier((EntityLivingBase) entity);
                }
                iterator.remove();
                continue;
            }

            EntityLivingBase living = (EntityLivingBase) entity;
            if (state.hitThisTick) {
                state.exposureTicks++;
            } else if (state.exposureTicks > 0) {
                state.exposureTicks--;
            }

            if (state.freezeTicksRemaining > 0) {
                state.freezeTicksRemaining--;
            } else if (state.exposureTicks > FREEZE_THRESHOLD && living.isNonBoss()) {
                state.freezeTicksRemaining = FREEZE_TICKS;
                state.exposureTicks = 0;
            }

            double slowAmount = getSlowAmount(state);
            if (slowAmount == 0.0D) {
                removeSlowModifier(living);
            } else {
                applySlowModifier(living, slowAmount);
            }

            state.hitThisTick = false;
            if (state.exposureTicks <= 0 && state.freezeTicksRemaining <= 0) {
                removeSlowModifier(living);
                iterator.remove();
            }
        }

        if (states.isEmpty()) {
            WORLD_TARGET_STATES.remove(world.provider.getDimension());
        }
    }

    private static double getSlowAmount(TargetState state) {
        if (state.freezeTicksRemaining > 0) {
            return FREEZE_AMOUNT;
        }
        if (state.exposureTicks > SLOW_70_THRESHOLD) {
            return SLOW_70_AMOUNT;
        }
        if (state.exposureTicks > SLOW_30_THRESHOLD) {
            return SLOW_30_AMOUNT;
        }
        return 0.0D;
    }

    private static void applySlowModifier(EntityLivingBase entity, double amount) {
        IAttributeInstance attribute = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }

        AttributeModifier existing = attribute.getModifier(FREEZE_RAY_SLOW_UUID);
        if (existing != null && existing.getAmount() == amount) {
            return;
        }

        if (existing != null) {
            attribute.removeModifier(existing);
        }
        attribute.applyModifier(new AttributeModifier(FREEZE_RAY_SLOW_UUID, FREEZE_RAY_SLOW_NAME, amount, 2));
    }

    private static void removeSlowModifier(EntityLivingBase entity) {
        IAttributeInstance attribute = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }

        AttributeModifier existing = attribute.getModifier(FREEZE_RAY_SLOW_UUID);
        if (existing != null) {
            attribute.removeModifier(existing);
        }
    }

    private static final class TargetState {
        private final int entityId;
        private int exposureTicks;
        private int freezeTicksRemaining;
        private boolean hitThisTick;

        private TargetState(int entityId) {
            this.entityId = entityId;
        }
    }
}
