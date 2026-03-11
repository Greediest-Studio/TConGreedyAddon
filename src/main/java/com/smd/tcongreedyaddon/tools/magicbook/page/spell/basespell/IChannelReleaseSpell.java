package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

/**
 * Hold-to-channel spell: release to cast.
 * If released before {@link #getMinChannelTicks(SpellContext)}, it is treated as interrupted.
 */
public interface IChannelReleaseSpell extends ISpell {

    default int getMinChannelTicks(SpellContext context) {
        return 20;
    }

    default void onChannelTick(SpellContext context, int heldTicks) {
    }

    boolean onChannelRelease(SpellContext context, int heldTicks, boolean completed);

    default void onChannelInterrupted(SpellContext context, int heldTicks) {
    }
}

