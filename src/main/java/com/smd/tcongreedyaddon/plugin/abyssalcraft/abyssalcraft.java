package com.smd.tcongreedyaddon.plugin.abyssalcraft;

import com.shinoow.abyssalcraft.api.item.ACItems;
import com.smd.tcongreedyaddon.init.TraitRegistry;
import com.smd.tcongreedyaddon.traits.abyssalcraft.TraitCoraliumPlague;
import com.smd.tcongreedyaddon.traits.abyssalcraft.TraitDreadPlague;
import com.smd.tcongreedyaddon.traits.abyssalcraft.TraitDreadPurity;
import com.smd.tcongreedyaddon.traits.modifiers.base.abyssalcraft.ModLightPierce;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import com.smd.tcongreedyaddon.plugin.IModule;

public class abyssalcraft implements IModule {

    @Override
    public String getModuleName() { return "abyssalcraft"; }

    public static Material abyssalnite, coralium, dreadium;

    public static final AbstractTrait dread_plague = new TraitDreadPlague();
    public static final AbstractTrait coralium_plague = new TraitCoraliumPlague();
    public static final AbstractTrait dread_purity = new TraitDreadPurity();

    public static ModLightPierce lightpierce;

    @Override
    public boolean isModAvailable() {
        return Loader.isModLoaded("abyssalcraft");
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

        lightpierce = new ModLightPierce();
        TraitRegistry.REGISTERED_TRAITS.add(lightpierce);
        lightpierce.addItem(ACItems.shadow_gem);
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
