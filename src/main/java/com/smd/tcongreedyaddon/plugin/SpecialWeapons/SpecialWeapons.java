package com.smd.tcongreedyaddon.plugin.SpecialWeapons;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.client.ClientEventHandler;
import com.smd.tcongreedyaddon.client.SpellOverlayRenderer;
import com.smd.tcongreedyaddon.client.StrandConnectionRenderer;
import com.smd.tcongreedyaddon.init.TotalTinkersRegister;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.TConGreedyTypes;
import com.smd.tcongreedyaddon.tools.magicbook.page.BeamAttackPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.DefaultAttackPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.FireballPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.FreezeRayPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.JumpBoostPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.RangePulsePage;
import com.smd.tcongreedyaddon.tools.magicbook.page.StrandGrapplePage;
import com.smd.tcongreedyaddon.tools.magicbook.page.ThermalSunderPage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.Pattern;
import slimeknights.tconstruct.library.tools.ToolPart;
import slimeknights.tconstruct.tools.TinkerTools;

import static com.smd.tcongreedyaddon.client.KeyBindings.leftpage;
import static com.smd.tcongreedyaddon.client.KeyBindings.leftSkillA;
import static com.smd.tcongreedyaddon.client.KeyBindings.leftSkillB;
import static com.smd.tcongreedyaddon.client.KeyBindings.rightpage;
import static com.smd.tcongreedyaddon.client.KeyBindings.rightSkillA;
import static com.smd.tcongreedyaddon.client.KeyBindings.rightSkillB;

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
    public static FreezeRayPage freezeRayPage;
    public static DefaultAttackPage defaultAttackPage;
    public static JumpBoostPage jumoboostpage;
    public static RangePulsePage rangePulsePage;
    public static StrandGrapplePage strandGrapplePage;
    public static ThermalSunderPage thermalSunderPage;


    @Override
    public boolean isModAvailable() {
        return Loader.isModLoaded("tconstruct") && Loader.isModLoaded("bulletapi");
    }

    @Override
    public boolean hasDetailedConfig() {
        return true;
    }


    @Override
    public void preInit() {

        TConGreedyTypes.init();

    }

    @Override
    public void preInitClient(FMLPreInitializationEvent event) {
        registerKeyBindings();
        MinecraftForge.EVENT_BUS.register(new SpellOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new StrandConnectionRenderer());
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

        freezeRayPage = new FreezeRayPage();
        event.getRegistry().register(freezeRayPage);

        jumoboostpage = new JumpBoostPage();
        event.getRegistry().register(jumoboostpage);

        rangePulsePage = new RangePulsePage();
        event.getRegistry().register(rangePulsePage);

        strandGrapplePage = new StrandGrapplePage();
        event.getRegistry().register(strandGrapplePage);

        thermalSunderPage = new ThermalSunderPage();
        event.getRegistry().register(thermalSunderPage);
    }

    public void registerKeyBindings() {
        ClientRegistry.registerKeyBinding(leftpage);
        ClientRegistry.registerKeyBinding(rightpage);
        ClientRegistry.registerKeyBinding(leftSkillA);
        ClientRegistry.registerKeyBinding(leftSkillB);
        ClientRegistry.registerKeyBinding(rightSkillA);
        ClientRegistry.registerKeyBinding(rightSkillB);
    }
}
