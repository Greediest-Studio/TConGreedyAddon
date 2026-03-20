package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

public interface IKeybindSkillSpell extends ISpell {

    enum KeyAction {
        PRESS,
        RELEASE
    }

    enum KeybindResult {
        PASS(false, false),
        SUCCESS_NO_COOLDOWN(true, false),
        SUCCESS_APPLY_COOLDOWN(true, true);

        private final boolean success;
        private final boolean applyCooldown;

        KeybindResult(boolean success, boolean applyCooldown) {
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

    default String getKeyBindingId() {
        return "q";
    }

    KeybindResult onKeybindTriggered(SpellContext context, KeyAction action, boolean onCooldown);
}
