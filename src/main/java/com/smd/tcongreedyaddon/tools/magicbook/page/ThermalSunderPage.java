package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.ThermalSunderSpell;

public class ThermalSunderPage extends UnifiedMagicPage {

    private static final ISpell THERMAL_SUNDER = new ThermalSunderSpell();

    public ThermalSunderPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.LEFT)
                .keybindPage(true)
                .addLeftSpell(THERMAL_SUNDER)
                .displayName("thermal_sunder_page"));
        setRegistryName("thermal_sunder_page");
        setTranslationKey("thermal_sunder_page");
    }
}
