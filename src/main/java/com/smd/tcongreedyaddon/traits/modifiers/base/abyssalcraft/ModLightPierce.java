package com.smd.tcongreedyaddon.traits.modifiers.base.abyssalcraft;

import com.smd.tcongreedyaddon.traits.ITraitBookProvider;
import com.shinoow.abyssalcraft.api.AbyssalCraftAPI;
import com.smd.tcongreedyaddon.util.BookContentBuilder;
import slimeknights.tconstruct.library.book.content.ContentModifier;
import slimeknights.tconstruct.tools.modifiers.ModAntiMonsterType;

public class ModLightPierce extends ModAntiMonsterType implements ITraitBookProvider {

    public ModLightPierce() {
        super("light_pierce", 0xf2f2f2, 5, 24, AbyssalCraftAPI.SHADOW);
    }

    @Override
    public ContentModifier getBookContent() {
        return BookContentBuilder.create(this.getIdentifier())
                .addText("阴影克星")
                .addEffect("对阴影生物造成大量伤害。")
                .withDefaultDemoTools()
                .build();
    }
}