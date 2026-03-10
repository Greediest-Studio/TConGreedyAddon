package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.BeamAttackSpell;

public class BeamAttackPage extends UnifiedMagicPage {

    private static final ISpell BEAM_ATTACK = new BeamAttackSpell();

    public BeamAttackPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.RIGHT)
                .addRightSpell(BEAM_ATTACK)
                .displayName("beam_attack_page")
        );

        setRegistryName("beam_attack_page");
        setTranslationKey("beam_attack_page");
    }
}
