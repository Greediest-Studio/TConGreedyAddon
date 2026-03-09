package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class SpellOverlayRenderer {

    private static final int COLUMNS_PER_SIDE = 2;
    private static final int SLOT_SIZE = 20;
    private static final int X_OFFSET = 40;
    private static final int Y_OFFSET = 10;
    private static final int GROUP_GAP = SLOT_SIZE;

    private static class SpellDisplayInfo {
        final String name;
        final ResourceLocation icon;
        final NBTTagCompound pageData;
        final int internalIndex;
        final MagicPageItem page;

        SpellDisplayInfo(String name, ResourceLocation icon, NBTTagCompound pageData, int internalIndex, MagicPageItem page) {
            this.name = name;
            this.icon = icon;
            this.pageData = pageData;
            this.internalIndex = internalIndex;
            this.page = page;
        }
    }

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

        List<SpellDisplayInfo> leftSpells = buildSpellList(tag, MagicPageItem.SlotType.LEFT);
        List<SpellDisplayInfo> rightSpells = buildSpellList(tag, MagicPageItem.SlotType.RIGHT);

        int leftRows = getRows(leftSpells.size());
        int rightRows = getRows(rightSpells.size());
        int visibleRows = Math.max(leftRows, rightRows);
        if (visibleRows <= 0) return;

        int screenHeight = event.getResolution().getScaledHeight();
        int startY = screenHeight - Y_OFFSET - visibleRows * SLOT_SIZE;

        int leftCurrentIndex = tag.getInteger(MagicBook.TAG_CUR_LEFT_INDEX);
        int rightCurrentIndex = tag.getInteger(MagicBook.TAG_CUR_RIGHT_INDEX);
        long worldTime = player.world.getTotalWorldTime();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < COLUMNS_PER_SIDE; col++) {
                int y = startY + row * SLOT_SIZE;
                int localIndex = row * COLUMNS_PER_SIDE + col;

                // 左槽
                int leftX = X_OFFSET + col * SLOT_SIZE;
                if (localIndex < leftSpells.size()) {
                    SpellDisplayInfo info = leftSpells.get(localIndex);
                    boolean isCurrent = (localIndex == leftCurrentIndex);
                    drawSpellSlot(mc, leftX, y, info, isCurrent, worldTime);
                }

                // 右槽
                int rightStartX = X_OFFSET + COLUMNS_PER_SIDE * SLOT_SIZE + GROUP_GAP;
                int rightX = rightStartX + col * SLOT_SIZE;
                if (localIndex < rightSpells.size()) {
                    SpellDisplayInfo info = rightSpells.get(localIndex);
                    boolean isCurrent = (localIndex == rightCurrentIndex);
                    drawSpellSlot(mc, rightX, y, info, isCurrent, worldTime);
                }
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private List<SpellDisplayInfo> buildSpellList(NBTTagCompound toolTag, MagicPageItem.SlotType slotType) {
        List<SpellDisplayInfo> list = new ArrayList<>();
        String listKey = (slotType == MagicPageItem.SlotType.LEFT) ? MagicBook.TAG_LEFT_PAGES : MagicBook.TAG_RIGHT_PAGES;
        NBTTagList pageList = toolTag.getTagList(listKey, 10);

        for (int i = 0; i < pageList.tagCount(); i++) {
            NBTTagCompound pageData = pageList.getCompoundTagAt(i);
            if (pageData.isEmpty()) continue;

            String pageId = pageData.getString(MagicBook.TAG_PAGE_ID);
            Item item = Item.REGISTRY.getObject(new ResourceLocation(pageId));
            if (!(item instanceof UnifiedMagicPage)) continue;

            UnifiedMagicPage page = (UnifiedMagicPage) item;
            List<String> names = page.getAllSpellNames(pageData);
            List<ResourceLocation> icons = page.getSpellIcons(pageData);
            int spellCount = Math.min(names.size(), icons.size()); // 确保一致
            for (int j = 0; j < spellCount; j++) {
                list.add(new SpellDisplayInfo(names.get(j), icons.get(j), pageData, j, page));
            }
        }
        return list;
    }

    private int getRows(int spellCount) {
        return (spellCount + COLUMNS_PER_SIDE - 1) / COLUMNS_PER_SIDE;
    }

    private void drawSpellSlot(Minecraft mc, int x, int y, SpellDisplayInfo info, boolean isCurrent, long worldTime) {
        Gui.drawRect(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x80000000);

        if (info.icon != null) {
            mc.getTextureManager().bindTexture(info.icon);
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

            // 冷却遮罩
            NBTTagCompound cooldowns = info.pageData.getCompoundTag(MagicBook.TAG_COOLDOWNS);
            long lastUsed = cooldowns.getLong(String.valueOf(info.internalIndex));
            int cooldownTicks = info.page.getSpellCooldownTicks(info.internalIndex, info.page.getSlotType());
            if (cooldownTicks > 0) {
                long endTick = lastUsed + cooldownTicks;
                int remainingTicks = (int) (endTick - worldTime);
                if (remainingTicks > 0) {
                    float ratio = (float) remainingTicks / cooldownTicks;
                    int coverHeight = (int) (iconSize * ratio);
                    Gui.drawRect(iconX, iconY + iconSize - coverHeight, iconX + iconSize, iconY + iconSize, 0x80000000);
                }
            }
        }

        drawSlotBorder(x, y, SLOT_SIZE, isCurrent ? 0xFFFFAA00 : 0xFF888888);
    }

    private void drawSlotBorder(int x, int y, int size, int color) {
        Gui.drawRect(x, y, x + size, y + 1, color);
        Gui.drawRect(x, y + size - 1, x + size, y + size, color);
        Gui.drawRect(x, y, x + 1, y + size, color);
        Gui.drawRect(x + size - 1, y, x + size, y + size, color);
    }
}