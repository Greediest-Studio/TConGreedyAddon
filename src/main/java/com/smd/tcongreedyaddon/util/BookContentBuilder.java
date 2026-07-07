package com.smd.tcongreedyaddon.util;

import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.tconstruct.library.book.content.ContentModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookContentBuilder {
    private String modifierName;
    private final List<TextData> textParts = new ArrayList<>();
    private final List<String> effects = new ArrayList<>();
    private final List<String> demoTools = new ArrayList<>();

    private BookContentBuilder() {}

    public static BookContentBuilder create(String modifierName) {
        BookContentBuilder builder = new BookContentBuilder();
        builder.modifierName = modifierName;
        return builder;
    }

    public BookContentBuilder addText(String text) {
        this.textParts.add(new TextData(text));
        return this;
    }

    public BookContentBuilder addStyledText(TextData textData) {
        this.textParts.add(textData);
        return this;
    }

    public BookContentBuilder addEffect(String effect) {
        this.effects.add(effect);
        return this;
    }

    public BookContentBuilder addEffects(String... effects) {
        this.effects.addAll(Arrays.asList(effects));
        return this;
    }

    public BookContentBuilder addDemoTool(String toolId) {
        this.demoTools.add(toolId);
        return this;
    }

    public BookContentBuilder addDemoTools(String... toolIds) {
        this.demoTools.addAll(Arrays.asList(toolIds));
        return this;
    }

    public BookContentBuilder withBowTools() {
        return addDemoTools(
                "tconstruct:shortbow",
                "tconstruct:longbow",
                "tconstruct:crossbow"
        );
    }

    public BookContentBuilder withDefaultDemoTools() {
        return addDemoTools(
                "tconstruct:pickaxe",
                "tconstruct:shovel",
                "tconstruct:hatchet",
                "tconstruct:mattock",
                "tconstruct:hammer",
                "tconstruct:lumberaxe",
                "tconstruct:excavator",
                "tconstruct:scythe"
        );
    }

    public ContentModifier build() {
        ContentModifier content = new ContentModifier();
        content.modifierName = this.modifierName;
        content.text = this.textParts.toArray(new TextData[0]);
        content.effects = this.effects.toArray(new String[0]);
        content.demoTool = this.demoTools.toArray(new String[0]);
        return content;
    }
}