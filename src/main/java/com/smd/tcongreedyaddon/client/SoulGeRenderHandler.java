package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.tools.solidarytinker.SoulGe;
import com.smd.tcongreedyaddon.tools.solidarytinker.SoulGeClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class SoulGeRenderHandler {

    public static final SoulGeRenderHandler INSTANCE = new SoulGeRenderHandler();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null || mc.world == null) {
            return;
        }

        EnumHand hand = resolveActiveSoulGeHand(player);
        if (hand == null) {
            return;
        }

        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof SoulGe) || !player.isHandActive()) {
            return;
        }

        List<EntityLivingBase> targets = SoulGeClientState.getRenderTargets(player, hand);
        if (targets.isEmpty()) {
            return;
        }

        boolean singleMode = SoulGeClientState.isSingleMode(player, hand);
        int temperature = SoulGeClientState.getTemperatureRiseTick(player, hand);

        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        for (EntityLivingBase target : targets) {
            if (target == null || !target.isEntityAlive() || player.getDistance(target) > 80.0f) {
                continue;
            }
            boolean executing = SoulGeClientState.isExecuting(player, hand, target);
            float lineWidth = singleMode
                    ? 5.5f + Math.min(6.0f, temperature * 0.14f)
                    : (executing ? 9.0f : 6.0f);
            GL11.glLineWidth(lineWidth);
            drawBeam(player, target, event.getPartialTicks(), camX, camY, camZ, singleMode, temperature, executing);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawBeam(EntityPlayer player, EntityLivingBase target, float partialTicks,
                          double camX, double camY, double camZ, boolean singleMode, int temperature, boolean executing) {
        Vec3d playerEyes = player.getPositionEyes(partialTicks);
        Vec3d targetEyes = target.getPositionEyes(partialTicks);

        Vec3d playerCenter = playerEyes.subtract(0, player.getEyeHeight() - player.height / 2.0, 0);
        Vec3d targetCenter = targetEyes.subtract(0, target.getEyeHeight() - target.height / 2.0, 0);

        Vec3d p0 = new Vec3d(playerCenter.x - camX, playerCenter.y - camY, playerCenter.z - camZ);
        Vec3d p2 = new Vec3d(targetCenter.x - camX, targetCenter.y - camY, targetCenter.z - camZ);

        float time = player.ticksExisted + partialTicks;
        double offsetY = executing ? 0.65 * Math.sin(time * 0.16) : (singleMode ? 0.4 * Math.sin(time * 0.12) : 0.2 * Math.sin(time * 0.08));
        Vec3d midPoint = playerCenter.add(targetCenter).scale(0.5);
        Vec3d controlPoint = new Vec3d(midPoint.x - camX, midPoint.y - camY + offsetY, midPoint.z - camZ);

        float[] colorStart = getStartColor(temperature, executing);
        float[] colorEnd = getEndColor(temperature, executing);
        float alpha = executing ? 0.85f : (singleMode ? 0.65f : 0.55f);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        int segments = executing ? 52 : (singleMode ? 42 : 30);
        for (int i = 0; i <= segments; i++) {
            float t = i / (float) segments;
            double x = (1 - t) * (1 - t) * p0.x + 2 * (1 - t) * t * controlPoint.x + t * t * p2.x;
            double y = (1 - t) * (1 - t) * p0.y + 2 * (1 - t) * t * controlPoint.y + t * t * p2.y;
            double z = (1 - t) * (1 - t) * p0.z + 2 * (1 - t) * t * controlPoint.z + t * t * p2.z;

            float r = colorStart[0] + (colorEnd[0] - colorStart[0]) * t;
            float g = colorStart[1] + (colorEnd[1] - colorStart[1]) * t;
            float b = colorStart[2] + (colorEnd[2] - colorStart[2]) * t;
            GlStateManager.color(r, g, b, alpha);
            GL11.glVertex3d(x, y, z);
        }
        GL11.glEnd();
    }

    private float[] getStartColor(int temperature, boolean executing) {
        if (executing) {
            return new float[]{1.0f, 0.08f, 0.02f};
        }
        return heatColor(temperature, 0.0f);
    }

    private float[] getEndColor(int temperature, boolean executing) {
        if (executing) {
            return new float[]{1.0f, 0.85f, 0.12f};
        }
        return heatColor(temperature, 0.18f);
    }

    private float[] heatColor(int temperature, float brighten) {
        float heat = Math.max(0.0f, Math.min(1.0f, temperature / 24.0f));
        float[] blue = new float[]{0.18f, 0.62f, 1.0f};
        float[] yellow = new float[]{1.0f, 0.92f, 0.18f};
        float[] red = new float[]{1.0f, 0.12f, 0.02f};
        float[] from;
        float[] to;
        float localT;
        if (heat < 0.5f) {
            from = blue;
            to = yellow;
            localT = heat / 0.5f;
        } else {
            from = yellow;
            to = red;
            localT = (heat - 0.5f) / 0.5f;
        }

        return new float[]{
                clampColor(from[0] + (to[0] - from[0]) * localT + brighten),
                clampColor(from[1] + (to[1] - from[1]) * localT + brighten),
                clampColor(from[2] + (to[2] - from[2]) * localT + brighten)
        };
    }

    private float clampColor(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private EnumHand resolveActiveSoulGeHand(EntityPlayer player) {
        if (!player.isHandActive()) {
            return null;
        }
        EnumHand hand = player.getActiveHand();
        if (hand == null) {
            return null;
        }
        ItemStack stack = player.getHeldItem(hand);
        return stack.getItem() instanceof SoulGe ? hand : null;
    }
}
