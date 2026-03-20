package com.smd.tcongreedyaddon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class StrandConnectionRenderer {
    private static ClientConnection clientConnection;

    public static void setClientConnection(int dimension, Vec3d anchorPos) {
        if (anchorPos == null) {
            clientConnection = null;
            return;
        }
        clientConnection = new ClientConnection(dimension, anchorPos);
    }

    public static void clearClientConnection() {
        clientConnection = null;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null || mc.world == null || clientConnection == null) {
            return;
        }
        if (clientConnection.dimension != mc.world.provider.getDimension()) {
            return;
        }

        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        Vec3d start = getRopeStart(player, event.getPartialTicks());
        Vec3d end = clientConnection.anchorPos;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-camX, -camY, -camZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        float pulse = 0.75F + 0.25F * (float) Math.sin((player.ticksExisted + event.getPartialTicks()) * 0.45F);
        drawGlowLine(start, end, 7.5F, 0.16F * pulse, 0.25F, 1.0F, 0.22F);
        drawGlowLine(start, end, 4.0F, 0.26F * pulse, 0.45F, 1.0F, 0.42F);
        drawGlowLine(start, end, 1.8F, 0.55F * pulse, 0.95F, 0.75F, 0.95F);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null && event.getWorld() == mc.world) {
            clientConnection = null;
        }
    }

    private static void drawGlowLine(Vec3d start, Vec3d end, float width, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GL11.glLineWidth(width);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(start.x, start.y, start.z).color(red, green, blue, alpha).endVertex();
        buffer.pos(end.x, end.y, end.z).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }

    private static Vec3d getRopeStart(EntityPlayerSP player, float partialTicks) {
        double px = interpolate(player.prevPosX, player.posX, partialTicks);
        double py = interpolate(player.prevPosY, player.posY, partialTicks);
        double pz = interpolate(player.prevPosZ, player.posZ, partialTicks);
        float eyeHeight = player.getEyeHeight();
        Vec3d look = player.getLook(partialTicks).normalize();
        Vec3d side = look.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
        if (side.lengthSquared() > 1.0E-6D) {
            side = side.normalize();
        } else {
            side = new Vec3d(1.0D, 0.0D, 0.0D);
        }

        return new Vec3d(px, py + eyeHeight - 0.18D, pz)
                .add(side.scale(0.18D))
                .add(look.scale(0.28D));
    }

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }

    private static final class ClientConnection {
        private final int dimension;
        private final Vec3d anchorPos;

        private ClientConnection(int dimension, Vec3d anchorPos) {
            this.dimension = dimension;
            this.anchorPos = anchorPos;
        }
    }
}
