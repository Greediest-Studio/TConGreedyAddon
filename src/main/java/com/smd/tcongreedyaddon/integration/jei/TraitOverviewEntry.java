package com.smd.tcongreedyaddon.integration.jei;

class TraitOverviewEntry {

    enum EntryKind {
        TRAIT,
        MODIFIER
    }

    private final String identifier;
    private final String displayName;
    private final String descriptionKey;
    private final String sourceModId;
    private final EntryKind entryKind;
    private final String descriptionText;
    private final String jeiDescriptionKey;

    TraitOverviewEntry(String identifier, String displayName, String descriptionKey, String descriptionText, String jeiDescriptionKey, String sourceModId, EntryKind entryKind) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.descriptionKey = descriptionKey;
        this.descriptionText = descriptionText;
        this.jeiDescriptionKey = jeiDescriptionKey;
        this.sourceModId = sourceModId;
        this.entryKind = entryKind;
    }

    String getIdentifier() {
        return identifier;
    }

    String getDisplayName() {
        return displayName;
    }

    String getDescriptionKey() {
        return descriptionKey;
    }

    String getDescriptionText() {
        return descriptionText;
    }

    String getJeiDescriptionKey() {
        return jeiDescriptionKey;
    }

    String getSourceModId() {
        return sourceModId;
    }

    EntryKind getEntryKind() {
        return entryKind;
    }
}
