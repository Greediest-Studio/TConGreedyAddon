package com.smd.tcongreedyaddon.network;

import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.IKeybindSkillSpell;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SkillKeyPacketHandler implements IMessageHandler<SkillKeyPacket, IMessage> {
    @Override
    public IMessage onMessage(SkillKeyPacket message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            ItemStack held = player.getHeldItemMainhand();
            if (!(held.getItem() instanceof MagicBook)) {
                return;
            }

            MagicBook book = (MagicBook) held.getItem();
            IKeybindSkillSpell.KeyAction action = message.action == SkillKeyPacket.Action.RELEASE
                    ? IKeybindSkillSpell.KeyAction.RELEASE
                    : IKeybindSkillSpell.KeyAction.PRESS;
            book.triggerKeybindSkill(held, player, "q", action);
        });
        return null;
    }
}
