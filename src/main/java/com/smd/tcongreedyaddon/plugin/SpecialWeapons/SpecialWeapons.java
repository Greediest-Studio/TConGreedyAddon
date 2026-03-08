package com.smd.tcongreedyaddon.plugin.SpecialWeapons;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.init.TotalTinkersRegister;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.RangeMaterialStats;
import com.smd.tcongreedyaddon.tools.magicbook.SlotStats;
import com.smd.tcongreedyaddon.tools.magicbook.TConGreedyTypes;
import com.smd.tcongreedyaddon.tools.magicbook.page.BeamAttackPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.DefaultAttackPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.FireballPage;
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
            if (material.getStats(RangeMaterialStats.TYPE) == null) {
                material.addStats(new RangeMaterialStats(getRangeForMaterial(material)));
            }
            if (material.getStats(SlotStats.TYPE) == null) {
                material.addStats(getSlotStatsForMaterial(material));
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
    }

    private float getRangeForMaterial(Material material) {

        switch (material.getIdentifier()) {
            case "iron": return 5.0f;
            case "diamond": return 15.0f;
            case "manyullyn": return 20.0f;
            default: return 10.0f;
        }
    }

    private SlotStats getSlotStatsForMaterial(Material material) {
        String id = material.getIdentifier();
        // 根据材料 ID 分配槽位
        switch (id) {
            case "iron":
                return new SlotStats(false, true);  // 只有右槽
            case "diamond":
            case "manyullyn":
                return new SlotStats(true, true);   // 双槽
            default:
                return new SlotStats(true, true);   // 默认双槽
        }
    }
}
