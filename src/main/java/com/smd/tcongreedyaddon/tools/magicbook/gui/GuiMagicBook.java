package com.smd.tcongreedyaddon.tools.magicbook.gui;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class GuiMagicBook extends GuiContainer {
    private final InventoryPlayer playerInv;
    private final ItemStack bookStack;
    private final MagicBook book;
    private final int leftSlots;
    private final int rightSlots;

    // 对称后的槽位起始坐标
    private static final int LEFT_SLOT_X = 34;
    private static final int RIGHT_SLOT_X = 106;
    private static final int SLOT_Y = 18;
    private static final int SLOT_SPACING = 18;

    public GuiMagicBook(InventoryPlayer playerInv, ItemStack bookStack) {
        super(new ContainerMagicBook(playerInv, bookStack));
        this.playerInv = playerInv;
        this.bookStack = bookStack;
        this.book = (MagicBook) bookStack.getItem();
        this.leftSlots = book.getInventory(bookStack).getLeftSlots();
        this.rightSlots = book.getInventory(bookStack).getRightSlots();
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // 绘制半透明黑色背景（覆盖整个 GUI 区域）
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0x80000000);

        // 绘制槽位边框
        drawSlotBorders();
    }

    private void drawSlotBorders() {
        // 左槽边框
        for (int i = 0; i < leftSlots; i++) {
            int x = guiLeft + LEFT_SLOT_X + (i % 2) * SLOT_SPACING;
            int y = guiTop + SLOT_Y + (i / 2) * SLOT_SPACING;
            drawSlotBorder(x, y, 18, 0xFFC0C0C0); // 银白色边框
        }
        // 右槽边框
        for (int i = 0; i < rightSlots; i++) {
            int x = guiLeft + RIGHT_SLOT_X + (i % 2) * SLOT_SPACING;
            int y = guiTop + SLOT_Y + (i / 2) * SLOT_SPACING;
            drawSlotBorder(x, y, 18, 0xFFC0C0C0);
        }
        // 玩家背包槽位边框由原版自动绘制，无需额外处理
    }

    private void drawSlotBorder(int x, int y, int size, int color) {
        Gui.drawRect(x, y, x + size, y + 1, color); // 上边
        Gui.drawRect(x, y + size - 1, x + size, y + size, color); // 下边
        Gui.drawRect(x, y, x + 1, y + size, color); // 左边
        Gui.drawRect(x + size - 1, y, x + size, y + size, color); // 右边
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // 左右标签（位于槽位上方居中）
        fontRenderer.drawString(I18n.format("container.magicbook.left"), LEFT_SLOT_X, 6, 0x404040);
        fontRenderer.drawString(I18n.format("container.magicbook.right"), RIGHT_SLOT_X, 6, 0x404040);
        // 玩家背包标题
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