package com.smd.tcongreedyaddon.network;

import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindAction;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindChannel;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindSide;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class KeybindInputPacket implements IMessage {
    public int sequence;
    public KeybindSide side = KeybindSide.LEFT;
    public KeybindChannel channel = KeybindChannel.A;
    public KeybindAction action = KeybindAction.PRESS;
    public int clientTick;

    public KeybindInputPacket() {
    }

    public KeybindInputPacket(int sequence, KeybindSide side, KeybindChannel channel, KeybindAction action, int clientTick) {
        this.sequence = sequence;
        if (side != null) {
            this.side = side;
        }
        if (channel != null) {
            this.channel = channel;
        }
        if (action != null) {
            this.action = action;
        }
        this.clientTick = clientTick;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        sequence = buf.readInt();
        side = readEnum(buf, KeybindSide.values(), KeybindSide.LEFT);
        channel = readEnum(buf, KeybindChannel.values(), KeybindChannel.A);
        action = readEnum(buf, KeybindAction.values(), KeybindAction.PRESS);
        clientTick = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(sequence);
        buf.writeByte(side.ordinal());
        buf.writeByte(channel.ordinal());
        buf.writeByte(action.ordinal());
        buf.writeInt(clientTick);
    }

    private static <T extends Enum<T>> T readEnum(ByteBuf buf, T[] values, T fallback) {
        int ordinal = buf.readByte();
        if (ordinal < 0 || ordinal >= values.length) {
            return fallback;
        }
        return values[ordinal];
    }
}
