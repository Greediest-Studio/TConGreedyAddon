package com.smd.tcongreedyaddon.event;

import com.smd.bulletapi.api.Battlefield;
import com.smd.bulletapi.api.BulletApi;
import com.smd.bulletapi.api.handle.BulletHandle;
import com.smd.tcongreedyaddon.Tags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class StrandNodeManager {
    public static final int NODE_LIFE_TICKS = 20 * 60;
    private static final double REUSE_RADIUS = 1.6D;

    private static final Map<Integer, Map<Integer, StrandNode>> WORLD_NODES = new ConcurrentHashMap<>();

    private StrandNodeManager() {
    }

    @Nullable
    public static StrandNode findReusableNode(World world, EntityPlayer player, double range) {
        if (world == null || player == null) {
            return null;
        }

        Map<Integer, StrandNode> nodes = WORLD_NODES.get(world.provider.getDimension());
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        Vec3d eyePos = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        return nodes.values().stream()
                .filter(node -> isNodeValid(world, node))
                .filter(node -> isNodeNearSightLine(eyePos, look, node.anchorPos, range))
                .min(Comparator
                        .comparingDouble((StrandNode node) -> perpendicularDistance(eyePos, look, node.anchorPos))
                        .thenComparingDouble(node -> node.anchorPos.squareDistanceTo(eyePos)))
                .orElse(null);
    }

    @Nullable
    public static StrandNode createNode(World world, EntityPlayer player, Vec3d anchorPos) {
        if (world == null || world.isRemote || player == null || anchorPos == null) {
            return null;
        }

        BulletHandle handle = BulletApi.builder(world)
                .position(anchorPos)
                .velocity(new Vec3d(0.0D, 0.0D, 0.0D))
                .life(NODE_LIFE_TICKS)
                .damage(0.0F)
                .rendererType("point")
                .color(0x45FF66)
                .size(1.15F)
                .shooter(player)
                .shooterHeldItem(player.getHeldItemMainhand())
                .set("strand_node", true)
                .spawnHandle();

        StrandNode node = new StrandNode(world.provider.getDimension(), handle.getId(), anchorPos);
        WORLD_NODES.computeIfAbsent(node.dimension, ignored -> new ConcurrentHashMap<>()).put(node.bulletId, node);
        return node;
    }

    public static void refreshNode(World world, StrandNode node) {
        if (world == null || world.isRemote || node == null) {
            return;
        }

        BulletHandle handle = BulletApi.handle(world, node.bulletId);
        if (handle.exists()) {
            handle.setLife(NODE_LIFE_TICKS);
        } else {
            removeNode(world.provider.getDimension(), node.bulletId);
        }
    }

    public static boolean isNodeValid(World world, StrandNode node) {
        return node != null && isNodeValid(world, node.bulletId);
    }

    public static boolean isNodeValid(World world, int bulletId) {
        return world != null && bulletId >= 0 && BulletApi.handle(world, bulletId).exists();
    }

    private static boolean isNodeNearSightLine(Vec3d eyePos, Vec3d look, Vec3d nodePos, double maxDistance) {
        Vec3d offset = nodePos.subtract(eyePos);
        double forward = offset.dotProduct(look);
        if (forward < 0.0D || forward > maxDistance) {
            return false;
        }
        return perpendicularDistance(eyePos, look, nodePos) <= REUSE_RADIUS;
    }

    private static double perpendicularDistance(Vec3d lineStart, Vec3d lineDir, Vec3d point) {
        Vec3d offset = point.subtract(lineStart);
        double projection = offset.dotProduct(lineDir);
        Vec3d nearest = lineStart.add(lineDir.scale(projection));
        return nearest.distanceTo(point);
    }

    private static void removeNode(int dimension, int bulletId) {
        Map<Integer, StrandNode> nodes = WORLD_NODES.get(dimension);
        if (nodes == null) {
            return;
        }
        nodes.remove(bulletId);
        if (nodes.isEmpty()) {
            WORLD_NODES.remove(dimension);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || event.phase != TickEvent.Phase.END) {
            return;
        }

        Map<Integer, StrandNode> nodes = WORLD_NODES.get(event.world.provider.getDimension());
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        nodes.entrySet().removeIf(entry -> !Battlefield.of(event.world).bullets().contains(entry.getKey()));
        if (nodes.isEmpty()) {
            WORLD_NODES.remove(event.world.provider.getDimension());
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof World) {
            WORLD_NODES.remove(((World) event.getWorld()).provider.getDimension());
        }
    }

    public static final class StrandNode {
        public final int dimension;
        public final int bulletId;
        public final Vec3d anchorPos;

        private StrandNode(int dimension, int bulletId, Vec3d anchorPos) {
            this.dimension = dimension;
            this.bulletId = bulletId;
            this.anchorPos = anchorPos;
        }
    }
}
