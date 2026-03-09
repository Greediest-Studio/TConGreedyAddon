package com.smd.tcongreedyaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SwitchSpellPacket implements IMessage {
    public int slot;      // 0=左, 1=右
    public boolean next;  // true=下一个, false=上一个

    public SwitchSpellPacket() {}

    public SwitchSpellPacket(int slot, boolean next) {
        this.slot = slot;
        this.next = next;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = buf.readInt();
        next = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slot);
        buf.writeBoolean(next);
    }
}