package com.smd.tcongreedyaddon.event;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.gui.BookInventory;
import com.smd.tcongreedyaddon.tools.magicbook.page.UnifiedMagicPage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.utils.ToolHelper;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class MagicBookEventHandler {

    @SubscribeEvent
    public static void onLivingJump(LivingJumpEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntity();
        handleEvent(player, event);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntity();
        handleEvent(player, event);
    }

    /**
     * 通用事件处理：遍历玩家主手魔导书，对每个书页调用 onEvent 方法。
     */
    private static void handleEvent(EntityPlayer player, Event forgeEvent) {
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.getItem() instanceof MagicBook) {
            processBookEvent((MagicBook) mainHand.getItem(), mainHand, player, forgeEvent);
        }
    }

    private static void processBookEvent(MagicBook book, ItemStack bookStack, EntityPlayer player, Event forgeEvent) {
        if (ToolHelper.isBroken(bookStack)) return;

        BookInventory inv = book.getInventory(bookStack);
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof UnifiedMagicPage)) continue;

            UnifiedMagicPage page = (UnifiedMagicPage) pageStack.getItem();
            MagicPageItem.SlotType slotType = (slot < inv.getLeftSlots()) ? MagicPageItem.SlotType.LEFT : MagicPageItem.SlotType.RIGHT;

            NBTTagCompound pageData = pageStack.getTagCompound();
            if (pageData == null) {
                pageData = new NBTTagCompound();
            }

            NBTTagCompound beforeData = pageData.copy();
            ItemStack beforeStack = pageStack.copy();

            page.onEvent(forgeEvent, player, bookStack, pageStack, pageData, slotType);

            boolean dataChanged = !pageData.equals(beforeData);
            boolean stackChanged = !ItemStack.areItemStacksEqual(beforeStack, pageStack);
            if (dataChanged || stackChanged) {
                pageStack.setTagCompound(pageData);
                inv.setStackInSlot(slot, pageStack);
            }
        }
    }
}
