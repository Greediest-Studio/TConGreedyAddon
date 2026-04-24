package com.smd.tcongreedyaddon.tools.magicbook.keybind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KeybindGestureState {
    private final SideState left = new SideState();
    private final SideState right = new SideState();
    private int lastSequence = -1;

    public static final class SideGesture {
        public final KeybindSide side;
        public final GestureType gesture;

        public SideGesture(KeybindSide side, GestureType gesture) {
            this.side = side;
            this.gesture = gesture;
        }
    }

    public List<GestureType> onInput(int sequence, KeybindSide side, KeybindChannel channel,
                                     KeybindAction action, long serverTick) {
        if (side == null || channel == null || action == null) {
            return Collections.emptyList();
        }
        if (sequence == lastSequence) {
            return Collections.emptyList();
        }
        lastSequence = sequence;

        SideState state = side == KeybindSide.LEFT ? left : right;
        return state.onInput(channel, action, serverTick);
    }

    public List<SideGesture> pollTickGestures(long serverTick) {
        List<SideGesture> gestures = new ArrayList<>(2);
        for (GestureType gesture : left.pollTickGestures(serverTick)) {
            gestures.add(new SideGesture(KeybindSide.LEFT, gesture));
        }
        for (GestureType gesture : right.pollTickGestures(serverTick)) {
            gestures.add(new SideGesture(KeybindSide.RIGHT, gesture));
        }
        return gestures;
    }

    private static final class ChannelState {
        boolean down;
        boolean consumed;
        boolean longFired;
        long pressTick = -1L;

        long getDuration(long currentTick) {
            if (!down || pressTick < 0L) return 0L;
            return currentTick - pressTick + 1L;
        }
    }

    private static final class SideState {
        private final ChannelState stateA = new ChannelState();
        private final ChannelState stateB = new ChannelState();

        private long overlapStartTick = -1L;
        private boolean pendingChordTap;
        private long pendingChordStartTick = -1L;

        private ChannelState channel(KeybindChannel ch) {
            return ch == KeybindChannel.A ? stateA : stateB;
        }

        private List<GestureType> onInput(KeybindChannel channel, KeybindAction action, long tick) {
            return action == KeybindAction.PRESS ? onPress(channel, tick) : onRelease(channel, tick);
        }

        private List<GestureType> onPress(KeybindChannel channel, long tick) {
            ChannelState chState = channel(channel);
            if (chState.down) {
                return Collections.emptyList();
            }

            List<GestureType> result = new ArrayList<>(1);
            chState.down = true;
            chState.pressTick = tick;
            chState.consumed = false;
            chState.longFired = false;

            result.add(channel == KeybindChannel.A ? GestureType.PRESS_A : GestureType.PRESS_B);

            if (stateA.down && stateB.down) {
                overlapStartTick = tick;
                pendingChordTap = false;
                pendingChordStartTick = -1L;
            }
            return result;
        }

        private List<GestureType> onRelease(KeybindChannel channel, long tick) {
            ChannelState chState = channel(channel);
            if (!chState.down) {
                return Collections.emptyList();
            }

            List<GestureType> result = new ArrayList<>(1);
            KeybindChannel other = channel == KeybindChannel.A ? KeybindChannel.B : KeybindChannel.A;
            ChannelState otherState = channel(other);
            boolean otherDown = otherState.down;
            long releasedDuration = chState.getDuration(tick);

            result.add(channel == KeybindChannel.A ? GestureType.RELEASE_A : GestureType.RELEASE_B);

            if (otherDown && isLongPressed(otherState, tick) && isTapDuration(releasedDuration)) {
                result.add(channel == KeybindChannel.B ? GestureType.HOLD_A_TAP_B : GestureType.HOLD_B_TAP_A);
                chState.consumed = true;
                otherState.consumed = true;
                pendingChordTap = false;
            } else if (otherDown && overlapStartTick >= 0L && !chState.consumed && !otherState.consumed) {
                long overlapDuration = tick - overlapStartTick + 1L;
                if (isChordLongDuration(overlapDuration)) {
                    result.add(GestureType.CHORD_LONG);
                    chState.consumed = true;
                    otherState.consumed = true;
                    pendingChordTap = false;
                } else if (isTapDuration(releasedDuration) && isTapDuration(otherState.getDuration(tick))) {
                    pendingChordTap = true;
                    pendingChordStartTick = overlapStartTick;
                    chState.consumed = true;
                }
            }

            chState.down = false;
            if (!stateA.down || !stateB.down) {
                overlapStartTick = -1L;
            }

            if (pendingChordTap && !stateA.down && !stateB.down) {
                long overlapDuration = tick - pendingChordStartTick + 1L;
                pendingChordTap = false;
                pendingChordStartTick = -1L;
                if (isChordTapDuration(overlapDuration)) {
                    result.add(GestureType.CHORD_TAP);
                    stateA.consumed = false;
                    stateB.consumed = false;
                    return result;
                }
            }

            if (chState.consumed) {
                chState.consumed = false;
                return result;
            }

            if (!chState.longFired && isTapDuration(releasedDuration)) {
                result.add(channel == KeybindChannel.A ? GestureType.TAP_A : GestureType.TAP_B);
            }
            return result;
        }

        private List<GestureType> pollTickGestures(long tick) {
            List<GestureType> result = new ArrayList<>(2);
            checkLongGesture(stateA, GestureType.LONG_A, tick, result);
            checkLongGesture(stateB, GestureType.LONG_B, tick, result);
            return result;
        }

        private void checkLongGesture(ChannelState chState, GestureType gesture, long tick,
                                      List<GestureType> result) {
            if (chState.down && !chState.longFired && isLongDuration(chState.getDuration(tick))) {
                chState.longFired = true;
                result.add(gesture);
            }
        }

        private boolean isLongPressed(ChannelState chState, long tick) {
            return chState.getDuration(tick) >= getLongPressTicks();
        }

        private boolean isTapDuration(long duration) {
            return duration > 0L && duration <= getTapMaxTicks();
        }

        private boolean isLongDuration(long duration) {
            return duration >= getLongPressTicks();
        }

        private boolean isChordLongDuration(long duration) {
            return duration >= getChordLongTicks();
        }

        private boolean isChordTapDuration(long duration) {
            return duration > 0L && duration <= getChordTapMaxTicks();
        }

        private int getLongPressTicks() {
            return KeybindTuningConfig.getLongPressTicks();

        }
        private int getTapMaxTicks() {
            return KeybindTuningConfig.getTapMaxTicks();
        }

        private int getChordLongTicks() {
            return KeybindTuningConfig.getChordLongTicks();
        }

        private int getChordTapMaxTicks() {
            return KeybindTuningConfig.getChordTapMaxTicks();
        }
    }
}