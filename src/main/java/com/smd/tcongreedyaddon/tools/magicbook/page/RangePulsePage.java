package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.AdaptiveGuardSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.RangePulseSpell;

public class RangePulsePage extends UnifiedMagicPage {

    private static final ISpell RANGE_PULSE = new RangePulseSpell();
    private static final ISpell ADAPTIVE_GUARD = new AdaptiveGuardSpell();

    public RangePulsePage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.RIGHT)
                .addRightSpell(RANGE_PULSE)
                .addRightSpell(ADAPTIVE_GUARD)
                .displayName("range_pulse_page")
        );
        setRegistryName("range_pulse_page");
        setTranslationKey("range_pulse_page");
    }
}
