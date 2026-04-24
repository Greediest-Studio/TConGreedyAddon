package com.smd.tcongreedyaddon.plugin.SpecialWeapons;

import com.smd.tcongreedyaddon.client.ClientEventHandler;
import com.smd.tcongreedyaddon.client.SpellOverlayRenderer;
import com.smd.tcongreedyaddon.client.StrandConnectionRenderer;
import com.smd.tcongreedyaddon.init.TotalTinkersRegister;
import com.smd.tcongreedyaddon.plugin.IModule;
import com.smd.tcongreedyaddon.plugin.ModuleConfig;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.TConGreedyTypes;
import com.smd.tcongreedyaddon.tools.magicbook.keybind.KeybindTuningConfig;
import com.smd.tcongreedyaddon.tools.magicbook.page.BeamAttackPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.DefaultAttackPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.FireballPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.FreezeRayPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.JumpBoostPage;
import com.smd.tcongreedyaddon.tools.magicbook.page.RangePulsePage;
import com.smd.tcongreedyaddon.tools.magicbook.page.StrandGrapplePage;
import com.smd.tcongreedyaddon.tools.magicbook.page.ThermalSunderPage;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.ToolPart;
import com.smd.tcongreedyaddon.client.KeyBindings;

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
    public void setupModuleConfig(ModuleConfig config) {
        config.addInteger("keybindLongPressTicks", KeybindTuningConfig.DEFAULT_LONG_PRESS_TICKS,
                1, 40, "Ticks to classify a key as long-press.");
        config.addInteger("keybindTapMaxTicks", KeybindTuningConfig.DEFAULT_TAP_MAX_TICKS,
                1, 20, "Max key-down ticks still counted as tap.");
        config.addInteger("keybindChordLongTicks", KeybindTuningConfig.DEFAULT_CHORD_LONG_TICKS,
                1, 40, "Overlap ticks required for CHORD_LONG.");
        config.addInteger("keybindChordTapMaxTicks", KeybindTuningConfig.DEFAULT_CHORD_TAP_MAX_TICKS,
                1, 20, "Max overlap ticks counted as CHORD_TAP.");
        config.addInteger("keybindHoldTriggerTicks", KeybindTuningConfig.DEFAULT_HOLD_TRIGGER_TICKS,
                1, 40, "Default trigger ticks for hold-spells that do not override threshold.");
        config.addInteger("keybindActionLockMinTicks", KeybindTuningConfig.DEFAULT_ACTION_LOCK_MIN_TICKS,
                0, 20, "Minimum cast action lock for keybind spells when castActionTicks > 0.");
    }

    @Override
    public void loadModuleConfig(ModuleConfig config) {
        KeybindTuningConfig.apply(
                config.getInteger("keybindLongPressTicks"),
                config.getInteger("keybindTapMaxTicks"),
                config.getInteger("keybindChordLongTicks"),
                config.getInteger("keybindChordTapMaxTicks"),
                config.getInteger("keybindHoldTriggerTicks"),
                config.getInteger("keybindActionLockMinTicks")
        );
    }


    @Override
    public void preInit() {
        TConGreedyTypes.init();
    }

    @Override
    public void preInitClient(FMLPreInitializationEvent event) {
        registerKeyBindings();
    }

    @Override
    public void initClient(FMLInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(new SpellOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new StrandConnectionRenderer());
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {

        cover = TotalTinkersRegister.registerToolPart(event, "cover", Material.VALUE_Ingot * 24);
        hinge = TotalTinkersRegister.registerToolPart(event, "hinge", Material.VALUE_Ingot * 12);
        bookpage = TotalTinkersRegister.registerToolPart(event, "bookpage", Material.VALUE_Ingot * 12);
        magiccore = TotalTinkersRegister.registerToolPart(event, "magiccore", Material.VALUE_Ingot * 24);

        magicbook = new MagicBook();
        TotalTinkersRegister.initForgeTool(magicbook, event);

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
        ClientRegistry.registerKeyBinding(KeyBindings.leftpage);
        ClientRegistry.registerKeyBinding(KeyBindings.rightpage);
        ClientRegistry.registerKeyBinding(KeyBindings.leftSkillA);
        ClientRegistry.registerKeyBinding(KeyBindings.leftSkillB);
        ClientRegistry.registerKeyBinding(KeyBindings.rightSkillA);
        ClientRegistry.registerKeyBinding(KeyBindings.rightSkillB);
    }
}
