package com.smd.tcongreedyaddon.client;

import com.smd.tcongreedyaddon.config.SpellOverlayConfig;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.gui.BookInventory;
import com.smd.tcongreedyaddon.tools.magicbook.page.UnifiedMagicPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.*;

@SideOnly(Side.CLIENT)
public class SpellOverlayRenderer {

    private static final int COLUMNS_PER_SIDE = 2;
    private static final int SLOT_SIZE = 20;
    private static final int GROUP_GAP = SLOT_SIZE / 2;

    private static class SpellRenderInfo {
        final String name;
        final ResourceLocation icon;
        final NBTTagCompound pageData;
        final int internalIndex;
        final UnifiedMagicPage page;
        final boolean isSelectable;
        final int cooldownTicks;

        SpellRenderInfo(String name, ResourceLocation icon, NBTTagCompound pageData,
                        int internalIndex, UnifiedMagicPage page, boolean isSelectable, int cooldownTicks) {
            this.name = name;
            this.icon = icon;
            this.pageData = pageData;
            this.internalIndex = internalIndex;
            this.page = page;
            this.isSelectable = isSelectable;
            this.cooldownTicks = cooldownTicks;
        }
    }

    private static class RenderCache {
        final ItemStack bookStack;
        final int structureHash;
        final List<SpellRenderInfo> leftSelectable;
        final List<SpellRenderInfo> leftNonSelectable;
        final List<SpellRenderInfo> rightSelectable;
        final List<SpellRenderInfo> rightNonSelectable;

        RenderCache(ItemStack bookStack, int structureHash,
                    List<SpellRenderInfo> leftSel, List<SpellRenderInfo> leftNon,
                    List<SpellRenderInfo> rightSel, List<SpellRenderInfo> rightNon) {
            this.bookStack = bookStack;
            this.structureHash = structureHash;
            this.leftSelectable = leftSel;
            this.leftNonSelectable = leftNon;
            this.rightSelectable = rightSel;
            this.rightNonSelectable = rightNon;
        }
    }

    private RenderCache cachedRender = null; // 缓存实例

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        ItemStack mainHand = player.getHeldItemMainhand();
        if (!(mainHand.getItem() instanceof MagicBook)) return;

        int currentHash = computeStructureHash(mainHand);

        if (cachedRender != null && cachedRender.bookStack == mainHand && cachedRender.structureHash == currentHash) {

            renderLists(event, cachedRender.leftSelectable, cachedRender.leftNonSelectable,
                    cachedRender.rightSelectable, cachedRender.rightNonSelectable);
        } else {
            // 缓存失效，重新构建列表
            List<SpellRenderInfo> leftSelectable = new ArrayList<>();
            List<SpellRenderInfo> leftNonSelectable = new ArrayList<>();
            List<SpellRenderInfo> rightSelectable = new ArrayList<>();
            List<SpellRenderInfo> rightNonSelectable = new ArrayList<>();

            buildSpellLists(mainHand, MagicPageItem.SlotType.LEFT, leftSelectable, leftNonSelectable);
            buildSpellLists(mainHand, MagicPageItem.SlotType.RIGHT, rightSelectable, rightNonSelectable);

            cachedRender = new RenderCache(mainHand, currentHash,
                    leftSelectable, leftNonSelectable, rightSelectable, rightNonSelectable);

            renderLists(event, leftSelectable, leftNonSelectable, rightSelectable, rightNonSelectable);
        }

