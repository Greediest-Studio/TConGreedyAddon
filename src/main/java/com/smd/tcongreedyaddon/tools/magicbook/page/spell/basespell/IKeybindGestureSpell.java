package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.GestureType;

public interface IKeybindGestureSpell extends ISpell {

    enum GestureResult {
        PASS(false, false),
        SUCCESS_NO_COOLDOWN(true, false),
        SUCCESS_APPLY_COOLDOWN(true, true);

        private final boolean success;
        private final boolean applyCooldown;

        GestureResult(boolean success, boolean applyCooldown) {
            this.success = success;
            this.applyCooldown = applyCooldown;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean shouldApplyCooldown() {
            return applyCooldown;
        }
    }

    boolean supportsGesture(MagicPageItem.SlotType slotType, GestureType gesture);

    GestureResult onGestureTriggered(SpellContext context, GestureType gesture, boolean onCooldown);
}
