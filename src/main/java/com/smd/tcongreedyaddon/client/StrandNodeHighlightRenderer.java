package com.smd.tcongreedyaddon.client;

import com.smd.bulletapi.client.ClientBullet;
import com.smd.bulletapi.client.ClientDanmakuCache;
import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.event.StrandNodeManager;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.util.MagicBookHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class StrandNodeHighlightRenderer {
    private static final double REUSE_RADIUS = 1.2D;
    private static final float ANIMATION_LERP_FACTOR = 0.35F;
    private static final float RENDER_EPSILON = 0.01F;

    private static final Map<Integer, Map<Integer, Float>> CLIENT_WORLD_PROGRESS = new ConcurrentHashMap<>();

    private StrandNodeHighlightRenderer() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.world == null || mc.player == null) {
            return;
        }

        World world = mc.world;
        int dimension = world.provider.getDimension();
        Map<Integer, ClientBullet> bullets = ClientDanmakuCache.INSTANCE.getBullets();
        if (bullets == null || bullets.isEmpty()) {
            CLIENT_WORLD_PROGRESS.remove(dimension);
            return;
        }

        double maxRange = getClientReuseRange(mc.player);
        int aimedBulletId = findAimedNodeBulletId(mc.player, bullets, maxRange);

        Map<Integer, Float> progress = CLIENT_WORLD_PROGRESS.computeIfAbsent(dimension, ignored -> new ConcurrentHashMap<>());
        progress.keySet().removeIf(bulletId -> !isStrandNodeBullet(bullets.get(bulletId)));

        for (Map.Entry<Integer, ClientBullet> entry : bullets.entrySet()) {
            int bulletId = entry.getKey();
            if (!isStrandNodeBullet(entry.getValue())) {
                progress.remove(bulletId);
                continue;
            }

            float current = progress.getOrDefault(bulletId, 0.0F);
            float target = bulletId == aimedBulletId ? 1.0F : 0.0F;
            float next = lerp(current, target, ANIMATION_LERP_FACTOR);
            if (Math.abs(next - target) <= RENDER_EPSILON) {
                next = target;
            }
            progress.put(bulletId, next);
            applyBulletScale(entry.getValue(), next);
        }
    }

    private static float lerp(float current, float target, float factor) {
        return current + (target - current) * factor;
    }

    private static int findAimedNodeBulletId(EntityPlayer player, Map<Integer, ClientBullet> bullets, double maxDistance) {
        Vec3d eyePos = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();

        int bestBulletId = -1;
        double bestPerpendicular = Double.MAX_VALUE;
        double bestDistanceSq = Double.MAX_VALUE;

        for (Map.Entry<Integer, ClientBullet> entry : bullets.entrySet()) {
            ClientBullet bullet = entry.getValue();
            if (!isStrandNodeBullet(bullet)) {
                continue;
            }

            Vec3d bulletPos = bullet.getRenderPosition(1.0F);
            Vec3d offset = bulletPos.subtract(eyePos);
            double forward = offset.dotProduct(look);
            if (forward < 0.0D || forward > maxDistance) {
                continue;
            }

            double perpendicular = perpendicularDistance(eyePos, look, bulletPos);
            if (perpendicular > REUSE_RADIUS) {
                continue;
            }

            double distanceSq = bulletPos.squareDistanceTo(eyePos);
            if (perpendicular < bestPerpendicular
                    || (Math.abs(perpendicular - bestPerpendicular) <= 1.0E-6D && distanceSq < bestDistanceSq)) {
                bestPerpendicular = perpendicular;
                bestDistanceSq = distanceSq;
                bestBulletId = entry.getKey();
            }
        }

        return bestBulletId;
    }

    private static boolean isStrandNodeBullet(@Nullable ClientBullet bullet) {
        if (bullet == null || bullet.isDead()) {
            return false;
        }
        NBTTagCompound data = bullet.getCustomData();
        return data != null && data.getBoolean("strand_node");
    }

    private static double perpendicularDistance(Vec3d lineStart, Vec3d lineDir, Vec3d point) {
        Vec3d offset = point.subtract(lineStart);
        double projection = offset.dotProduct(lineDir);
        Vec3d nearest = lineStart.add(lineDir.scale(projection));
        return nearest.distanceTo(point);
    }

    private static void applyBulletScale(ClientBullet bullet, float highlightProgress) {
        NBTTagCompound data = bullet.getCustomData();
        if (data == null) {
            data = new NBTTagCompound();
        } else {
            data = data.copy();
        }

        float scale = StrandNodeManager.NODE_BASE_SCALE
                + (StrandNodeManager.NODE_HIGHLIGHT_SCALE - StrandNodeManager.NODE_BASE_SCALE) * highlightProgress;
        data.setFloat("scale", scale);
        bullet.setCustomData(data);
    }

    private static double getClientReuseRange(@Nullable EntityPlayer player) {
        if (player == null) {
            return MagicBook.BEAM_RANGE;
        }
        Float range = MagicBookHelper.getRange(player.getHeldItemMainhand());
        if (range == null || range <= 0.0F) {
            return MagicBook.BEAM_RANGE;
        }
        return range;
    }
}