        renderHoldChargeInfo(mc, event, mainHand);
    }

    private void renderHoldChargeInfo(Minecraft mc, RenderGameOverlayEvent.Post event, ItemStack bookStack) {
        EntityPlayer player = mc.player;
        if (player == null) {
            return;
        }

        MagicBook.HoldDisplayInfo holdInfo = MagicBook.getSelectedMainHandHoldDisplayInfo(player);
        if (holdInfo == null) {
            return;
        }

        boolean locallyHoldingBook = player.isHandActive()
                && player.getActiveHand() == net.minecraft.util.EnumHand.MAIN_HAND
                && !player.getActiveItemStack().isEmpty()
                && player.getActiveItemStack().getItem() == bookStack.getItem()
                && mc.gameSettings.keyBindUseItem.isKeyDown();
        boolean holdActive = MagicBook.isClientHoldActive(player);

        if (!holdActive && !locallyHoldingBook) {
            return;
        }

        int heldTicks = locallyHoldingBook
                ? Math.max(0, bookStack.getMaxItemUseDuration() - player.getItemInUseCount())
                : MagicBook.getClientHoldTicks(player);

        int targetTicks = holdInfo.chargeTicks;
        float heldSeconds = heldTicks / 20.0f;
        float targetSeconds = targetTicks / 20.0f;

        String text;
        int color;
        if (targetTicks <= 0 || heldTicks >= targetTicks) {
            text = I18n.format("overlay.hold_charge_ready");
            color = 0xFF55FF55;
        } else {
            text = I18n.format("overlay.hold_charge", String.format("%.1f", heldSeconds), String.format("%.1f", targetSeconds));
            color = 0xFFFFFF55;
        }

        int screenWidth = event.getResolution().getScaledWidth();
        int screenHeight = event.getResolution().getScaledHeight();
        int x = (screenWidth - mc.fontRenderer.getStringWidth(text)) / 2;
        int y = screenHeight - 38;
        mc.fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    private int computeStructureHash(ItemStack bookStack) {
        if (!(bookStack.getItem() instanceof MagicBook)) return 0;
        MagicBook book = (MagicBook) bookStack.getItem();
        BookInventory inv = book.getInventory(bookStack);
        int hash = 1;
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack pageStack = inv.getStackInSlot(i);
            if (!pageStack.isEmpty() && pageStack.getItem() instanceof UnifiedMagicPage) {

                hash = 31 * hash + pageStack.getItem().getRegistryName().hashCode();
                hash = 31 * hash + i;
            }
        }
        return hash;
    }

    /**
     * 渲染所有列表
     */
    private void renderLists(RenderGameOverlayEvent.Post event,
                             List<SpellRenderInfo> leftSelectable,
                             List<SpellRenderInfo> leftNonSelectable,
                             List<SpellRenderInfo> rightSelectable,
                             List<SpellRenderInfo> rightNonSelectable) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        NBTTagCompound bookTag = player.getHeldItemMainhand().getTagCompound();
        if (bookTag == null) bookTag = new NBTTagCompound();
        int leftCurrentIndex = bookTag.getInteger(MagicBook.TAG_CUR_LEFT_INDEX);
        int rightCurrentIndex = bookTag.getInteger(MagicBook.TAG_CUR_RIGHT_INDEX);
        long worldTime = player.world.getTotalWorldTime();

        int leftSelectableRows = getRows(leftSelectable.size());
        int rightSelectableRows = getRows(rightSelectable.size());
        int maxSelectableRows = Math.max(leftSelectableRows, rightSelectableRows);

        int leftNonSelectableRows = getRows(leftNonSelectable.size());
        int rightNonSelectableRows = getRows(rightNonSelectable.size());
        int maxNonSelectableRows = Math.max(leftNonSelectableRows, rightNonSelectableRows);

        if (maxSelectableRows == 0 && maxNonSelectableRows == 0) return;

        int screenHeight = event.getResolution().getScaledHeight();
        int screenWidth = event.getResolution().getScaledWidth();
        int startY = screenHeight - SpellOverlayConfig.spellsYOffsetFromBottom - (maxSelectableRows) * SLOT_SIZE;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        // 快捷栏宽 182px，以屏幕水平中心对称排列，半宽 = 91
        final int HOTBAR_HALF_WIDTH = 91;
        int activeGroupWidth = COLUMNS_PER_SIDE * SLOT_SIZE * 2 + GROUP_GAP;
        // 主动法术槽：锚定到快捷栏右边缘，向右偏移 activeSpellsXOffsetFromRight
        int activeStartX = screenWidth / 2 + HOTBAR_HALF_WIDTH + SpellOverlayConfig.activeSpellsXOffsetFromRight;

        for (int row = 0; row < maxSelectableRows; row++) {
            for (int col = 0; col < COLUMNS_PER_SIDE; col++) {
                int y = startY + row * SLOT_SIZE;
                int localIndex = row * COLUMNS_PER_SIDE + col;

                int leftX = activeStartX + col * SLOT_SIZE;
                if (localIndex < leftSelectable.size()) {
                    SpellRenderInfo info = leftSelectable.get(localIndex);
                    boolean isCurrent = (localIndex == leftCurrentIndex);
                    drawSpellSlot(mc, leftX, y, info, isCurrent, worldTime);
                }

                int rightStartX = activeStartX + COLUMNS_PER_SIDE * SLOT_SIZE + GROUP_GAP;
                int rightX = rightStartX + col * SLOT_SIZE;
                if (localIndex < rightSelectable.size()) {
                    SpellRenderInfo info = rightSelectable.get(localIndex);
                    boolean isCurrent = (localIndex == rightCurrentIndex);
                    drawSpellSlot(mc, rightX, y, info, isCurrent, worldTime);
                }
            }
        }

        // 绘制不可切换法术（被动）
        int nonSelectableStartY = screenHeight - SpellOverlayConfig.spellsYOffsetFromBottom - maxNonSelectableRows * SLOT_SIZE;
        // 被动法术槽：锚定到快捷栏左边缘，向左偏移 passiveGroupWidth + passiveSpellsXOffsetFromLeft
        int passiveGroupWidth = COLUMNS_PER_SIDE * SLOT_SIZE * 2 + GROUP_GAP;
        int passiveStartX = screenWidth / 2 - HOTBAR_HALF_WIDTH - passiveGroupWidth - SpellOverlayConfig.passiveSpellsXOffsetFromLeft;
        for (int row = 0; row < maxNonSelectableRows; row++) {
            for (int col = 0; col < COLUMNS_PER_SIDE; col++) {
                int y = nonSelectableStartY + row * SLOT_SIZE;
                int localIndex = row * COLUMNS_PER_SIDE + col;

                int leftX = passiveStartX + col * SLOT_SIZE;
                if (localIndex < leftNonSelectable.size()) {
                    SpellRenderInfo info = leftNonSelectable.get(localIndex);
                    drawSpellSlot(mc, leftX, y, info, false, worldTime);
                }

                int rightStartX = passiveStartX + COLUMNS_PER_SIDE * SLOT_SIZE + GROUP_GAP;
                int rightX = rightStartX + col * SLOT_SIZE;
                if (localIndex < rightNonSelectable.size()) {
                    SpellRenderInfo info = rightNonSelectable.get(localIndex);
                    drawSpellSlot(mc, rightX, y, info, false, worldTime);
                }
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * 从魔法书物品中构建指定槽位的法术渲染信息，并分类到 selectable 和 nonSelectable 列表。
     */
    private void buildSpellLists(ItemStack bookStack, MagicPageItem.SlotType slotType,
                                 List<SpellRenderInfo> selectableOut,
                                 List<SpellRenderInfo> nonSelectableOut) {
        if (!(bookStack.getItem() instanceof MagicBook)) return;

        MagicBook book = (MagicBook) bookStack.getItem();
        BookInventory inv = book.getInventory(bookStack);

        int start = (slotType == MagicPageItem.SlotType.LEFT) ? 0 : inv.getLeftSlots();
        int end = (slotType == MagicPageItem.SlotType.LEFT) ? inv.getLeftSlots() : inv.getSlots();

        for (int slot = start; slot < end; slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof UnifiedMagicPage)) continue;

            UnifiedMagicPage page = (UnifiedMagicPage) pageStack.getItem();
            List<UnifiedMagicPage.SpellDisplayData> allData = page.getAllSpellDisplayData(pageStack);
            for (UnifiedMagicPage.SpellDisplayData data : allData) {
                if (!data.renderInOverlay) continue;
                SpellRenderInfo info = new SpellRenderInfo(
                        data.name, data.icon, data.pageData, data.internalIndex,
                        page, data.selectable, data.cooldownTicks
                );
                if (data.selectable) {
                    selectableOut.add(info);
                } else {
                    nonSelectableOut.add(info);
                }
            }
        }
    }

    private int getRows(int spellCount) {
        return (spellCount + COLUMNS_PER_SIDE - 1) / COLUMNS_PER_SIDE;
    }

    private void drawSpellSlot(Minecraft mc, int x, int y, SpellRenderInfo info,
                               boolean isCurrent, long worldTime) {
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

            if (info.cooldownTicks > 0) {
                NBTTagCompound cooldowns = info.pageData.getCompoundTag(MagicBook.TAG_COOLDOWNS);
                long lastUsed = cooldowns.getLong(String.valueOf(info.internalIndex));
                long endTick = lastUsed + info.cooldownTicks;
                int remainingTicks = (int) (endTick - worldTime);
                if (remainingTicks > 0) {
                    float ratio = (float) remainingTicks / info.cooldownTicks;
                    int coverHeight = (int) (iconSize * ratio);
                    Gui.drawRect(iconX, iconY + iconSize - coverHeight, iconX + iconSize, iconY + iconSize, 0x80000000);
                }
            }
        }

        int borderColor;
        if (isCurrent) {
            borderColor = 0xFFFFAA00;
        } else if (info.isSelectable) {
            borderColor = 0xFF888888;
        } else {
            borderColor = 0xFF444444;
        }
        drawSlotBorder(x, y, SLOT_SIZE, borderColor);
    }

    private void drawSlotBorder(int x, int y, int size, int color) {
        Gui.drawRect(x, y, x + size, y + 1, color);
        Gui.drawRect(x, y + size - 1, x + size, y + size, color);
        Gui.drawRect(x, y, x + 1, y + size, color);
        Gui.drawRect(x + size - 1, y, x + size, y + size, color);
    }
}

