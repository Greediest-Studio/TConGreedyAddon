package com.smd.tcongreedyaddon.network;

import com.smd.tcongreedyaddon.client.StrandConnectionRenderer;
import com.smd.tcongreedyaddon.event.StrandConnectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StrandConnectionSyncPacketHandler implements IMessageHandler<StrandConnectionSyncPacket, IMessage> {
    @Override
    public IMessage onMessage(StrandConnectionSyncPacket message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            StrandConnectionManager.setClientVisualState(message.active, message.meleeReady);
            if (message.active) {
                StrandConnectionRenderer.setClientConnection(
                        message.dimension,
                        new Vec3d(message.anchorX, message.anchorY, message.anchorZ)
                );
            } else {
                StrandConnectionRenderer.clearClientConnection();
            }
        });
        return null;
    }
}
