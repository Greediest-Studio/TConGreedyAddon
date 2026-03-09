package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * 触发源，表示法术被调用的时机。
 * 可以是预定义的类型，也可以是具体的 Forge 事件。
 */
public class TriggerSource {
    public enum Type {
        LEFT_CLICK,   // 左键点击实体
        RIGHT_CLICK,  // 右键使用书
        TICK          // 每 tick 更新
    }

    private final Type type;
    private final Event event; // 当 type == null 时，表示这是一个自定义事件

    private TriggerSource(Type type, Event event) {
        this.type = type;
        this.event = event;
    }

    public static TriggerSource leftClick() {
        return new TriggerSource(Type.LEFT_CLICK, null);
    }

    public static TriggerSource rightClick() {
        return new TriggerSource(Type.RIGHT_CLICK, null);
    }

    public static TriggerSource tick() {
        return new TriggerSource(Type.TICK, null);
    }

    public static TriggerSource event(Event event) {
        return new TriggerSource(null, event);
    }

    public boolean isType(Type type) {
        return this.type == type;
    }

    public boolean isEvent() {
        return event != null;
    }

    public Event getEvent() {
        return event;
    }

    public Type getType() {
        return type;
    }
}