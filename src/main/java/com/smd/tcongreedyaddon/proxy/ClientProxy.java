package com.smd.tcongreedyaddon.proxy;

import com.smd.tcongreedyaddon.init.BookTransformerAppendModifiers;
import com.smd.tcongreedyaddon.plugin.SpecialWeapons.SpecialWeapons;
import net.minecraft.item.Item;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.book.TinkerBook;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;
import com.smd.tcongreedyaddon.plugin.oldweapons.OldWeapons;

public class ClientProxy extends CommonProxy {

    public static final BookRepository TCON_BOOK_REPO = new FileRepository("tconstruct:book");

    public void initToolGuis() {
        if (OldWeapons.greatblade != null) {
            ToolBuildGuiInfo greatbladeInfo = new ToolBuildGuiInfo(OldWeapons.greatblade);
            greatbladeInfo.addSlotPosition(33 - 10 - 14, 42 + 10 + 12); // handle
            greatbladeInfo.addSlotPosition(33 - 8 + 6, 42 - 10 + 4 - 4); // head
            greatbladeInfo.addSlotPosition(33 + 14 + 6, 42 - 10 - 2 - 4); // head 2
            greatbladeInfo.addSlotPosition(33 + 10 - 10, 42 + 10 + 6); //core
            greatbladeInfo.addSlotPosition( 33 - 10 - 12, 42); //guard
            TinkerRegistryClient.addToolBuilding(greatbladeInfo);
        }
        if (OldWeapons.battleaxe != null) {
            ToolBuildGuiInfo battleaxeInfo = new ToolBuildGuiInfo(OldWeapons.battleaxe);
            battleaxeInfo.addSlotPosition(33 - 10 - 2, 42 + 10); // handle
            battleaxeInfo.addSlotPosition(33 + 10 + 16 - 2, 42 - 10 + 16); // head 1
            battleaxeInfo.addSlotPosition(33 + 10 - 16 - 2, 42 - 10 - 16); // head 2
            battleaxeInfo.addSlotPosition(33 + 13 - 2, 42 - 13); // binding
            TinkerRegistryClient.addToolBuilding(battleaxeInfo);
        }
        if (OldWeapons.dagger != null) {
            ToolBuildGuiInfo daggerInfo = new ToolBuildGuiInfo(OldWeapons.dagger);
            daggerInfo.addSlotPosition(33 - 20 - 1, 42 + 20); // handle
            daggerInfo.addSlotPosition(33 + 20 - 5, 42 - 20 + 4); // blade
            daggerInfo.addSlotPosition(33 - 2 - 1, 42 + 2); // guard
            TinkerRegistryClient.addToolBuilding(daggerInfo);
        }
        if (OldWeapons.allinonetool != null) {
            ToolBuildGuiInfo allinonetoolInfo = new ToolBuildGuiInfo(OldWeapons.allinonetool);
            allinonetoolInfo.addSlotPosition(33 - 10 - 14, 42 + 10 + 12); // handle
            allinonetoolInfo.addSlotPosition(33 - 8 + 6, 42 - 10 + 4 - 4); // head
            allinonetoolInfo.addSlotPosition(33 + 14 + 6, 42 - 10 - 2 - 4); // head 2
            allinonetoolInfo.addSlotPosition(33 + 10 - 10, 42 + 10 + 6); //core
            allinonetoolInfo.addSlotPosition( 33 - 10 - 12, 42); //guard
            TinkerRegistryClient.addToolBuilding(allinonetoolInfo);
        }
        if (SpecialWeapons.magicbook != null) {
            ToolBuildGuiInfo magicbookInfo = new ToolBuildGuiInfo(SpecialWeapons.magicbook);
            magicbookInfo.addSlotPosition(33 - 10 - 2, 42 + 10); // handle
            magicbookInfo.addSlotPosition(33 + 10 + 16 - 2, 42 - 10 + 16); // head 1
            magicbookInfo.addSlotPosition(33 + 10 - 16 - 2, 42 - 10 - 16); // head 2
            magicbookInfo.addSlotPosition(33 + 13 - 2, 42 - 13); // binding
            TinkerRegistryClient.addToolBuilding(magicbookInfo);
        }
    }

    public void registerToolModel(ToolCore tc) {
        ModelRegisterUtil.registerToolModel(tc);
    }

    @Override
    public void registerBookData() {
        TinkerBook.INSTANCE.addTransformer(new BookTransformerAppendModifiers());
    }

    @Override
    public void preInit() {
        super.preInit();
    }

    public <T extends Item & IToolPart> void registerToolPartModel(T part) {
        ModelRegisterUtil.registerPartModel(part);
    }
}
