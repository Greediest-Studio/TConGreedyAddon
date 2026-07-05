package com.smd.tcongreedyaddon.proxy;

import com.smd.tcongreedyaddon.init.BookTransformerAppendModifiers;
import net.minecraft.item.Item;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import slimeknights.tconstruct.library.book.TinkerBook;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;

public class ClientProxy extends CommonProxy {

    public static final BookRepository TCON_BOOK_REPO = new FileRepository("tconstruct:book");

    @Override
    public void registerToolModel(ToolCore tc) {
        ModelRegisterUtil.registerToolModel(tc);
    }

    @Override
    public void registerBookData() {
        TinkerBook.INSTANCE.addTransformer(new BookTransformerAppendModifiers());
    }

    @Override
    public <T extends Item & IToolPart> void registerToolPartModel(T part) {
        ModelRegisterUtil.registerPartModel(part);
    }
}
