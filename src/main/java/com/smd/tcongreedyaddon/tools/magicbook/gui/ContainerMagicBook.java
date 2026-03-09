package com.smd.tcongreedyaddon.tools.magicbook.gui;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMagicBook extends Container {
    private final BookInventory inventory;
    private final int leftSlots;

    public ContainerMagicBook(InventoryPlayer playerInv, ItemStack bookStack) {
        this.inventory = ((MagicBook) bookStack.getItem()).getInventory(bookStack);
        this.leftSlots = inventory.getLeftSlots();

        // 左槽
        for (int i = 0; i < leftSlots; i++) {
            addSlotToContainer(new SlotItemHandler(inventory, i, 34 + (i % 2) * 18, 18 + (i / 2) * 18) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return getItemHandler().isItemValid(getSlotIndex(), stack);
                }
            });
        }
        // 右槽
        for (int i = 0; i < inventory.getRightSlots(); i++) {
            int idx = leftSlots + i;
            addSlotToContainer(new SlotItemHandler(inventory, idx, 106 + (i % 2) * 18, 18 + (i / 2) * 18) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return getItemHandler().isItemValid(getSlotIndex(), stack);
                }
            });
        }

        // 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
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

            int totalCustomSlots = leftSlots + inventory.getRightSlots();

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
}