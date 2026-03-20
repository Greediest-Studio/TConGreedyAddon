package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.StrandGrappleSpell;

public class StrandGrapplePage extends UnifiedMagicPage {

    private static final ISpell STRAND_GRAPPLE = new StrandGrappleSpell();

    public StrandGrapplePage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.RIGHT)
                .addRightSpell(STRAND_GRAPPLE)
                .displayName("strand_grapple_page"));
        setRegistryName("strand_grapple_page");
        setTranslationKey("strand_grapple_page");
    }
}
