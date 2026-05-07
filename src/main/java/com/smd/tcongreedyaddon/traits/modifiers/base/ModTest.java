package com.smd.tcongreedyaddon.traits.modifiers.base;

import com.smd.tcongreedyaddon.util.BookContentBuilder;
import slimeknights.tconstruct.library.book.content.ContentModifier;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import com.smd.tcongreedyaddon.traits.ITraitBookProvider;

public class ModTest extends ModifierTrait implements ITraitBookProvider {

    public ModTest() {
        super("test", 0xFFD700);
    }

    @Override
    public ContentModifier getBookContent() {
        return BookContentBuilder.create(this.getIdentifier())
                .addText("测试用强化")
                .addEffect("第一行")
                .addEffect("第二行")
                .withDefaultDemoTools()
                .build();
    }
}