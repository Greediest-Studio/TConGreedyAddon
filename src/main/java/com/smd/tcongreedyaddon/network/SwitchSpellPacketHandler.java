package com.smd.tcongreedyaddon.network;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SwitchSpellPacketHandler implements IMessageHandler<SwitchSpellPacket, IMessage> {
    @Override
    public IMessage onMessage(SwitchSpellPacket message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {

            if (message.slot != 0 && message.slot != 1) {
                return;
            }

            ItemStack held = player.getHeldItemMainhand();
            if (!(held.getItem() instanceof MagicBook)) return;

            MagicBook book = (MagicBook) held.getItem();
            MagicPageItem.SlotType slotType = (message.slot == 0) ? MagicPageItem.SlotType.LEFT : MagicPageItem.SlotType.RIGHT;
            book.switchSpell(held, slotType, message.next);
        });
        return null;
    }
}