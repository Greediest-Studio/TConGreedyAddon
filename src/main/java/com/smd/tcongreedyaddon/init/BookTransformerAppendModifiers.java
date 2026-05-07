package com.smd.tcongreedyaddon.init;

import com.smd.tcongreedyaddon.proxy.ClientProxy;
import com.smd.tcongreedyaddon.traits.ITraitBookProvider;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.tconstruct.library.book.content.ContentListing;
import slimeknights.tconstruct.library.book.sectiontransformer.SectionTransformer;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;

public class BookTransformerAppendModifiers extends SectionTransformer {

    public BookTransformerAppendModifiers() {
        super("modifiers");
    }

    @Override
    public void transform(BookData book, SectionData section) {
        ContentListing listing = (ContentListing) section.pages.get(0).content;

        for (ModifierTrait trait : TraitRegistry.REGISTERED_TRAITS) {
            if (trait instanceof ITraitBookProvider) {
                ITraitBookProvider provider = (ITraitBookProvider) trait;
                PageData page = createPageFromProvider(section, trait, provider);
                section.pages.add(page);
                listing.addEntry(trait.getLocalizedName(), page);
            }
        }
    }

    private PageData createPageFromProvider(SectionData section, ModifierTrait trait, ITraitBookProvider provider) {
        PageData page = new PageData(true);
        page.source = ClientProxy.TCON_BOOK_REPO;
        page.parent = section;
        page.name = trait.getIdentifier();
        page.type = "modifier";
        page.content = provider.getBookContent();
        page.load();
        return page;
    }
}