package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.page.UnifiedMagicPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class SpellOverlayRenderer {

    private static final int COLUMNS_PER_SIDE = 2;
    private static final int SLOT_SIZE = 20;
    private static final int X_OFFSET = 40;
    private static final int Y_OFFSET = 10;
    private static final int GROUP_GAP = SLOT_SIZE;

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        ItemStack mainHand = player.getHeldItemMainhand();
        if (!(mainHand.getItem() instanceof MagicBook)) return;

        NBTTagCompound tag = mainHand.getTagCompound();
        if (tag == null) return;

        NBTTagCompound leftPageData = tag.getCompoundTag(MagicBook.TAG_LEFT_PAGE);
        NBTTagCompound rightPageData = tag.getCompoundTag(MagicBook.TAG_RIGHT_PAGE);

        UnifiedMagicPage leftPage = null;
        UnifiedMagicPage rightPage = null;

        if (!leftPageData.isEmpty()) {
            String leftPageId = leftPageData.getString(MagicBook.TAG_PAGE_ID);
            Item leftItem = Item.REGISTRY.getObject(new ResourceLocation(leftPageId));
            if (leftItem instanceof UnifiedMagicPage) {
                leftPage = (UnifiedMagicPage) leftItem;
            }
        }

        if (!rightPageData.isEmpty()) {
            String rightPageId = rightPageData.getString(MagicBook.TAG_PAGE_ID);
            Item rightItem = Item.REGISTRY.getObject(new ResourceLocation(rightPageId));
            if (rightItem instanceof UnifiedMagicPage) {
                rightPage = (UnifiedMagicPage) rightItem;
            }
        }

        List<String> leftSpellNames = leftPage != null ? leftPage.getAllSpellNames(leftPageData) : null;
        List<ResourceLocation> leftSpellIcons = leftPage != null ? leftPage.getSpellIcons(leftPageData) : null;
        int leftSpellCount = leftSpellNames != null ? leftSpellNames.size() : 0;
        int leftRows = getRows(leftSpellCount);

        List<String> rightSpellNames = rightPage != null ? rightPage.getAllSpellNames(rightPageData) : null;
        List<ResourceLocation> rightSpellIcons = rightPage != null ? rightPage.getSpellIcons(rightPageData) : null;
        int rightSpellCount = rightSpellNames != null ? rightSpellNames.size() : 0;
        int rightRows = getRows(rightSpellCount);

        int visibleRows = Math.max(leftRows, rightRows);
        if (visibleRows <= 0) {
            return;
        }

        int screenHeight = event.getResolution().getScaledHeight();
        int startY = screenHeight - Y_OFFSET - visibleRows * SLOT_SIZE;

        int leftCurrentIndex = leftPageData.getInteger(MagicBook.TAG_SPELL_INDEX);
        int rightCurrentIndex = rightPageData.getInteger(MagicBook.TAG_SPELL_INDEX);
        NBTTagCompound leftCooldowns = leftPageData.getCompoundTag("cooldowns");
        NBTTagCompound rightCooldowns = rightPageData.getCompoundTag("cooldowns");
        long worldTime = player.world.getTotalWorldTime();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < COLUMNS_PER_SIDE; col++) {
                int y = startY + row * SLOT_SIZE;
                int localIndex = row * COLUMNS_PER_SIDE + col;

                int leftX = X_OFFSET + col * SLOT_SIZE;
                if (localIndex < leftSpellCount) {
                    drawSpellSlot(mc, leftX, y, localIndex, leftSpellCount, leftCurrentIndex, leftPage, leftSpellIcons, leftCooldowns, worldTime);
                }

                int rightStartX = X_OFFSET + COLUMNS_PER_SIDE * SLOT_SIZE + GROUP_GAP;
                int rightX = rightStartX + col * SLOT_SIZE;
                if (localIndex < rightSpellCount) {
                    drawSpellSlot(mc, rightX, y, localIndex, rightSpellCount, rightCurrentIndex, rightPage, rightSpellIcons, rightCooldowns, worldTime);
                }
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private int getRows(int spellCount) {
        return spellCount <= 0 ? 0 : (spellCount + COLUMNS_PER_SIDE - 1) / COLUMNS_PER_SIDE;
    }

    private void drawSpellSlot(Minecraft mc, int x, int y, int localIndex, int spellCount, int currentIndex,
                               UnifiedMagicPage page, List<ResourceLocation> spellIcons,
                               NBTTagCompound cooldowns, long worldTime) {
        Gui.drawRect(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x80000000);

        boolean isCurrentSpell = false;
        if (localIndex < spellCount && page != null && spellIcons != null && localIndex < spellIcons.size()) {
            ResourceLocation iconTex = spellIcons.get(localIndex);
            if (iconTex != null) {
                mc.getTextureManager().bindTexture(iconTex);
                GlStateManager.enableTexture2D();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                int iconX = x + 1;
                int iconY = y + 1;
                int iconSize = SLOT_SIZE - 2;

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(iconX, iconY + iconSize, 0).tex(0, 1).endVertex();
                buffer.pos(iconX + iconSize, iconY + iconSize, 0).tex(1, 1).endVertex();
                buffer.pos(iconX + iconSize, iconY, 0).tex(1, 0).endVertex();
                buffer.pos(iconX, iconY, 0).tex(0, 0).endVertex();
                tessellator.draw();

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

                long lastUsed = cooldowns.getLong(String.valueOf(localIndex));
                int cooldownTicks = page.getSpellCooldownTicks(localIndex);
                if (cooldownTicks > 0) {
                    long endTick = lastUsed + cooldownTicks;
                    int remainingTicks = (int)(endTick - worldTime);
                    if (remainingTicks > 0) {
                        float ratio = (float) remainingTicks / cooldownTicks;
                        int coverHeight = (int)(iconSize * ratio);
                        Gui.drawRect(iconX, iconY + iconSize - coverHeight, iconX + iconSize, iconY + iconSize, 0x80000000);
                    }
                }
            }

            if (localIndex == currentIndex) {
                isCurrentSpell = true;
            }
        }

        drawSlotBorder(x, y, SLOT_SIZE, 0xFF888888);
        if (isCurrentSpell) {
            drawSlotBorder(x, y, SLOT_SIZE, 0xFFFFAA00);
        }
    }

    private void drawSlotBorder(int x, int y, int size, int color) {
        Gui.drawRect(x, y, x + size, y + 1, color); // 上
        Gui.drawRect(x, y + size - 1, x + size, y + size, color); // 下
        Gui.drawRect(x, y, x + 1, y + size, color); // 左
        Gui.drawRect(x + size - 1, y, x + size, y + size, color); // 右
    }
}