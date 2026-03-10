package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.LargeFireballSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.SmallFireballSpell;

public class FireballPage extends UnifiedMagicPage {

    private static final ISpell SMALL_FIREBALL = new SmallFireballSpell();
    private static final ISpell LARGE_FIREBALL = new LargeFireballSpell();

    public FireballPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.RIGHT)
                .addRightSpell(SMALL_FIREBALL)
                .addRightSpell(LARGE_FIREBALL)
                .displayName("fireball_page")
        );
        setRegistryName("fireball_page");
        setTranslationKey("fireball_page");
    }
}
