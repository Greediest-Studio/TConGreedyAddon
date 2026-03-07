package com.smd.tcongreedyaddon.network;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

// SwitchSpellPacketHandler.java
public class SwitchSpellPacketHandler implements IMessageHandler<SwitchSpellPacket, IMessage> {
    @Override
    public IMessage onMessage(SwitchSpellPacket message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            ItemStack held = player.getHeldItemMainhand();
            if (!(held.getItem() instanceof MagicBook)) return;

            NBTTagCompound tag = held.getTagCompound();
            if (tag == null) return;

            String slotKey = message.slot == 0 ? MagicBook.TAG_LEFT_PAGE : MagicBook.TAG_RIGHT_PAGE;
            NBTTagCompound pageData = tag.getCompoundTag(slotKey);
            if (pageData.isEmpty()) return;

            String pageId = pageData.getString(MagicBook.TAG_PAGE_ID);
            Item item = Item.REGISTRY.getObject(new ResourceLocation(pageId));
            if (!(item instanceof MagicPageItem)) return;

            MagicPageItem page = (MagicPageItem) item;
            page.nextSpell(held, pageData);
            tag.setTag(slotKey, pageData);
            held.setTagCompound(tag);
        });
        return null;
    }
}