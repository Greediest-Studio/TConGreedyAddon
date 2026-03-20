package com.smd.tcongreedyaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class StrandConnectionSyncPacket implements IMessage {
    public boolean active;
    public boolean meleeReady;
    public int dimension;
    public double anchorX;
    public double anchorY;
    public double anchorZ;

    public StrandConnectionSyncPacket() {
    }

    public StrandConnectionSyncPacket(boolean active, boolean meleeReady,
                                      int dimension, double anchorX, double anchorY, double anchorZ) {
        this.active = active;
        this.meleeReady = meleeReady;
        this.dimension = dimension;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.anchorZ = anchorZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        active = buf.readBoolean();
        meleeReady = buf.readBoolean();
        dimension = buf.readInt();
        anchorX = buf.readDouble();
        anchorY = buf.readDouble();
        anchorZ = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(active);
        buf.writeBoolean(meleeReady);
        buf.writeInt(dimension);
        buf.writeDouble(anchorX);
        buf.writeDouble(anchorY);
        buf.writeDouble(anchorZ);
    }
}
