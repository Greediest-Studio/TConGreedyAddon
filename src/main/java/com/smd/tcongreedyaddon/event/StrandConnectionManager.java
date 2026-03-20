package com.smd.tcongreedyaddon.event;

import com.smd.bulletapi.api.Battlefield;
import com.smd.bulletapi.api.BulletApi;
import com.smd.bulletapi.api.handle.BulletHandle;
import com.smd.bulletapi.common.CollisionContext;
import com.smd.bulletapi.common.collision.SphereShape;
import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.network.NetworkHandler;
import com.smd.tcongreedyaddon.network.StrandConnectionSyncPacket;
import com.smd.tcongreedyaddon.util.MagicBookHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import slimeknights.tconstruct.library.tools.ToolCore;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class StrandConnectionManager {
    private static final double MIN_ROPE_LENGTH = 2.4D;
    private static final double EMERGENCY_RELEASE_DISTANCE = 0.55D;
    private static final double CLOSE_RELEASE_RANGE_FACTOR = 0.10D;
    private static final double DETACH_OBTUSE_DOT = -0.02D;
    private static final int DETACH_WARNING_TICKS = 8;

    private static final int TRACTION_MIN_TICKS = 8;
    private static final int TRACTION_MAX_TICKS = 26;
    private static final double TRACTION_DIRECT_SPEED = 0.86D;
    private static final double TRACTION_SPEED_SCALE = 0.08D;
    private static final double TRACTION_STEER = 0.04D;
    private static final double TRACTION_MIN_PULL_WEIGHT = 0.15D;
    private static final double TRACTION_TANGENT_KEEP = 0.78D;
    private static final double TRACTION_BLEND_CURRENT = 0.72D;
    private static final double TRACTION_BLEND_TARGET = 0.28D;
    private static final double GRAVITY_COMPENSATION_BASE = 0.08D;
    private static final double GRAVITY_COMPENSATION_PER_BLOCK = 0.012D;
    private static final double GRAVITY_COMPENSATION_MIN = 0.0D;
    private static final double GRAVITY_COMPENSATION_MAX = 0.13D;
    private static final double VERTICAL_TARGET_SPEED_PER_BLOCK = 0.045D;
    private static final double VERTICAL_TARGET_SPEED_MAX = 0.18D;
    private static final double VERTICAL_STABILIZE_RANGE = 1.75D;
    private static final double VERTICAL_STABILIZE_NEAR = 0.62D;
    private static final double VERTICAL_STABILIZE_FAR = 0.16D;
    private static final double VIEW_PULL_ENTER_DOT = 0.52D;
    private static final double VIEW_PULL_EXIT_DOT = 0.30D;
    private static final double VIEW_ORBIT_ENTER_TANGENT = 0.38D;
    private static final double VIEW_ORBIT_EXIT_TANGENT = 0.24D;
    private static final double VIEW_ORBIT_READY_TICKS = 4;
    private static final double VIEW_ORBIT_MIN_TANGENTIAL = 0.12D;
    private static final double VIEW_PULL_REENTER_DISTANCE = 0.14D;
    private static final double TRACTION_PULL_WEIGHT_PULL = 0.82D;
    private static final double TRACTION_PULL_WEIGHT_ORBIT = 0.22D;
    private static final double TRACTION_PULL_WEIGHT_DRIFT = 0.10D;
    private static final double TRACTION_TANGENT_KEEP_PULL = 0.72D;
    private static final double TRACTION_TANGENT_KEEP_ORBIT = 0.97D;
    private static final double TRACTION_TANGENT_KEEP_DRIFT = 0.90D;
    private static final double TRACTION_STEER_PULL = 0.045D;
    private static final double TRACTION_STEER_ORBIT = 0.12D;
    private static final double TRACTION_STEER_DRIFT = 0.0D;
    private static final double SWING_STEER_ORBIT_SCALE = 1.0D;
    private static final double SWING_STEER_PULL_SCALE = 0.2D;
    private static final double SWING_STEER_DRIFT_SCALE = 0.0D;
    private static final double VERTICAL_SUPPORT_PULL = 0.24D;
    private static final double VERTICAL_SUPPORT_ORBIT = 1.0D;
    private static final double VERTICAL_SUPPORT_DRIFT = 0.0D;

    private static final double SWING_STEER = 0.03D;
    private static final double SWING_TENSION = 0.26D;
    private static final double SWING_SHORTEN_FACTOR = 0.028D;
    private static final double SWING_SHORTEN_BASE = 0.008D;
    private static final double GROUND_REENTRY_TANGENTIAL = 0.24D;
    private static final double SWING_ENTRY_TANGENTIAL = 0.52D;
    private static final double SWING_ENTRY_PULL_EPSILON = 0.18D;

    private static final double MAX_CONNECTION_SPEED = 2.65D;
    private static final int REUSE_GRAPPLE_COOLDOWN_TICKS = 3;

    private static final int MELEE_DASH_TICKS = 6;
    private static final int MELEE_GRACE_TICKS = 7;
    private static final double MELEE_DASH_SPEED = 1.1D;
    private static final double MELEE_COLLISION_GROW = 0.24D;
    private static final int MELEE_IMPACT_LIFE = 4;
    private static final double MELEE_IMPACT_RADIUS = 2.2D;
    private static final float MELEE_DAMAGE_MULTIPLIER = 5.25F;
    private static final double MELEE_KNOCKBACK_SPEED = 0.78D;
    private static final double MELEE_BOSS_KNOCKBACK_SCALE = 0.2D;
    private static final double MELEE_RECOIL_SPEED = 0.1D;
    private static final String IMPACT_HIT_KEY_PREFIX = "strand_grapple_hit_";

    private static final Map<UUID, ActiveConnection> ACTIVE_CONNECTIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, MeleeDashState> ACTIVE_MELEE_DASHES = new ConcurrentHashMap<>();
    private static final Map<UUID, MeleeGraceState> ACTIVE_MELEE_GRACES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> ACTIVE_REUSE_COOLDOWNS = new ConcurrentHashMap<>();

    private static volatile boolean clientConnectionActive;
    private static volatile boolean clientMeleeReady;

    private StrandConnectionManager() {
    }

    public static boolean connect(EntityPlayer player, ItemStack bookStack,
                                  StrandNodeManager.StrandNode node,
                                  double maxRange, double spellSpeed, boolean reusedNode) {
        if (player == null || player.world == null || player.world.isRemote || node == null) {
            return false;
        }
        if (!StrandNodeManager.isNodeValid(player.world, node)) {
            return false;
        }

        disconnectInternal(player, false, false);
        ACTIVE_MELEE_DASHES.remove(player.getUniqueID());
        ACTIVE_MELEE_GRACES.remove(player.getUniqueID());

        Vec3d anchorPos = node.anchorPos;
        Vec3d playerCenter = getPlayerCenter(player);
        double distance = playerCenter.distanceTo(anchorPos);
        if (distance < 0.001D) {
            return false;
        }

        ActiveConnection connection = new ActiveConnection(
                player.world.provider.getDimension(),
                node.bulletId,
                anchorPos,
                Math.max(2.0D, maxRange),
                Math.max(0.5D, spellSpeed),
                distance,
                reusedNode
        );
        ACTIVE_CONNECTIONS.put(player.getUniqueID(), connection);
        syncClientConnection(player, connection);

        player.fallDistance = 0.0F;
        player.velocityChanged = true;
        return true;
    }

    public static boolean disconnect(EntityPlayer player, boolean launchForward) {
        return disconnectInternal(player, launchForward, true);
    }

    private static boolean disconnectInternal(EntityPlayer player, boolean launchForward, boolean grantMeleeGrace) {
        if (player == null) {
            return false;
        }

        ActiveConnection connection = ACTIVE_CONNECTIONS.remove(player.getUniqueID());
        if (connection == null) {
            return false;
        }
        if (connection.reusedNode && player.world != null && !player.world.isRemote) {
            ACTIVE_REUSE_COOLDOWNS.put(
                    player.getUniqueID(),
                    player.world.getTotalWorldTime() + REUSE_GRAPPLE_COOLDOWN_TICKS
            );
        }
        if (grantMeleeGrace && connection.isMeleeReady()) {
            ACTIVE_MELEE_GRACES.put(player.getUniqueID(),
                    new MeleeGraceState(player.world.provider.getDimension(), MELEE_GRACE_TICKS));
        } else {
            ACTIVE_MELEE_GRACES.remove(player.getUniqueID());
        }
        syncClientDisconnect(player);

        if (launchForward && player.world != null && !player.world.isRemote) {
            Vec3d motion = getMotion(player);
            Vec3d toAnchor = connection.anchorPos.subtract(getPlayerCenter(player));
            double anchorDistanceSq = toAnchor.lengthSquared();
            if (anchorDistanceSq > 1.0E-6D) {
                Vec3d ropeDir = toAnchor.scale(1.0D / Math.sqrt(anchorDistanceSq));
                double towardSpeed = motion.dotProduct(ropeDir);
                if (towardSpeed > 0.0D) {
                    motion = motion.subtract(ropeDir.scale(towardSpeed));
                }
            }

            setMotion(player, motion);
            player.velocityChanged = true;
            player.fallDistance = 0.0F;
        }
        return true;
    }

    public static boolean tryStartMeleeAttack(EntityPlayer player) {
        if (player == null || player.world == null || player.world.isRemote) {
            return false;
        }

        ActiveConnection connection = ACTIVE_CONNECTIONS.get(player.getUniqueID());
        boolean meleeReady = connection != null && connection.isMeleeReady();
        boolean graceReady = !meleeReady && hasActiveMeleeGrace(player);
        if (!meleeReady && !graceReady) {
            return false;
        }

        Vec3d dashDir = horizontalize(player.getLookVec());
        if (dashDir.lengthSquared() < 1.0E-6D) {
            float yawRad = (float) Math.toRadians(player.rotationYaw);
            dashDir = new Vec3d(-Math.sin(yawRad), 0.0D, Math.cos(yawRad));
        }
        dashDir = normalizeOrZero(dashDir);
        if (dashDir.lengthSquared() < 1.0E-6D) {
            return false;
        }

        if (connection != null) {
            disconnectInternal(player, false, false);
        } else {
            ACTIVE_MELEE_GRACES.remove(player.getUniqueID());
            syncClientDisconnect(player);
        }

        ACTIVE_MELEE_DASHES.put(player.getUniqueID(),
                new MeleeDashState(player.world.provider.getDimension(), dashDir));
        setMotion(player, new Vec3d(dashDir.x * MELEE_DASH_SPEED, Math.max(player.motionY, 0.0D), dashDir.z * MELEE_DASH_SPEED));
        player.velocityChanged = true;
        player.fallDistance = 0.0F;
        return true;
    }

    public static boolean hasConnection(EntityPlayer player) {
        return player != null && ACTIVE_CONNECTIONS.containsKey(player.getUniqueID());
    }

    public static boolean hasReuseCooldown(EntityPlayer player) {
        return getReuseCooldownRemainingTicks(player) > 0;
    }

    public static int getReuseCooldownRemainingTicks(EntityPlayer player) {
        if (player == null || player.world == null) {
            return 0;
        }
        Long cooldownEnd = ACTIVE_REUSE_COOLDOWNS.get(player.getUniqueID());
        if (cooldownEnd == null) {
            return 0;
        }
        long remaining = cooldownEnd - player.world.getTotalWorldTime();
        if (remaining <= 0L) {
            ACTIVE_REUSE_COOLDOWNS.remove(player.getUniqueID());
            return 0;
        }
        return (int) remaining;
    }

    public static boolean isClientMeleeReady() {
        return clientMeleeReady;
    }

    public static int getClientVisualHash() {
        int hash = 1;
        hash = 31 * hash + (clientConnectionActive ? 1 : 0);
        hash = 31 * hash + (clientMeleeReady ? 1 : 0);
        return hash;
    }

    public static void setClientVisualState(boolean active, boolean meleeReady) {
        clientConnectionActive = active;
        clientMeleeReady = meleeReady;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        EntityPlayer player = event.player;
        if (player == null || player.world == null || player.world.isRemote) {
            return;
        }

        ActiveConnection connection = ACTIVE_CONNECTIONS.get(player.getUniqueID());
        if (connection != null) {
            maintainConnection(player, connection);
        } else {
            maintainMeleeGrace(player);
            maintainMeleeDash(player);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || event.phase != TickEvent.Phase.END) {
            return;
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        cleanupPlayerState(event.player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        cleanupPlayerState(event.player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        cleanupPlayerState(event.player);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!(event.getWorld() instanceof World)) {
            return;
        }

        int dimension = ((World) event.getWorld()).provider.getDimension();
        ACTIVE_CONNECTIONS.entrySet().removeIf(entry -> entry.getValue().dimension == dimension);
        ACTIVE_MELEE_DASHES.entrySet().removeIf(entry -> entry.getValue().dimension == dimension);
        ACTIVE_MELEE_GRACES.entrySet().removeIf(entry -> entry.getValue().dimension == dimension);
    }

    private static void maintainConnection(EntityPlayer player, ActiveConnection connection) {
        World world = player.world;
        if (connection.dimension != world.provider.getDimension()
                || !StrandNodeManager.isNodeValid(world, connection.bulletId)
                || player.isDead) {
            clearConnection(player, false);
            return;
        }

        Vec3d playerCenter = getPlayerCenter(player);
        Vec3d toAnchor = connection.anchorPos.subtract(playerCenter);
        double distance = toAnchor.length();
        if (distance < 1.0E-4D) {
            disconnect(player, false);
            return;
        }
        if (distance > connection.maxRange + 2.0D) {
            disconnect(player, false);
            return;
        }
        if (distance <= computeCloseReleaseDistance(connection)) {
            disconnect(player, false);
            return;
        }

        Vec3d ropeDir = toAnchor.scale(1.0D / distance);
        Vec3d motion = getMotion(player);
        double towardSpeed = motion.dotProduct(ropeDir);
        Vec3d tangentialMotion = motion.subtract(ropeDir.scale(towardSpeed));
        double tangentialSpeed = tangentialMotion.length();

        Vec3d look = normalizeOrZero(player.getLookVec());
        double lookDot = look.dotProduct(ropeDir);
        double lookTangentStrength = MathHelper.clamp(projectOnPlane(look, ropeDir).length(), 0.0D, 1.0D);
        connection.viewIntent = resolveViewIntent(connection, lookDot, lookTangentStrength, tangentialSpeed);
        if (lookDot < DETACH_OBTUSE_DOT) {
            connection.detachWarningTicks++;
        } else {
            connection.detachWarningTicks = 0;
        }

        if (connection.detachWarningTicks >= DETACH_WARNING_TICKS) {
            disconnect(player, false);
            return;
        }

        connection.ticksConnected++;
        connection.ropeLength = Math.min(connection.ropeLength, connection.maxRange);
        connection.ropeLength = Math.max(connection.ropeLength, connection.minRopeLength);

        if (shouldEnterSwing(connection, distance, tangentialSpeed)) {
            connection.phase = Phase.SWING;
        } else if (shouldReenterTraction(player, connection, tangentialSpeed, toAnchor.y)) {
            connection.phase = Phase.TRACTION;
        }

        Vec3d adjustedMotion = connection.phase == Phase.TRACTION
                ? applyTraction(player, connection, ropeDir, distance, motion, tangentialMotion)
                : applySwing(player, connection, ropeDir, distance, motion, towardSpeed, tangentialSpeed);

        adjustedMotion = clampMagnitude(adjustedMotion, MAX_CONNECTION_SPEED);
        setMotion(player, adjustedMotion);
        player.fallDistance = 0.0F;
        player.velocityChanged = true;
    }

    private static void maintainMeleeGrace(EntityPlayer player) {
        MeleeGraceState graceState = ACTIVE_MELEE_GRACES.get(player.getUniqueID());
        if (graceState == null) {
            return;
        }
        if (graceState.dimension != player.world.provider.getDimension() || player.isDead) {
            ACTIVE_MELEE_GRACES.remove(player.getUniqueID());
            syncClientDisconnect(player);
            return;
        }

        if (--graceState.ticksRemaining <= 0) {
            ACTIVE_MELEE_GRACES.remove(player.getUniqueID());
            syncClientDisconnect(player);
        }
    }

    private static void maintainMeleeDash(EntityPlayer player) {
        MeleeDashState dashState = ACTIVE_MELEE_DASHES.get(player.getUniqueID());
        if (dashState == null) {
            return;
        }
        if (dashState.dimension != player.world.provider.getDimension() || player.isDead) {
            ACTIVE_MELEE_DASHES.remove(player.getUniqueID());
            return;
        }

        dashState.ticksRemaining--;
        setMotion(player, new Vec3d(
                dashState.direction.x * MELEE_DASH_SPEED,
                Math.max(player.motionY, 0.0D),
                dashState.direction.z * MELEE_DASH_SPEED
        ));
        player.fallDistance = 0.0F;
        player.velocityChanged = true;

        EntityLivingBase collided = findDashCollisionTarget(player, dashState.direction);
        if (collided != null) {
            Vec3d impactPos = getPlayerCenter(player).add(getPlayerCenter(collided)).scale(0.5D);
            spawnMeleeImpactBullet(player, impactPos);
            ACTIVE_MELEE_DASHES.remove(player.getUniqueID());
            return;
        }

        if (dashState.ticksRemaining <= 0) {
            ACTIVE_MELEE_DASHES.remove(player.getUniqueID());
        }
    }

    private static boolean shouldEnterSwing(ActiveConnection connection, double distance, double tangentialSpeed) {
        if (connection.phase != Phase.TRACTION) {
            return false;
        }
        if (connection.viewIntent == ViewIntent.ORBIT
                && connection.ticksConnected >= VIEW_ORBIT_READY_TICKS
                && tangentialSpeed >= VIEW_ORBIT_MIN_TANGENTIAL) {
            return true;
        }

        boolean enoughTime = connection.ticksConnected >= TRACTION_MIN_TICKS;
        boolean fastEnough = tangentialSpeed >= SWING_ENTRY_TANGENTIAL;
        boolean reachedPullEnd = connection.ticksConnected >= TRACTION_MAX_TICKS
                && distance <= connection.ropeLength + SWING_ENTRY_PULL_EPSILON;
        return enoughTime && (fastEnough || reachedPullEnd);
    }

    private static boolean shouldReenterTraction(EntityPlayer player, ActiveConnection connection,
                                                 double tangentialSpeed, double verticalGap) {
        if (connection.phase == Phase.SWING
                && connection.viewIntent == ViewIntent.PULL
                && (connection.anchorPos.distanceTo(getPlayerCenter(player)) > connection.ropeLength + VIEW_PULL_REENTER_DISTANCE
                || tangentialSpeed < SWING_ENTRY_TANGENTIAL)) {
            return true;
        }
        return connection.phase == Phase.SWING
                && player.onGround
                && verticalGap > 0.35D
                && tangentialSpeed < GROUND_REENTRY_TANGENTIAL;
    }

    private static Vec3d applyTraction(EntityPlayer player, ActiveConnection connection, Vec3d ropeDir,
                                       double distance, Vec3d currentMotion, Vec3d tangentialMotion) {
        Vec3d motion = currentMotion;
        double rangeFactor = MathHelper.clamp(distance / Math.max(2.0D, connection.maxRange), 0.35D, 1.0D);
        double directSpeed = (TRACTION_DIRECT_SPEED + connection.speedFactor * TRACTION_SPEED_SCALE)
                * (0.85D + rangeFactor * 0.45D);
        double pullWeight = getTractionPullWeight(connection.viewIntent);
        double tangentKeep = getTractionTangentKeep(connection.viewIntent);
        double steerStrength = getTractionSteer(connection.viewIntent);

        Vec3d desired = ropeDir.scale(directSpeed * pullWeight).add(tangentialMotion.scale(tangentKeep));
        Vec3d lookTangent = projectOnPlane(normalizeOrZero(player.getLookVec()), ropeDir);
        double lookTangentLength = lookTangent.length();
        if (lookTangentLength > 1.0E-5D && steerStrength > 1.0E-5D) {
            desired = desired.add(lookTangent.scale(steerStrength / lookTangentLength));
        }

        motion = motion.scale(TRACTION_BLEND_CURRENT).add(desired.scale(TRACTION_BLEND_TARGET));
        motion = applyVerticalStabilization(connection, player, ropeDir, motion);
        connection.ropeLength = Math.max(connection.minRopeLength, Math.min(connection.ropeLength, distance - 0.08D));
        return motion;
    }

    private static Vec3d applySwing(EntityPlayer player, ActiveConnection connection, Vec3d ropeDir, double distance,
                                    Vec3d currentMotion, double towardSpeed, double tangentialSpeed) {
        Vec3d motion = currentMotion;
        if (distance >= connection.ropeLength && towardSpeed < 0.0D) {
            motion = motion.subtract(ropeDir.scale(towardSpeed));
        }

        if (distance > connection.ropeLength) {
            double stretch = distance - connection.ropeLength;
            motion = motion.add(ropeDir.scale(stretch * SWING_TENSION));
        }

        Vec3d lookTangent = projectOnPlane(normalizeOrZero(player.getLookVec()), ropeDir);
        double lookTangentLength = lookTangent.length();
        if (lookTangentLength > 1.0E-5D) {
            double viewFactor = MathHelper.clamp(lookTangentLength, 0.0D, 1.0D);
            double ropeFactor = MathHelper.clamp(connection.ropeLength / 9.0D, 0.5D, 1.45D);
            double steer = SWING_STEER * viewFactor * ropeFactor * getSwingSteerScale(connection.viewIntent);
            motion = motion.add(lookTangent.scale(steer / lookTangentLength));
        }

        if (tangentialSpeed > 0.05D) {
            double shorten = Math.min(0.18D, SWING_SHORTEN_BASE + tangentialSpeed * SWING_SHORTEN_FACTOR);
            connection.ropeLength = Math.max(connection.minRopeLength, connection.ropeLength - shorten);
        }

        return applyVerticalStabilization(connection, player, ropeDir, motion);
    }

    private static EntityLivingBase findDashCollisionTarget(EntityPlayer player, Vec3d direction) {
        AxisAlignedBB searchBox = player.getEntityBoundingBox()
                .grow(MELEE_COLLISION_GROW)
                .expand(direction.x * 0.45D, 0.15D, direction.z * 0.45D);

        EntityLivingBase closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (EntityLivingBase candidate : player.world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox)) {
            if (candidate == player || !candidate.isEntityAlive()) {
                continue;
            }
            double distance = candidate.getDistanceSq(player);
            if (distance < closestDistance) {
                closest = candidate;
                closestDistance = distance;
            }
        }
        return closest;
    }

    private static void spawnMeleeImpactBullet(EntityPlayer player, Vec3d impactPos) {
        if (player == null || player.world == null || player.world.isRemote) {
            return;
        }

        BulletHandle handle = BulletApi.builder(player.world)
                .position(impactPos)
                .velocity(Vec3d.ZERO)
                .life(MELEE_IMPACT_LIFE)
                .damage(0.0F)
                .size(4.0f)
                .collisionShape(new SphereShape(MELEE_IMPACT_RADIUS))
                .collisionFilter((world, bullet, entity) -> entity != null && entity != player && entity.isEntityAlive())
                .hitBehavior(StrandConnectionManager::onMeleeImpactHit)
                .shooter(player)
                .shooterHeldItem(player.getHeldItemMainhand())
                .set("strand_grapple_impact", true)
                .spawnHandle();
    }

    private static void onMeleeImpactHit(CollisionContext context) {
        context.canceled = true;
        if (!(context.hitEntity instanceof EntityLivingBase) || !(context.shooter instanceof EntityPlayer)) {
            return;
        }

        NBTTagCompound bulletData = context.bullet.getCustomData();
        if (bulletData == null) {
            bulletData = new NBTTagCompound();
        }
        String hitKey = IMPACT_HIT_KEY_PREFIX + context.hitEntity.getEntityId();
        if (bulletData.getBoolean(hitKey)) {
            return;
        }
        bulletData.setBoolean(hitKey, true);
        context.bullet.setCustomData(bulletData);

        EntityPlayer player = (EntityPlayer) context.shooter;
        ItemStack held = player.getHeldItemMainhand();
        if (!(held.getItem() instanceof ToolCore)) {
            return;
        }

        MagicBookHelper.attackEntityRight(
                held,
                (ToolCore) held.getItem(),
                player,
                context.hitEntity,
                MELEE_DAMAGE_MULTIPLIER,
                createPlayerMagicDamageSource(player)
        );
        applyMeleeRecoil(player, (EntityLivingBase) context.hitEntity);
        applyMeleeKnockback(player, (EntityLivingBase) context.hitEntity);
    }

    private static DamageSource createPlayerMagicDamageSource(EntityPlayer player) {
        return new EntityDamageSource("magic", player)
                .setDamageBypassesArmor()
                .setMagicDamage();
    }

    private static Vec3d horizontalize(Vec3d vector) {
        if (vector == null) {
            return Vec3d.ZERO;
        }
        return new Vec3d(vector.x, 0.0D, vector.z);
    }

    private static Vec3d projectOnPlane(Vec3d vector, Vec3d normal) {
        if (vector == null || normal == null) {
            return Vec3d.ZERO;
        }
        return vector.subtract(normal.scale(vector.dotProduct(normal)));
    }

    private static Vec3d normalizeOrZero(Vec3d vector) {
        if (vector == null) {
            return Vec3d.ZERO;
        }
        double length = vector.length();
        return length < 1.0E-8D ? Vec3d.ZERO : vector.scale(1.0D / length);
    }

    private static Vec3d clampMagnitude(Vec3d vector, double maxMagnitude) {
        double length = vector.length();
        if (length <= maxMagnitude || length < 1.0E-8D) {
            return vector;
        }
        return vector.scale(maxMagnitude / length);
    }

    private static double computeVerticalCompensation(ActiveConnection connection, EntityPlayer player) {
        if (connection == null || player == null) {
            return 0.0D;
        }

        double verticalGap = connection.anchorPos.y - getPlayerCenter(player).y;
        return MathHelper.clamp(
                GRAVITY_COMPENSATION_BASE + verticalGap * GRAVITY_COMPENSATION_PER_BLOCK,
                GRAVITY_COMPENSATION_MIN,
                GRAVITY_COMPENSATION_MAX
        );
    }

    private static Vec3d applyVerticalStabilization(ActiveConnection connection, EntityPlayer player,
                                                    Vec3d ropeDir, Vec3d motion) {
        if (connection == null || player == null || motion == null) {
            return motion == null ? Vec3d.ZERO : motion;
        }

        double supportFactor = computeVerticalSupportFactor(connection);
        if (supportFactor <= 1.0E-4D) {
            return motion;
        }

        double verticalGap = connection.anchorPos.y - getPlayerCenter(player).y;
        double compensatedY = motion.y + computeVerticalCompensation(connection, player) * supportFactor;
        double targetY = MathHelper.clamp(
                verticalGap * VERTICAL_TARGET_SPEED_PER_BLOCK,
                -VERTICAL_TARGET_SPEED_MAX,
                VERTICAL_TARGET_SPEED_MAX
        ) * supportFactor;
        double proximity = 1.0D - MathHelper.clamp(Math.abs(verticalGap) / VERTICAL_STABILIZE_RANGE, 0.0D, 1.0D);
        double stabilizeFactor = VERTICAL_STABILIZE_FAR
                + (VERTICAL_STABILIZE_NEAR - VERTICAL_STABILIZE_FAR) * proximity;
        double stabilizedY = compensatedY + (targetY - compensatedY) * stabilizeFactor;
        return new Vec3d(motion.x, stabilizedY, motion.z);
    }

    private static ViewIntent resolveViewIntent(ActiveConnection connection, double lookDot,
                                                double lookTangentStrength, double tangentialSpeed) {
        if (connection == null) {
            return ViewIntent.DRIFT;
        }

        ViewIntent previous = connection.viewIntent;
        if (previous == ViewIntent.PULL && lookDot >= VIEW_PULL_EXIT_DOT) {
            return ViewIntent.PULL;
        }
        if (previous == ViewIntent.ORBIT
                && lookTangentStrength >= VIEW_ORBIT_EXIT_TANGENT
                && lookDot < VIEW_PULL_ENTER_DOT) {
            return ViewIntent.ORBIT;
        }
        if (lookDot >= VIEW_PULL_ENTER_DOT) {
            return ViewIntent.PULL;
        }
        if (lookTangentStrength >= VIEW_ORBIT_ENTER_TANGENT
                && (tangentialSpeed >= VIEW_ORBIT_MIN_TANGENTIAL
                || connection.ticksConnected >= VIEW_ORBIT_READY_TICKS)) {
            return ViewIntent.ORBIT;
        }
        return ViewIntent.DRIFT;
    }

    private static double getTractionPullWeight(ViewIntent intent) {
        switch (intent) {
            case PULL:
                return TRACTION_PULL_WEIGHT_PULL;
            case ORBIT:
                return TRACTION_PULL_WEIGHT_ORBIT;
            default:
                return TRACTION_PULL_WEIGHT_DRIFT;
        }
    }

    private static double getTractionTangentKeep(ViewIntent intent) {
        switch (intent) {
            case PULL:
                return TRACTION_TANGENT_KEEP_PULL;
            case ORBIT:
                return TRACTION_TANGENT_KEEP_ORBIT;
            default:
                return TRACTION_TANGENT_KEEP_DRIFT;
        }
    }

    private static double getTractionSteer(ViewIntent intent) {
        switch (intent) {
            case PULL:
                return TRACTION_STEER_PULL;
            case ORBIT:
                return TRACTION_STEER_ORBIT;
            default:
                return TRACTION_STEER_DRIFT;
        }
    }

    private static double getSwingSteerScale(ViewIntent intent) {
        switch (intent) {
            case PULL:
                return SWING_STEER_PULL_SCALE;
            case ORBIT:
                return SWING_STEER_ORBIT_SCALE;
            default:
                return SWING_STEER_DRIFT_SCALE;
        }
    }

    private static double computeVerticalSupportFactor(ActiveConnection connection) {
        if (connection == null) {
            return 0.0D;
        }
        switch (connection.viewIntent) {
            case PULL:
                return VERTICAL_SUPPORT_PULL;
            case ORBIT:
                return VERTICAL_SUPPORT_ORBIT;
            default:
                return VERTICAL_SUPPORT_DRIFT;
        }
    }

    private static double computeCloseReleaseDistance(ActiveConnection connection) {
        if (connection == null) {
            return EMERGENCY_RELEASE_DISTANCE;
        }
        return Math.max(EMERGENCY_RELEASE_DISTANCE, connection.maxRange * CLOSE_RELEASE_RANGE_FACTOR);
    }

    private static Vec3d getMotion(EntityPlayer player) {
        return new Vec3d(player.motionX, player.motionY, player.motionZ);
    }

    private static void setMotion(EntityPlayer player, Vec3d motion) {
        player.motionX = motion.x;
        player.motionY = motion.y;
        player.motionZ = motion.z;
    }

    private static Vec3d getPlayerCenter(Entity entity) {
        return new Vec3d(entity.posX, entity.posY + entity.height * 0.5D, entity.posZ);
    }

    private static void cleanupPlayerState(EntityPlayer player) {
        if (player == null) {
            return;
        }
        clearConnection(player, false);
        ACTIVE_MELEE_DASHES.remove(player.getUniqueID());
        ACTIVE_MELEE_GRACES.remove(player.getUniqueID());
        ACTIVE_REUSE_COOLDOWNS.remove(player.getUniqueID());
    }

    private static void clearConnection(EntityPlayer player, boolean grantMeleeGrace) {
        if (player == null) {
            return;
        }
        if (ACTIVE_CONNECTIONS.remove(player.getUniqueID()) != null) {
            if (grantMeleeGrace) {
                ACTIVE_MELEE_GRACES.put(player.getUniqueID(),
                        new MeleeGraceState(player.world.provider.getDimension(), MELEE_GRACE_TICKS));
            } else {
                ACTIVE_MELEE_GRACES.remove(player.getUniqueID());
            }
            syncClientDisconnect(player);
        }
    }

    private static void syncClientConnection(EntityPlayer player, ActiveConnection connection) {
        if (!(player instanceof EntityPlayerMP) || connection == null) {
            return;
        }
        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        NetworkHandler.INSTANCE.sendTo(new StrandConnectionSyncPacket(
                true,
                connection.isMeleeReady(),
                connection.dimension,
                connection.anchorPos.x,
                connection.anchorPos.y,
                connection.anchorPos.z
        ), playerMP);
    }

    private static void syncClientDisconnect(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        NetworkHandler.INSTANCE.sendTo(
                new StrandConnectionSyncPacket(false, hasActiveMeleeGrace(player), 0, 0.0D, 0.0D, 0.0D),
                (EntityPlayerMP) player
        );
    }

    private static boolean hasActiveMeleeGrace(EntityPlayer player) {
        if (player == null || player.world == null) {
            return false;
        }
        MeleeGraceState graceState = ACTIVE_MELEE_GRACES.get(player.getUniqueID());
        return graceState != null
                && graceState.dimension == player.world.provider.getDimension()
                && graceState.ticksRemaining > 0;
    }

    private static void applyMeleeKnockback(EntityPlayer player, EntityLivingBase target) {
        if (player == null || target == null) {
            return;
        }

        Vec3d pushDir = horizontalize(player.getLookVec());
        if (pushDir.lengthSquared() < 1.0E-6D) {
            pushDir = horizontalize(getPlayerCenter(target).subtract(getPlayerCenter(player)));
        }
        pushDir = normalizeOrZero(pushDir);
        if (pushDir.lengthSquared() < 1.0E-6D) {
            return;
        }

        double strength = isBossLike(target)
                ? MELEE_KNOCKBACK_SPEED * MELEE_BOSS_KNOCKBACK_SCALE
                : MELEE_KNOCKBACK_SPEED;
        target.motionX += pushDir.x * strength;
        target.motionZ += pushDir.z * strength;
        target.velocityChanged = true;
    }

    private static void applyMeleeRecoil(EntityPlayer player, EntityLivingBase target) {
        if (player == null || target == null) {
            return;
        }

        Vec3d recoilDir = horizontalize(getPlayerCenter(player).subtract(getPlayerCenter(target)));
        recoilDir = normalizeOrZero(recoilDir);
        setMotion(player, new Vec3d(recoilDir.x * MELEE_RECOIL_SPEED, 0.0D, recoilDir.z * MELEE_RECOIL_SPEED));
        player.velocityChanged = true;
        player.fallDistance = 0.0F;
    }

    private static boolean isBossLike(EntityLivingBase target) {
        return target != null
                && (!target.isNonBoss()
                || target instanceof EntityDragon
                || target instanceof EntityWither);
    }

    private enum Phase {
        TRACTION,
        SWING
    }

    private enum ViewIntent {
        PULL,
        ORBIT,
        DRIFT
    }

    private static final class ActiveConnection {
        private final int dimension;
        private final int bulletId;
        private final Vec3d anchorPos;
        private final double maxRange;
        private final double speedFactor;
        private final double minRopeLength;
        private final boolean reusedNode;
        private final boolean meleeArmed = true;
        private Phase phase = Phase.TRACTION;
        private ViewIntent viewIntent = ViewIntent.PULL;
        private int ticksConnected;
        private int detachWarningTicks;
        private double ropeLength;

        private ActiveConnection(int dimension, int bulletId, Vec3d anchorPos,
                                 double maxRange, double speedFactor, double initialDistance,
                                 boolean reusedNode) {
            this.dimension = dimension;
            this.bulletId = bulletId;
            this.anchorPos = anchorPos;
            this.maxRange = maxRange;
            this.speedFactor = speedFactor;
            this.minRopeLength = Math.max(MIN_ROPE_LENGTH, initialDistance * 0.24D);
            this.ropeLength = Math.min(maxRange, initialDistance);
            this.reusedNode = reusedNode;
        }

        private boolean isMeleeReady() {
            return meleeArmed;
        }
    }

    private static final class MeleeDashState {
        private final int dimension;
        private final Vec3d direction;
        private int ticksRemaining = MELEE_DASH_TICKS;

        private MeleeDashState(int dimension, Vec3d direction) {
            this.dimension = dimension;
            this.direction = direction;
        }
    }

    private static final class MeleeGraceState {
        private final int dimension;
        private int ticksRemaining;

        private MeleeGraceState(int dimension, int ticksRemaining) {
            this.dimension = dimension;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
