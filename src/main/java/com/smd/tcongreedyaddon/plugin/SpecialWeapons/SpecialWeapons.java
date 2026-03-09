package com.smd.tcongreedyaddon.plugin.SpecialWeapons;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.init.TotalTinkersRegister;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.BookPageStats;
import com.smd.tcongreedyaddon.tools.magicbook.materialstats.MagicCoreStats;
import com.smd.tcongreedyaddon.tools.magicbook.TConGreedyTypes;
import com.smd.tcongreedyaddon.tools.magicbook.page.BeamAttackPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.DefaultAttackPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.FireballPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.JumpBoostPage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.Pattern;
import slimeknights.tconstruct.library.tools.ToolPart;
import slimeknights.tconstruct.tools.TinkerTools;

public class SpecialWeapons implements IModule {

    @Override
    public String getModuleName() { return "specialweapons"; }

    public static ToolPart cover;
    public static ToolPart hinge;
    public static ToolPart magiccore;
    public static ToolPart bookpage;

    public static MagicBook magicbook;

    public static FireballPage fireballPage;
    public static BeamAttackPage beamAttackPage;
    public static DefaultAttackPage defaultAttackPage;
    public static JumpBoostPage jumoboostpage;


    @Override
    public boolean isModAvailable() {
        return Loader.isModLoaded("tconstruct");
    }

    @Override
    public boolean hasDetailedConfig() {
        return true;
    }


    @Override
    public void preInit() {

        TConGreedyTypes.init();
        
        for (Material material : TinkerRegistry.getAllMaterials()) {
            if (material.getStats(BookPageStats.TYPE) == null) {
                material.addStats(getBookPageForMaterial(material));
            }
            if (material.getStats(MagicCoreStats.TYPE) == null) {
                material.addStats(getMagicCoreForMaterial(material));
            }
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void postInit() {
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {

        cover = new ToolPart(Material.VALUE_Ingot * 24);
        cover.setTranslationKey("cover").setRegistryName("cover");
        event.getRegistry().register(cover);
        TinkerRegistry.registerToolPart(cover);
        TConGreedyAddon.proxy.registerToolPartModel(cover);
        TinkerRegistry.registerStencilTableCrafting(Pattern.setTagForPart(new ItemStack(TinkerTools.pattern), cover));

        hinge = new ToolPart(Material.VALUE_Ingot * 12);
        hinge.setTranslationKey("hinge").setRegistryName("hinge");
        event.getRegistry().register(hinge);
        TinkerRegistry.registerToolPart(hinge);
        TConGreedyAddon.proxy.registerToolPartModel(hinge);
        TinkerRegistry.registerStencilTableCrafting(Pattern.setTagForPart(new ItemStack(TinkerTools.pattern), hinge));

        bookpage = new ToolPart(Material.VALUE_Ingot * 12);
        bookpage.setTranslationKey("bookpage").setRegistryName("bookpage");
        event.getRegistry().register(bookpage);
        TinkerRegistry.registerToolPart(bookpage);
        TConGreedyAddon.proxy.registerToolPartModel(bookpage);
        TinkerRegistry.registerStencilTableCrafting(Pattern.setTagForPart(new ItemStack(TinkerTools.pattern), bookpage));


        magiccore = new ToolPart(Material.VALUE_Ingot * 24);
        magiccore.setTranslationKey("magiccore").setRegistryName("magiccore");
        event.getRegistry().register(magiccore);
        TinkerRegistry.registerToolPart(magiccore);
        TConGreedyAddon.proxy.registerToolPartModel(magiccore);
        TinkerRegistry.registerStencilTableCrafting(Pattern.setTagForPart(new ItemStack(TinkerTools.pattern), magiccore));

        SpecialWeapons.magicbook = new MagicBook();
        TotalTinkersRegister.initForgeTool(SpecialWeapons.magicbook, event);

        beamAttackPage = new BeamAttackPage();
        event.getRegistry().register(beamAttackPage);

        defaultAttackPage = new DefaultAttackPage();
        event.getRegistry().register(defaultAttackPage);

        fireballPage = new FireballPage();
        event.getRegistry().register(fireballPage);

        jumoboostpage = new JumpBoostPage();
        event.getRegistry().register(jumoboostpage);
    }

    private MagicCoreStats getMagicCoreForMaterial(Material material) {
        String id = material.getIdentifier();
        switch (id) {
            case "iron":
                return new MagicCoreStats(10,10);
            case "stone":
            case "wood":
                return new MagicCoreStats(10,5);
            default:
                return new MagicCoreStats(5,5);
        }
    }

    private BookPageStats getBookPageForMaterial(Material material) {
        String id = material.getIdentifier();
        switch (id) {
            case "iron":
                return new BookPageStats(10, 7, 5);
            case "diamond":
            case "manyullyn":
                return new BookPageStats(2, 2, 5);
            default:
                return new BookPageStats(1, 1, 2);
        }
    }
}
