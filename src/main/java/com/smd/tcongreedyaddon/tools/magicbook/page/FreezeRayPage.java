package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.impl.FreezeRaySpell;

public class FreezeRayPage extends UnifiedMagicPage {

    private static final ISpell FREEZE_RAY = new FreezeRaySpell();

    public FreezeRayPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.RIGHT)
                .addRightSpell(FREEZE_RAY)
                .displayName("freeze_ray_page")
        );
        setRegistryName("freeze_ray_page");
        setTranslationKey("freeze_ray_page");
    }
}
