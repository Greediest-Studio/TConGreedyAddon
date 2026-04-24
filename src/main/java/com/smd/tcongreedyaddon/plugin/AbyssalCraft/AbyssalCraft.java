package com.smd.tcongreedyaddon.plugin.AbyssalCraft;

import com.shinoow.abyssalcraft.api.AbyssalCraftAPI;
import com.shinoow.abyssalcraft.api.item.ACItems;
import com.smd.tcongreedyaddon.init.TraitRegistry;
import com.smd.tcongreedyaddon.traits.abyssalcraft.TraitCoraliumPlague;
import com.smd.tcongreedyaddon.traits.abyssalcraft.TraitDreadPlague;
import com.smd.tcongreedyaddon.traits.abyssalcraft.TraitDreadPurity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.tools.modifiers.ModAntiMonsterType;
import com.smd.tcongreedyaddon.plugin.IModule;

public class AbyssalCraft implements IModule {

    @Override
    public String getModuleName() { return "AbyssalCraft"; }

    public static Material abyssalnite, coralium, dreadium;

    public static final AbstractTrait dread_plague = new TraitDreadPlague();
    public static final AbstractTrait coralium_plague = new TraitCoraliumPlague();
    public static final AbstractTrait dread_purity = new TraitDreadPurity();

    public static Modifier light_pierce;

    @Override
    public boolean isModAvailable() {
        return Loader.isModLoaded("abyssalcraft");
    }

    @Override
    public void initItems(RegistryEvent.Register<Item> event) {
    }

    @Override
    public void init() {
        abyssalnite.addCommonItems("Abyssalnite");
        abyssalnite.setRepresentativeItem(new ItemStack(ACItems.abyssalnite_ingot));
        abyssalnite.addTrait(dread_purity);

        coralium.addCommonItems("LiquifiedCoralium");
        coralium.setRepresentativeItem(new ItemStack(ACItems.refined_coralium_ingot));
        coralium.addTrait(coralium_plague);

        dreadium.addCommonItems("Dreadium");
        dreadium.setRepresentativeItem(new ItemStack(ACItems.dreadium_ingot));
        dreadium.addTrait(dread_plague);

        light_pierce = new ModAntiMonsterType("light_pierce", 0xf2f2f2, 5, 24, AbyssalCraftAPI.SHADOW);
        light_pierce.addItem(ACItems.shadow_gem);
        TraitRegistry.REGISTERED_TRAITS.add((ModifierTrait) light_pierce);
    }

    @Override
    public void preInit() {

        abyssalnite = new Material("abyssalnite", 0x4a1c89);
        coralium = new Material("refined_coralium", 0x169265);
        dreadium = new Material("dreadium", 0x880101);

        TinkerRegistry.addMaterialStats(abyssalnite,
                new HeadMaterialStats(630, 10.00f, 6.00f, 4),
                new HandleMaterialStats(0.90f, 60),
                new ExtraMaterialStats(100),
                new BowMaterialStats(0.85f, 1.1f, 1.5f));
        TinkerRegistry.integrate(abyssalnite, "Abyssalnite").toolforge().preInit();

        TinkerRegistry.addMaterialStats(coralium,
                new HeadMaterialStats(900, 12.00f, 7.00f, 5),
                new HandleMaterialStats(0.90f, 60),
                new ExtraMaterialStats(100),
                new BowMaterialStats(0.75f, 1.2f, 2.5f));
        TinkerRegistry.integrate(coralium, "LiquifiedCoralium").toolforge().preInit();

        TinkerRegistry.addMaterialStats(dreadium,
                new HeadMaterialStats(1150, 14.00f, 8.00f, 6),
                new HandleMaterialStats(0.90f, 60),
                new ExtraMaterialStats(100),
                new BowMaterialStats(0.65f, 1.3f, 3.5f));
        TinkerRegistry.integrate(dreadium, "Dreadium").toolforge().preInit();
    }
}
