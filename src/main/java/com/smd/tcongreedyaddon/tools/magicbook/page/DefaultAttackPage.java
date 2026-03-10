package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.PassiveMessageSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.StandardAttackSpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.TestAttack1Spell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.TestAttack2Spell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.TestAttack3Spell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.TestAttack4Spell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.TestAttack5Spell;

public class DefaultAttackPage extends UnifiedMagicPage {

    private static final ISpell STANDARD_ATTACK = new StandardAttackSpell();
    private static final ISpell TEST_ATTACK_1 = new TestAttack1Spell();
    private static final ISpell TEST_ATTACK_2 = new TestAttack2Spell();
    private static final ISpell TEST_ATTACK_3 = new TestAttack3Spell();
    private static final ISpell TEST_ATTACK_4 = new TestAttack4Spell();
    private static final ISpell TEST_ATTACK_5 = new TestAttack5Spell();
    private static final ISpell PASSIVE_MESSAGE = new PassiveMessageSpell();

    public DefaultAttackPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.LEFT)
                .addLeftSpell(STANDARD_ATTACK)
                .addLeftSpell(TEST_ATTACK_1)
                .addLeftSpell(TEST_ATTACK_2)
                .addLeftSpell(TEST_ATTACK_3)
                .addLeftSpell(TEST_ATTACK_4)
                .addLeftSpell(TEST_ATTACK_5)
                .addLeftSpell(PASSIVE_MESSAGE)
                .displayName("default_attack_page")
        );

        setRegistryName("default_attack_page");
        setTranslationKey("default_attack_page");
    }
}
