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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
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
    private static final double DETACH_OBTUSE_DOT = -0.02D;
    private static final int DETACH_WARNING_TICKS = 8;

    private static final int TRACTION_MIN_TICKS = 8;
    private static final int TRACTION_MAX_TICKS = 26;
    private static final double TRACTION_DIRECT_SPEED = 0.86D;
    private static final double TRACTION_SPEED_SCALE = 0.08D;
    private static final double TRACTION_STEER = 0.04D;
    private static final double TRACTION_TANGENT_KEEP = 0.52D;
    private static final double TRACTION_GRAVITY_CANCEL = 0.072D;
    private static final double TRACTION_VERTICAL_BONUS = 0.12D;

    private static final double SWING_STEER = 0.03D;
    private static final double SWING_TENSION = 0.3D;
    private static final double SWING_GRAVITY_BLEND = 0.52D;
    private static final double SWING_SHORTEN_FACTOR = 0.028D;
    private static final double SWING_SHORTEN_BASE = 0.008D;
    private static final double GROUND_REENTRY_TANGENTIAL = 0.24D;
    private static final double SWING_ENTRY_TANGENTIAL = 0.52D;
    private static final double SWING_ENTRY_PULL_EPSILON = 0.18D;

    private static final double MAX_CONNECTION_SPEED = 2.65D;

    private static final int MELEE_DASH_TICKS = 6;
    private static final double MELEE_DASH_SPEED = 1.1D;
    private static final double MELEE_COLLISION_GROW = 0.24D;
    private static final int MELEE_IMPACT_LIFE = 4;
    private static final double MELEE_IMPACT_RADIUS = 2.2D;
    private static final float MELEE_DAMAGE_MULTIPLIER = 1.1F;

    private static final Map<UUID, ActiveConnection> ACTIVE_CONNECTIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, MeleeDashState> ACTIVE_MELEE_DASHES = new ConcurrentHashMap<>();
    private static final Map<Integer, ImpactBulletState> ACTIVE_IMPACT_BULLETS = new ConcurrentHashMap<>();

    private static volatile boolean clientConnectionActive;
    private static volatile boolean clientMeleeReady;

    private StrandConnectionManager() {
    }

    public static boolean connect(EntityPlayer player, ItemStack bookStack,
                                  StrandNodeManager.StrandNode node,
                                  double maxRange, double spellSpeed) {
        if (player == null || player.world == null || player.world.isRemote || node == null) {
            return false;
        }
        if (!StrandNodeManager.isNodeValid(player.world, node)) {
            return false;
        }

        disconnect(player, false);
        ACTIVE_MELEE_DASHES.remove(player.getUniqueID());

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
                distance
        );
        ACTIVE_CONNECTIONS.put(player.getUniqueID(), connection);
        syncClientConnection(player, connection);

        player.fallDistance = 0.0F;
        player.velocityChanged = true;
        return true;
    }

    public static boolean disconnect(EntityPlayer player, boolean launchForward) {
        if (player == null) {
            return false;
        }

        ActiveConnection connection = ACTIVE_CONNECTIONS.remove(player.getUniqueID());
        if (connection == null) {
            return false;
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
        if (connection == null || !connection.isMeleeReady()) {
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

        clearConnection(player);

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
        clientMeleeReady = active && meleeReady;
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
            maintainMeleeDash(player);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || event.phase != TickEvent.Phase.END) {
            return;
        }

        Iterator<Map.Entry<Integer, ImpactBulletState>> iterator = ACTIVE_IMPACT_BULLETS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ImpactBulletState> entry = iterator.next();
            ImpactBulletState state = entry.getValue();
            if (state.dimension != event.world.provider.getDimension()
                    || !Battlefield.of(event.world).bullets().contains(entry.getKey())) {
                iterator.remove();
            }
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
        ACTIVE_IMPACT_BULLETS.entrySet().removeIf(entry -> entry.getValue().dimension == dimension);
    }

    private static void maintainConnection(EntityPlayer player, ActiveConnection connection) {
        World world = player.world;
        if (connection.dimension != world.provider.getDimension()
                || !StrandNodeManager.isNodeValid(world, connection.bulletId)
                || player.isDead) {
            clearConnection(player);
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
        if (distance <= EMERGENCY_RELEASE_DISTANCE) {
            disconnect(player, false);
            return;
        }

        Vec3d ropeDir = toAnchor.scale(1.0D / distance);
        Vec3d motion = getMotion(player);
        double towardSpeed = motion.dotProduct(ropeDir);
        Vec3d tangentialMotion = motion.subtract(ropeDir.scale(towardSpeed));
        double tangentialSpeed = tangentialMotion.length();

        boolean meleeReadyBefore = connection.isMeleeReady();
        double lookDot = normalizeOrZero(player.getLookVec()).dotProduct(ropeDir);
        if (lookDot < DETACH_OBTUSE_DOT) {
            connection.detachWarningTicks++;
        } else {
            connection.detachWarningTicks = 0;
        }

        if (connection.detachWarningTicks >= DETACH_WARNING_TICKS) {
            disconnect(player, false);
            return;
        }
        if (meleeReadyBefore != connection.isMeleeReady()) {
            syncClientConnection(player, connection);
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
                ? applyTraction(player, connection, ropeDir, distance, tangentialMotion)
                : applySwing(player, connection, ropeDir, distance, towardSpeed, tangentialMotion, tangentialSpeed);

        adjustedMotion = clampMagnitude(adjustedMotion, MAX_CONNECTION_SPEED);
        setMotion(player, adjustedMotion);
        player.fallDistance = 0.0F;
        player.velocityChanged = true;
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

        boolean enoughTime = connection.ticksConnected >= TRACTION_MIN_TICKS;
        boolean fastEnough = tangentialSpeed >= SWING_ENTRY_TANGENTIAL;
        boolean reachedPullEnd = connection.ticksConnected >= TRACTION_MAX_TICKS
                && distance <= connection.ropeLength + SWING_ENTRY_PULL_EPSILON;
        return enoughTime && (fastEnough || reachedPullEnd);
    }

    private static boolean shouldReenterTraction(EntityPlayer player, ActiveConnection connection,
                                                 double tangentialSpeed, double verticalGap) {
        return connection.phase == Phase.SWING
                && player.onGround
                && verticalGap > 0.35D
                && tangentialSpeed < GROUND_REENTRY_TANGENTIAL;
    }

    private static Vec3d applyTraction(EntityPlayer player, ActiveConnection connection, Vec3d ropeDir,
                                       double distance, Vec3d tangentialMotion) {
        Vec3d motion = getMotion(player);
        double rangeFactor = MathHelper.clamp(distance / Math.max(2.0D, connection.maxRange), 0.35D, 1.0D);
        double directSpeed = (TRACTION_DIRECT_SPEED + connection.speedFactor * TRACTION_SPEED_SCALE)
                * (0.85D + rangeFactor * 0.45D);

        Vec3d desired = ropeDir.scale(directSpeed).add(tangentialMotion.scale(TRACTION_TANGENT_KEEP));
        Vec3d lookTangent = projectOnPlane(normalizeOrZero(player.getLookVec()), ropeDir);
        double lookTangentLength = lookTangent.length();
        if (lookTangentLength > 1.0E-5D) {
            desired = desired.add(lookTangent.scale(TRACTION_STEER / lookTangentLength));
        }

        desired = desired.add(0.0D, TRACTION_GRAVITY_CANCEL, 0.0D);
        if (ropeDir.y > 0.0D) {
            desired = desired.add(0.0D, ropeDir.y * TRACTION_VERTICAL_BONUS + 0.03D, 0.0D);
        }

        motion = motion.scale(0.22D).add(desired.scale(0.78D));
        connection.ropeLength = Math.max(connection.minRopeLength, Math.min(connection.ropeLength, distance - 0.08D));
        return motion;
    }

    private static Vec3d applySwing(EntityPlayer player, ActiveConnection connection, Vec3d ropeDir, double distance,
                                    double towardSpeed, Vec3d tangentialMotion, double tangentialSpeed) {
        Vec3d motion = tangentialMotion;
        if (towardSpeed > 0.0D) {
            motion = motion.add(ropeDir.scale(towardSpeed));
        }

        if (distance > connection.ropeLength) {
            double stretch = distance - connection.ropeLength;
            motion = motion.add(ropeDir.scale(stretch * SWING_TENSION));
        }

        Vec3d tangentGravity = projectOnPlane(new Vec3d(0.0D, -0.08D, 0.0D), ropeDir).scale(SWING_GRAVITY_BLEND);
        motion = motion.add(tangentGravity);

        Vec3d lookTangent = projectOnPlane(normalizeOrZero(player.getLookVec()), ropeDir);
        double lookTangentLength = lookTangent.length();
        if (lookTangentLength > 1.0E-5D) {
            double viewFactor = MathHelper.clamp(lookTangentLength, 0.0D, 1.0D);
            double ropeFactor = MathHelper.clamp(connection.ropeLength / 9.0D, 0.5D, 1.45D);
            double steer = SWING_STEER * viewFactor * ropeFactor;
            motion = motion.add(lookTangent.scale(steer / lookTangentLength));
        }

        if (tangentialSpeed > 0.05D) {
            double shorten = Math.min(0.18D, SWING_SHORTEN_BASE + tangentialSpeed * SWING_SHORTEN_FACTOR);
            connection.ropeLength = Math.max(connection.minRopeLength, connection.ropeLength - shorten);
        }

        return motion;
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
                .collisionShape(new SphereShape(MELEE_IMPACT_RADIUS))
                .collisionFilter((world, bullet, entity) -> entity != null && entity != player && entity.isEntityAlive())
                .hitBehavior(StrandConnectionManager::onMeleeImpactHit)
                .shooter(player)
                .shooterHeldItem(player.getHeldItemMainhand())
                .spawnHandle();

        ACTIVE_IMPACT_BULLETS.put(handle.getId(),
                new ImpactBulletState(player.world.provider.getDimension()));
    }

    private static void onMeleeImpactHit(CollisionContext context) {
        context.canceled = true;
        if (!(context.hitEntity instanceof EntityLivingBase) || !(context.shooter instanceof EntityPlayer)) {
            return;
        }

        ImpactBulletState bulletState = ACTIVE_IMPACT_BULLETS.get(context.bullet.getId());
        if (bulletState == null || !bulletState.hitEntityIds.add(context.hitEntity.getEntityId())) {
            return;
        }

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
                DamageSource.causePlayerDamage(player)
        );
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
        clearConnection(player);
        ACTIVE_MELEE_DASHES.remove(player.getUniqueID());
    }

    private static void clearConnection(EntityPlayer player) {
        if (player == null) {
            return;
        }
        if (ACTIVE_CONNECTIONS.remove(player.getUniqueID()) != null) {
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
                new StrandConnectionSyncPacket(false, false, 0, 0.0D, 0.0D, 0.0D),
                (EntityPlayerMP) player
        );
    }

    private enum Phase {
        TRACTION,
        SWING
    }

    private static final class ActiveConnection {
        private final int dimension;
        private final int bulletId;
        private final Vec3d anchorPos;
        private final double maxRange;
        private final double speedFactor;
        private final double minRopeLength;
        private Phase phase = Phase.TRACTION;
        private int ticksConnected;
        private int detachWarningTicks;
        private double ropeLength;

        private ActiveConnection(int dimension, int bulletId, Vec3d anchorPos,
                                 double maxRange, double speedFactor, double initialDistance) {
            this.dimension = dimension;
            this.bulletId = bulletId;
            this.anchorPos = anchorPos;
            this.maxRange = maxRange;
            this.speedFactor = speedFactor;
            this.minRopeLength = Math.max(MIN_ROPE_LENGTH, initialDistance * 0.24D);
            this.ropeLength = Math.min(maxRange, initialDistance);
        }

        private boolean isMeleeReady() {
            return detachWarningTicks > 0;
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

    private static final class ImpactBulletState {
        private final int dimension;
        private final Set<Integer> hitEntityIds = ConcurrentHashMap.newKeySet();

        private ImpactBulletState(int dimension) {
            this.dimension = dimension;
        }
    }
}
