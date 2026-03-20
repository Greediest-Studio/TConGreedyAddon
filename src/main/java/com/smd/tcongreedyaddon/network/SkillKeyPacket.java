package com.smd.tcongreedyaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SkillKeyPacket implements IMessage {

    public enum Action {
        PRESS,
        RELEASE;

        public static Action fromOrdinal(int ordinal) {
            Action[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return PRESS;
            }
            return values[ordinal];
        }
    }

    public Action action = Action.PRESS;

    public SkillKeyPacket() {
    }

    public SkillKeyPacket(Action action) {
        this.action = action == null ? Action.PRESS : action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = Action.fromOrdinal(buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
    }
}
