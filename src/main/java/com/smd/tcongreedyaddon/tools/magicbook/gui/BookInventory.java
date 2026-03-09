package com.smd.tcongreedyaddon.tools.magicbook.gui;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class BookInventory implements IItemHandlerModifiable {
    private final ItemStack bookStack;
    private final ItemStackHandler leftHandler;
    private final ItemStackHandler rightHandler;
    private final int leftSlots;
    private final int rightSlots;

    public BookInventory(ItemStack bookStack, int leftSlots, int rightSlots) {
        this.bookStack = bookStack;
        this.leftSlots = leftSlots;
        this.rightSlots = rightSlots;
        this.leftHandler = new ItemStackHandler(leftSlots);
        this.rightHandler = new ItemStackHandler(rightSlots);
        deserializeNBT(bookStack.getOrCreateSubCompound("BookInventory"));
    }

    @Override
    public int getSlots() {
        return leftHandler.getSlots() + rightHandler.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < leftSlots) {
            return leftHandler.getStackInSlot(slot);
        } else {
            return rightHandler.getStackInSlot(slot - leftSlots);
        }
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot < leftSlots) {
            leftHandler.setStackInSlot(slot, stack);
        } else {
            rightHandler.setStackInSlot(slot - leftSlots, stack);
        }
        onContentsChanged(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack result;
        if (slot < leftSlots) {
            result = leftHandler.insertItem(slot, stack, simulate);
        } else {
            result = rightHandler.insertItem(slot - leftSlots, stack, simulate);
        }
        if (!simulate && result != stack) {
            onContentsChanged(slot);
        }
        return result;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack extracted;
        if (slot < leftSlots) {
            extracted = leftHandler.extractItem(slot, amount, simulate);
        } else {
            extracted = rightHandler.extractItem(slot - leftSlots, amount, simulate);
        }
        if (!simulate && !extracted.isEmpty()) {
            onContentsChanged(slot);
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (!(stack.getItem() instanceof MagicPageItem)) return false;
        MagicPageItem page = (MagicPageItem) stack.getItem();

        // 侧别检查
        if (slot < leftSlots && page.getSlotType() != MagicPageItem.SlotType.LEFT) return false;
        if (slot >= leftSlots && page.getSlotType() != MagicPageItem.SlotType.RIGHT) return false;

        // 重复检查
        return !isDuplicatePage(stack, slot);
    }

    public int getLeftSlots() { return leftSlots; }
    public int getRightSlots() { return rightSlots; }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Left", leftHandler.serializeNBT());
        nbt.setTag("Right", rightHandler.serializeNBT());
        return nbt;
    }

    public void deserializeNBT(NBTTagCompound nbt) {
        // 重置 handler 为空
        for (int i = 0; i < leftHandler.getSlots(); i++) {
            leftHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < rightHandler.getSlots(); i++) {
            rightHandler.setStackInSlot(i, ItemStack.EMPTY);
        }

        if (nbt.hasKey("Left", 10)) {
            loadHandlerFromNBT(leftHandler, nbt.getCompoundTag("Left"), leftSlots);
        }
        if (nbt.hasKey("Right", 10)) {
            loadHandlerFromNBT(rightHandler, nbt.getCompoundTag("Right"), rightSlots);
        }
    }

    private void loadHandlerFromNBT(ItemStackHandler handler, NBTTagCompound handlerNBT, int expectedSize) {
        if (!handlerNBT.hasKey("Items", 9)) return;
        NBTTagList items = handlerNBT.getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound slotTag = items.getCompoundTagAt(i);
            int slot = slotTag.getInteger("Slot");
            if (slot >= 0 && slot < expectedSize) {
                ItemStack stack = new ItemStack(slotTag);
                if (!stack.isEmpty()) {
                    handler.setStackInSlot(slot, stack);
                }
            }
            // 超出 expectedSize 的忽略，后续由 cleanup 弹出
        }
    }

    protected void onContentsChanged(int slot) {
        bookStack.getOrCreateSubCompound("BookInventory").merge(serializeNBT());
    }

    private boolean isDuplicatePage(@Nonnull ItemStack newStack, int excludeSlot) {
        String newId = ((MagicPageItem) newStack.getItem()).getPageIdentifier();
        // 左槽
        for (int i = 0; i < leftHandler.getSlots(); i++) {
            if (i == excludeSlot && excludeSlot < leftSlots) continue;
            ItemStack stack = leftHandler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof MagicPageItem) {
                if (newId.equals(((MagicPageItem) stack.getItem()).getPageIdentifier())) return true;
            }
        }
        // 右槽
        for (int i = 0; i < rightHandler.getSlots(); i++) {
            int globalSlot = leftSlots + i;
            if (globalSlot == excludeSlot) continue;
            ItemStack stack = rightHandler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof MagicPageItem) {
                if (newId.equals(((MagicPageItem) stack.getItem()).getPageIdentifier())) return true;
            }
        }
        return false;
    }
}