package com.smd.tcongreedyaddon.tools.magicbook.gui;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMagicBook extends Container {
    private static final int LEFT_CENTER_X = 52;
    private static final int RIGHT_CENTER_X = 124;
    private static final int CUSTOM_SLOT_START_Y = 18;
    private static final int SLOT_SPACING = 18;

    private final BookInventory inventory;
    private final int leftSlots;
    private final int rightSlots;
    private final int customRows;

    public ContainerMagicBook(InventoryPlayer playerInv, ItemStack bookStack) {
        this.inventory = ((MagicBook) bookStack.getItem()).getInventory(bookStack);
        this.leftSlots = inventory.getLeftSlots();
        this.rightSlots = inventory.getRightSlots();
        this.customRows = Math.max(getRows(leftSlots), getRows(rightSlots));

        // 左槽
        for (int i = 0; i < leftSlots; i++) {
            final int slotX = getSlotX(i, leftSlots, LEFT_CENTER_X);
            final int slotY = CUSTOM_SLOT_START_Y + (i / 2) * SLOT_SPACING;
            addSlotToContainer(new SlotItemHandler(inventory, i, slotX, slotY) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return getItemHandler().isItemValid(getSlotIndex(), stack);
                }
            });
        }
        // 右槽
        for (int i = 0; i < inventory.getRightSlots(); i++) {
            int idx = leftSlots + i;
            final int slotX = getSlotX(i, rightSlots, RIGHT_CENTER_X);
            final int slotY = CUSTOM_SLOT_START_Y + (i / 2) * SLOT_SPACING;
            addSlotToContainer(new SlotItemHandler(inventory, idx, slotX, slotY) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return getItemHandler().isItemValid(getSlotIndex(), stack);
                }
            });
        }

        int playerInvStartY = 14 + customRows * SLOT_SPACING + 17;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int slotIndex = col + row * 9 + 9;
                addSlotToContainer(new Slot(playerInv, slotIndex,
                        8 + col * SLOT_SPACING, playerInvStartY + row * SLOT_SPACING));
            }
        }

        int currentItemSlot = playerInv.currentItem;
        for (int col = 0; col < 9; ++col) {
            int slotIndex = col;
            ItemStack stackInSlot = playerInv.getStackInSlot(slotIndex);
            if (col == currentItemSlot && ItemStack.areItemStacksEqual(bookStack, stackInSlot)) {
                addSlotToContainer(new LockedSlot(playerInv, slotIndex,
                        8 + col * SLOT_SPACING, playerInvStartY + 58));
            } else {
                addSlotToContainer(new Slot(playerInv, slotIndex,
                        8 + col * SLOT_SPACING, playerInvStartY + 58));
            }
        }
    }

    private static int getRows(int slots) {
        return (slots + 1) / 2;
    }

    private static int getSlotX(int slotIndex, int totalSlots, int sideCenter) {
        int row = slotIndex / 2;
        int col = slotIndex % 2;
        int slotsInRow = Math.min(2, totalSlots - row * 2);
        return (slotsInRow == 2) ? sideCenter - 18 + col * 18 : sideCenter - 9;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();

            int totalCustomSlots = leftSlots + rightSlots;

            if (index < totalCustomSlots) {
                if (!mergeItemStack(stack, totalCustomSlots, inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                if (!mergeItemStack(stack, 0, totalCustomSlots, false))
                    return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    /**
     * 锁定槽位：禁止玩家取出或放入物品，但允许容器内部更新。
     */
    private static class LockedSlot extends Slot {
        public LockedSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}