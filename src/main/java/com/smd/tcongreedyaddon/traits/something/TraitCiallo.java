package com.smd.tcongreedyaddon.traits.something;

import com.smd.tcongreedyaddon.init.SoundsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import slimeknights.tconstruct.library.traits.AbstractTrait;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TraitCiallo extends AbstractTrait {

    private static final Random RANDOM = new Random();
    private static final String TEXT = "Ciallo～(∠・ω< )⌒☆";
    private static final float TEXT_SCALE = 1.5F;
    private final List<RenderInstance> renderInstances = new ArrayList<>();

    public TraitCiallo() {
        super("ciallo", 0xffffff);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, boolean isCritical) {
        target.world.playSound(
                null,
                target.posX, target.posY, target.posZ,
                SoundsHandler.CIALLO,
                SoundCategory.PLAYERS,
                2.0F, 1.0F
        );
        
        triggerRender();
    }

    @Override
    public void onPlayerHurt(ItemStack tool, EntityPlayer player, EntityLivingBase attacker, LivingHurtEvent event) {
        attacker.world.playSound(
                null,
                attacker.posX, attacker.posY, attacker.posZ,
                SoundsHandler.CIALLO,
                SoundCategory.PLAYERS,
                2.0F, 1.0F
        );
        
        triggerRender();
    }
    
    private void triggerRender() {
        if (!Minecraft.getMinecraft().world.isRemote) {
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        long endTime = System.currentTimeMillis() + 2000;
        
        int screenWidth = mc.displayWidth / 2;
        int screenHeight = mc.displayHeight / 2;
        int textWidth = mc.fontRenderer.getStringWidth(TEXT);
        int textHeight = mc.fontRenderer.FONT_HEIGHT;
        
        int x = RANDOM.nextInt(Math.max(1, screenWidth - textWidth));
        int y = RANDOM.nextInt(Math.max(1, screenHeight - textHeight));
        
        int color = RANDOM.nextInt(0x1000000);
        
        synchronized (renderInstances) {
            renderInstances.add(new RenderInstance(endTime, x, y, color));
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.fontRenderer == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        
        synchronized (renderInstances) {
            Iterator<RenderInstance> iterator = renderInstances.iterator();
            while (iterator.hasNext()) {
                RenderInstance instance = iterator.next();
                
                if (currentTime > instance.endTime) {
                    iterator.remove();
                    continue;
                }
                
                float alpha = 1.0F;
                long remainingTime = instance.endTime - currentTime;
                if (remainingTime < 500) {
                    alpha = remainingTime / 500.0F;
                }
                
                int baseColor = instance.color & 0xFFFFFF;
                int alphaInt = (int)(alpha * 255) & 0xFF;
                int color = (alphaInt << 24) | baseColor;
                
                GlStateManager.pushMatrix();
                GlStateManager.scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
                mc.fontRenderer.drawStringWithShadow(
                        TEXT,
                        instance.x / TEXT_SCALE,
                        instance.y / TEXT_SCALE,
                        color
                );
                GlStateManager.popMatrix();
            }
        }
        
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static class RenderInstance {
        long endTime;
        int x;
        int y;
        int color;

        RenderInstance(long endTime, int x, int y, int color) {
            this.endTime = endTime;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}
