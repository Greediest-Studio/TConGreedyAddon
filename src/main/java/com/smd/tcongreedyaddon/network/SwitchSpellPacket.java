package com.smd.tcongreedyaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SwitchSpellPacket implements IMessage {
    public int slot;

    public SwitchSpellPacket() {}

    public SwitchSpellPacket(int slot) {
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slot);
    }

    public int getSlot() {
        return slot;
    }
}