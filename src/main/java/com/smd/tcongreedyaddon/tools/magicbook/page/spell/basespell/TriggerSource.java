package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import net.minecraftforge.fml.common.eventhandler.Event;

public class TriggerSource {
    public enum Type {
        LEFT_CLICK,
        RIGHT_CLICK,
        TICK,
        HOLD_TICK,
        HOLD_RELEASE,
        KEY_GESTURE,
        KEY_HOLD_START,
        KEY_HOLD_TICK,
        KEY_HOLD_END
    }

    private static final TriggerSource LEFT_CLICK = new TriggerSource(Type.LEFT_CLICK, null);
    private static final TriggerSource RIGHT_CLICK = new TriggerSource(Type.RIGHT_CLICK, null);
    private static final TriggerSource TICK = new TriggerSource(Type.TICK, null);
    private static final TriggerSource HOLD_TICK = new TriggerSource(Type.HOLD_TICK, null);
    private static final TriggerSource HOLD_RELEASE = new TriggerSource(Type.HOLD_RELEASE, null);
    private static final TriggerSource KEY_GESTURE = new TriggerSource(Type.KEY_GESTURE, null);
    private static final TriggerSource KEY_HOLD_START = new TriggerSource(Type.KEY_HOLD_START, null);
    private static final TriggerSource KEY_HOLD_TICK = new TriggerSource(Type.KEY_HOLD_TICK, null);
    private static final TriggerSource KEY_HOLD_END = new TriggerSource(Type.KEY_HOLD_END, null);

    private final Type type;
    private final Event event;

    private TriggerSource(Type type, Event event) {
        this.type = type;
        this.event = event;
    }

    public static TriggerSource leftClick() {
        return LEFT_CLICK;
    }

    public static TriggerSource rightClick() {
        return RIGHT_CLICK;
    }

    public static TriggerSource tick() {
        return TICK;
    }

    public static TriggerSource holdTick() {
        return HOLD_TICK;
    }

    public static TriggerSource holdRelease() {
        return HOLD_RELEASE;
    }

    public static TriggerSource keyGesture() {
        return KEY_GESTURE;
    }

    public static TriggerSource keyHoldStart() {
        return KEY_HOLD_START;
    }

    public static TriggerSource keyHoldTick() {
        return KEY_HOLD_TICK;
    }

    public static TriggerSource keyHoldEnd() {
        return KEY_HOLD_END;
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
