package com.smd.tcongreedyaddon.event;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.gui.BookInventory;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IEventSpell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.utils.ToolHelper;

import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class MagicBookEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        handleEventForPlayer(event, player);
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) return;
        handleEventForPlayer(event, event.getEntityPlayer());
    }

    private static void handleEventForPlayer(Event event, EntityPlayer player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();

        if (mainHand.getItem() instanceof MagicBook) {
            processBook((MagicBook) mainHand.getItem(), mainHand, player, event);
        }
        if (offHand.getItem() instanceof MagicBook) {
            processBook((MagicBook) offHand.getItem(), offHand, player, event);
        }
    }

    private static void processBook(MagicBook book, ItemStack bookStack, EntityPlayer player, Event event) {
        if (ToolHelper.isBroken(bookStack)) return;

        BookInventory inv = book.getInventory(bookStack);

        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack pageStack = inv.getStackInSlot(slot);
            if (pageStack.isEmpty() || !(pageStack.getItem() instanceof MagicPageItem)) continue;

            MagicPageItem pageItem = (MagicPageItem) pageStack.getItem();
            if (!(pageItem instanceof IEventSpell)) continue;

            IEventSpell spell = (IEventSpell) pageItem;

            List<Class<? extends Event>> listeningEvents = spell.getListeningEvents();
            if (!listeningEvents.isEmpty() && !listeningEvents.contains(event.getClass())) {
                continue;
            }

            MagicPageItem.SlotType slotType = (slot < inv.getLeftSlots()) ? MagicPageItem.SlotType.LEFT : MagicPageItem.SlotType.RIGHT;

            NBTTagCompound pageData = pageStack.getTagCompound();
            if (pageData == null) pageData = new NBTTagCompound();

            boolean success = spell.onEvent(event, player, bookStack, pageStack, pageData, slotType);
            if (success) {
                pageStack.setTagCompound(pageData);
                inv.setStackInSlot(slot, pageStack);
                ToolHelper.damageTool(bookStack, MagicBook.DURABILITY_COST, player);
            }
        }

    }
}