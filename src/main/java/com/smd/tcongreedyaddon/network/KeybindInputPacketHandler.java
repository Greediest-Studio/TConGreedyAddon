package com.smd.tcongreedyaddon.network;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class KeybindInputPacketHandler implements IMessageHandler<KeybindInputPacket, IMessage> {
    @Override
    public IMessage onMessage(KeybindInputPacket message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            ItemStack held = player.getHeldItemMainhand();
            if (!(held.getItem() instanceof MagicBook)) {
                return;
            }
            MagicBook book = (MagicBook) held.getItem();
            book.handleKeybindInput(
                    held,
                    player,
                    message.sequence,
                    message.side,
                    message.channel,
                    message.action,
                    message.clientTick
            );
        });
        return null;
    }
}
