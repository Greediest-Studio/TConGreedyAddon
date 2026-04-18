package com.smd.tcongreedyaddon.tools.magicbook.keybind;

public final class KeybindTuningConfig {

    public static final int DEFAULT_LONG_PRESS_TICKS = 10;
    public static final int DEFAULT_TAP_MAX_TICKS = 6;
    public static final int DEFAULT_CHORD_LONG_TICKS = 10;
    public static final int DEFAULT_CHORD_TAP_MAX_TICKS = 6;
    public static final int DEFAULT_HOLD_TRIGGER_TICKS = 10;
    public static final int DEFAULT_ACTION_LOCK_MIN_TICKS = 1;

    private static volatile int longPressTicks = DEFAULT_LONG_PRESS_TICKS;
    private static volatile int tapMaxTicks = DEFAULT_TAP_MAX_TICKS;
    private static volatile int chordLongTicks = DEFAULT_CHORD_LONG_TICKS;
    private static volatile int chordTapMaxTicks = DEFAULT_CHORD_TAP_MAX_TICKS;
    private static volatile int holdTriggerTicks = DEFAULT_HOLD_TRIGGER_TICKS;
    private static volatile int actionLockMinTicks = DEFAULT_ACTION_LOCK_MIN_TICKS;

    private KeybindTuningConfig() {
    }

    public static void apply(int newLongPressTicks,
                             int newTapMaxTicks,
                             int newChordLongTicks,
                             int newChordTapMaxTicks,
                             int newHoldTriggerTicks,
                             int newActionLockMinTicks) {
        longPressTicks = clamp(newLongPressTicks, 1, 40);
        tapMaxTicks = clamp(newTapMaxTicks, 1, 20);
        chordLongTicks = clamp(newChordLongTicks, 1, 40);
        chordTapMaxTicks = clamp(newChordTapMaxTicks, 1, 20);
        holdTriggerTicks = clamp(newHoldTriggerTicks, 1, 40);
        actionLockMinTicks = clamp(newActionLockMinTicks, 0, 20);
    }

    public static int getLongPressTicks() {
        return longPressTicks;
    }

    public static int getTapMaxTicks() {
        return tapMaxTicks;
    }

    public static int getChordLongTicks() {
        return chordLongTicks;
    }

    public static int getChordTapMaxTicks() {
        return chordTapMaxTicks;
    }

    public static int getHoldTriggerTicks() {
        return holdTriggerTicks;
    }

    public static int getActionLockMinTicks() {
        return actionLockMinTicks;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
