package com.smd.tcongreedyaddon.tools.magicbook.gui;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class BookInventory extends ItemStackHandler {
    private final ItemStack bookStack;
    private final int leftSlots;
    private final int rightSlots;

    public BookInventory(ItemStack bookStack, int leftSlots, int rightSlots) {
        super(leftSlots + rightSlots);
        this.bookStack = bookStack;
        this.leftSlots = leftSlots;
        this.rightSlots = rightSlots;
        deserializeNBT(bookStack.getOrCreateSubCompound("Inventory"));
    }

    @Override
    protected void onContentsChanged(int slot) {
        bookStack.getOrCreateSubCompound("Inventory").merge(serializeNBT());
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (!(stack.getItem() instanceof MagicPageItem)) return false;
        MagicPageItem page = (MagicPageItem) stack.getItem();

        if (slot < leftSlots && page.getSlotType() != MagicPageItem.SlotType.LEFT) return false;
        if (slot >= leftSlots && page.getSlotType() != MagicPageItem.SlotType.RIGHT) return false;

        if (isDuplicatePage(stack, slot)) {
            return false;
        }
        return super.isItemValid(slot, stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!simulate) {
            ItemStack existing = getStackInSlot(slot);
            if (existing.isEmpty()) {

                if (isDuplicatePage(stack, slot)) {
                    return stack;
                }
            } else {
                if (isDuplicatePage(stack, slot)) {
                    return stack;
                }
            }
        }
        return super.insertItem(slot, stack, simulate);
    }

    /**
     * 检查除了指定槽位外，是否已经存在相同类型的书签
     */
    private boolean isDuplicatePage(@Nonnull ItemStack newStack, int excludeSlot) {
        if (!(newStack.getItem() instanceof MagicPageItem)) return false;
        String newPageId = ((MagicPageItem) newStack.getItem()).getPageIdentifier();
        for (int i = 0; i < getSlots(); i++) {
            if (i == excludeSlot) continue;
            ItemStack stack = getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof MagicPageItem)) continue;
            String existingId = ((MagicPageItem) stack.getItem()).getPageIdentifier();
            if (existingId.equals(newPageId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    public int getLeftSlots() { return leftSlots; }
    public int getRightSlots() { return rightSlots; }
}