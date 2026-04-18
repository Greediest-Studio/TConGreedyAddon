package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindChannel;

public interface IKeybindHoldSpell extends ISpell {

    boolean supportsHold(MagicPageItem.SlotType slotType, KeybindChannel channel);

    default int getHoldTriggerTicks(SpellContext context, KeybindChannel channel) {
        return 10;
    }

    default int getMaxHoldTicks(SpellContext context, KeybindChannel channel) {
        return -1;
    }

    default boolean onKeyHoldStart(SpellContext context, KeybindChannel channel, int heldTicks) {
        return false;
    }

    default boolean onKeyHoldTick(SpellContext context, KeybindChannel channel, int heldTicks) {
        return false;
    }

    default void onKeyHoldEnd(SpellContext context, KeybindChannel channel, int heldTicks,
                              boolean wasActive, boolean interrupted) {
    }
}
