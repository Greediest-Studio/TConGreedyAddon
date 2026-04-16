package com.smd.tcongreedyaddon.tools.magicbook.keybind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KeybindGestureState {
    private static final int LONG_PRESS_TICKS = 2;
    private static final int TAP_MAX_TICKS = 6;

    private final SideState left = new SideState();
    private final SideState right = new SideState();
    private int lastSequence = -1;

    public List<GestureType> onInput(int sequence, KeybindSide side, KeybindChannel channel, KeybindAction action, long serverTick) {
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

    private static final class SideState {
        private boolean downA;
        private boolean downB;
        private boolean consumedA;
        private boolean consumedB;
        private long pressTickA = -1L;
        private long pressTickB = -1L;
        private long overlapStartTick = -1L;
        private boolean pendingChordTap;
        private long pendingChordStartTick = -1L;

        private List<GestureType> onInput(KeybindChannel channel, KeybindAction action, long tick) {
            if (action == KeybindAction.PRESS) {
                return onPress(channel, tick);
            }
            return onRelease(channel, tick);
        }

        private List<GestureType> onPress(KeybindChannel channel, long tick) {
            if (isDown(channel)) {
                return Collections.emptyList();
            }
            List<GestureType> result = new ArrayList<>(1);
            setDown(channel, true);
            setPressTick(channel, tick);
            setConsumed(channel, false);
            result.add(channel == KeybindChannel.A ? GestureType.PRESS_A : GestureType.PRESS_B);
            if (downA && downB) {
                overlapStartTick = tick;
                pendingChordTap = false;
                pendingChordStartTick = -1L;
            }
            return result;
        }

        private List<GestureType> onRelease(KeybindChannel channel, long tick) {
            if (!isDown(channel)) {
                return Collections.emptyList();
            }

            List<GestureType> result = new ArrayList<>(1);
            KeybindChannel other = channel == KeybindChannel.A ? KeybindChannel.B : KeybindChannel.A;
            boolean otherDown = isDown(other);
            long releasedDuration = getDuration(channel, tick);
            result.add(channel == KeybindChannel.A ? GestureType.RELEASE_A : GestureType.RELEASE_B);

            if (otherDown && isLongPressed(other, tick) && isTapDuration(releasedDuration)) {
                result.add(channel == KeybindChannel.B ? GestureType.HOLD_A_TAP_B : GestureType.HOLD_B_TAP_A);
                setConsumed(channel, true);
                setConsumed(other, true);
                pendingChordTap = false;
            } else if (otherDown && overlapStartTick >= 0L && !isConsumed(channel) && !isConsumed(other)) {
                long overlapDuration = tick - overlapStartTick + 1L;
                if (overlapDuration >= LONG_PRESS_TICKS) {
                    result.add(GestureType.CHORD_LONG);
                    setConsumed(channel, true);
                    setConsumed(other, true);
                    pendingChordTap = false;
                } else if (isTapDuration(releasedDuration) && isTapDuration(getDuration(other, tick))) {
                    pendingChordTap = true;
                    pendingChordStartTick = overlapStartTick;
                    setConsumed(channel, true);
                }
            }

            setDown(channel, false);
            if (!downA || !downB) {
                overlapStartTick = -1L;
            }

            if (pendingChordTap && !downA && !downB) {
                long overlapDuration = tick - pendingChordStartTick + 1L;
                pendingChordTap = false;
                pendingChordStartTick = -1L;
                if (overlapDuration < LONG_PRESS_TICKS) {
                    result.add(GestureType.CHORD_TAP);
                    setConsumed(KeybindChannel.A, false);
                    setConsumed(KeybindChannel.B, false);
                    return result;
                }
            }

            if (isConsumed(channel)) {
                setConsumed(channel, false);
                return result;
            }

            if (isLongDuration(releasedDuration)) {
                result.add(channel == KeybindChannel.A ? GestureType.LONG_A : GestureType.LONG_B);
            } else {
                result.add(channel == KeybindChannel.A ? GestureType.TAP_A : GestureType.TAP_B);
            }
            return result;
        }

        private boolean isDown(KeybindChannel channel) {
            return channel == KeybindChannel.A ? downA : downB;
        }

        private void setDown(KeybindChannel channel, boolean down) {
            if (channel == KeybindChannel.A) {
                downA = down;
            } else {
                downB = down;
            }
        }

        private boolean isConsumed(KeybindChannel channel) {
            return channel == KeybindChannel.A ? consumedA : consumedB;
        }

        private void setConsumed(KeybindChannel channel, boolean consumed) {
            if (channel == KeybindChannel.A) {
                consumedA = consumed;
            } else {
                consumedB = consumed;
            }
        }

        private long getDuration(KeybindChannel channel, long tick) {
            long start = channel == KeybindChannel.A ? pressTickA : pressTickB;
            if (start < 0L) {
                return 0L;
            }
            return tick - start + 1L;
        }

        private void setPressTick(KeybindChannel channel, long tick) {
            if (channel == KeybindChannel.A) {
                pressTickA = tick;
            } else {
                pressTickB = tick;
            }
        }

        private boolean isLongPressed(KeybindChannel channel, long tick) {
            return getDuration(channel, tick) >= LONG_PRESS_TICKS;
        }

        private boolean isTapDuration(long duration) {
            return duration > 0L && duration <= TAP_MAX_TICKS;
        }

        private boolean isLongDuration(long duration) {
            return duration >= LONG_PRESS_TICKS;
        }
    }
}
