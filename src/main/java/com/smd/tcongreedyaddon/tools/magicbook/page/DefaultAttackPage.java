package com.smd.tcongreedyaddon.tools.magicbook.page;

public class DefaultAttackPage extends SimpleLeftPage {

    public DefaultAttackPage() {
        setTranslationKey("default_attack_page").setRegistryName("default_attack_page");
        setLeftClickAction(this::performStandardAttack);
    }
}