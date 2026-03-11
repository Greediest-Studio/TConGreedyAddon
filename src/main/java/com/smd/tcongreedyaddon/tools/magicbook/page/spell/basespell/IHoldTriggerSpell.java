package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

/**
 * Hold-to-trigger spell: executes while holding.
 * Cooldown is applied when hold ends (including early stop).
 */
public interface IHoldTriggerSpell extends ISpell {

    default int getTriggerStartTicks(SpellContext context) {
        return 0;
    }

    /**
     * Maximum total hold duration in ticks, including any charge-up time.
     * Return {@code <= 0} for no limit.
     */
    default int getMaxHoldTicks(SpellContext context) {
        return -1;
    }

    boolean onHoldTriggerTick(SpellContext context, int heldTicks);

    default void onHoldEnd(SpellContext context, int heldTicks, boolean interrupted) {
    }
}
