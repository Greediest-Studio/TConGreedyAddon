package com.smd.tcongreedyaddon.tools.magicbook.page.spell;

import com.smd.tcongreedyaddon.tools.magicbook.page.UnifiedMagicPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class SpellRegistry {

    private static final Map<UnifiedMagicPage, PageDefinition> PAGE_DEFINITIONS = new IdentityHashMap<>();

    private SpellRegistry() {}

    public static void registerPage(UnifiedMagicPage page, List<ISpell> leftSpells, List<ISpell> rightSpells) {
        registerPage(page, leftSpells, rightSpells, false);
    }

    public static void registerPage(UnifiedMagicPage page, List<ISpell> leftSpells, List<ISpell> rightSpells, boolean keybindPage) {
        if (page == null) {
            throw new IllegalArgumentException("page cannot be null");
        }
        PAGE_DEFINITIONS.put(page, new PageDefinition(
                page,
                unmodifiableCopy(leftSpells),
                unmodifiableCopy(rightSpells),
                keybindPage
        ));
    }

    public static PageDefinition getDefinition(UnifiedMagicPage page) {
        return PAGE_DEFINITIONS.get(page);
    }

    public static Collection<PageDefinition> getRegisteredPages() {
        return Collections.unmodifiableCollection(PAGE_DEFINITIONS.values());
    }

    public static final class PageDefinition {
        private final UnifiedMagicPage page;
        private final List<ISpell> leftSpells;
        private final List<ISpell> rightSpells;
        private final boolean keybindPage;

        private PageDefinition(UnifiedMagicPage page, List<ISpell> leftSpells, List<ISpell> rightSpells, boolean keybindPage) {
            this.page = page;
            this.leftSpells = leftSpells;
            this.rightSpells = rightSpells;
            this.keybindPage = keybindPage;
        }

        public UnifiedMagicPage getPage() {
            return page;
        }

        public List<ISpell> getLeftSpells() {
            return leftSpells;
        }

        public List<ISpell> getRightSpells() {
            return rightSpells;
        }

        public boolean isKeybindPage() {
            return keybindPage;
        }

        public List<ISpell> getAllSpells() {
            List<ISpell> all = new ArrayList<>(leftSpells.size() + rightSpells.size());
            all.addAll(leftSpells);
            all.addAll(rightSpells);
            return Collections.unmodifiableList(all);
        }
    }

    private static List<ISpell> unmodifiableCopy(List<ISpell> source) {
        if (source == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(source));
    }
}
