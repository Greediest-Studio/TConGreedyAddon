package com.smd.tcongreedyaddon.tools.magicbook.gui;

import com.smd.tcongreedyaddon.Tags;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiMagicBook extends GuiContainer {
    private static final ResourceLocation VANILLA_CONTAINER_TEXTURE =
            new ResourceLocation(Tags.MOD_ID, "textures/gui/magicbook/magicbook.png");
    private static final int SLOT_SPACING = 18;

    private final InventoryPlayer playerInv;
    private final int leftSlots;
    private final int rightSlots;
    private final int customRows;

    // Center X of each side's slot group in GUI-local coords
    private static final int LEFT_CENTER_X = 52;
    private static final int RIGHT_CENTER_X = 124;
    private static final int SLOT_START_Y = 18;

    public GuiMagicBook(InventoryPlayer playerInv, ItemStack bookStack) {
        super(new ContainerMagicBook(playerInv, bookStack));
        this.playerInv = playerInv;
        BookInventory inventory = ((com.smd.tcongreedyaddon.tools.magicbook.MagicBook) bookStack.getItem()).getInventory(bookStack);
        this.leftSlots = inventory.getLeftSlots();
        this.rightSlots = inventory.getRightSlots();
        this.customRows = Math.max(getRows(inventory.getLeftSlots()), getRows(inventory.getRightSlots()));
        this.xSize = 176;
        this.ySize = 114 + customRows * SLOT_SPACING;
    }

    private static int getRows(int slots) {
        return (slots + 1) / 2;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int topHeight = 17 + customRows * SLOT_SPACING;

        // Both sections use the same vanilla texture source — no seam between them.
        mc.getTextureManager().bindTexture(VANILLA_CONTAINER_TEXTURE);
        // Upper section: borrow the chest-row area of the texture for a seamless panel.
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, topHeight);
        // Lower section: player inventory (unchanged).
        drawTexturedModalRect(guiLeft, guiTop + topHeight, 0, 126, xSize, 96);

        // Custom slots drawn as GL overlay on top of the texture.
        drawDynamicSlotsForSide(LEFT_CENTER_X, leftSlots);
        drawDynamicSlotsForSide(RIGHT_CENTER_X, rightSlots);
    }

    /**
     * Draw slots for one side, each row centered at sideCenter.
     * Full rows (2 slots) have a 1-slot (18px) gap between columns.
     * The last row with only 1 slot is centered individually.
     */
    private void drawDynamicSlotsForSide(int sideCenter, int slotCount) {
        for (int row = 0, remaining = slotCount; remaining > 0; row++, remaining -= 2) {
            int slotsInRow = Math.min(2, remaining);
            for (int col = 0; col < slotsInRow; col++) {
                // 2-slot row: adjacent, col0 = center-18, col1 = center (no intra-side gap)
                // 1-slot row: centered at sideCenter
                int slotX = (slotsInRow == 2) ? sideCenter - 18 + col * 18 : sideCenter - 9;
                int slotY = SLOT_START_Y + row * SLOT_SPACING;
                // Vanilla border starts 1px before the container slot position
                drawVanillaSlot(guiLeft + slotX - 1, guiTop + slotY - 1);
            }
        }
    }

    private void drawVanillaSlot(int x, int y) {
        drawRect(x, y, x + 18, y + 18, 0xFF8B8B8B);
        drawRect(x, y, x + 18, y + 1, 0xFF373737);
        drawRect(x, y, x + 1, y + 18, 0xFF373737);
        drawRect(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
        drawRect(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String leftLabel = I18n.format("container.magicbook.left");
        fontRenderer.drawString(leftLabel, LEFT_CENTER_X - fontRenderer.getStringWidth(leftLabel) / 2, 6, 0x404040);
        String rightLabel = I18n.format("container.magicbook.right");
        fontRenderer.drawString(rightLabel, RIGHT_CENTER_X - fontRenderer.getStringWidth(rightLabel) / 2, 6, 0x404040);
        fontRenderer.drawString(playerInv.getDisplayName().getUnformattedText(), 8, ySize - 94, 0x404040);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 绘制默认的背景（屏幕外的暗色背景）
        this.drawDefaultBackground();
        // 调用父类方法绘制所有槽位、物品和控件
        super.drawScreen(mouseX, mouseY, partialTicks);
        // 渲染悬停的物品提示
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}