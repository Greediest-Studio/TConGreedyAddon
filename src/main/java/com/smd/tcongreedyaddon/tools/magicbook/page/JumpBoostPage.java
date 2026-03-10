package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.JumpBoostSpell;

public class JumpBoostPage extends UnifiedMagicPage {

    private static final ISpell JUMP_BOOST = new JumpBoostSpell();

    public JumpBoostPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.RIGHT)
                .addRightSpell(JUMP_BOOST)
                .displayName("jump_boost_page")
        );
        setRegistryName("jump_boost_page");
        setTranslationKey("jump_boost_page");
    }
}
