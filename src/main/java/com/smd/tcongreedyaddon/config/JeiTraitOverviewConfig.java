package com.smd.tcongreedyaddon.config;

import com.smd.tcongreedyaddon.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

@Config(modid = Tags.MOD_ID, name = "TConGreedyAddon/JeiTraitOverview")
@Config.LangKey("tcongreedyaddon.config.jei_trait_overview")
public class JeiTraitOverviewConfig {

    @Config.Comment({
        "Trait visibility rules used by the JEI trait overview.",
        "Use an exact trait id to hide a single trait, or add '*' at the end to hide a whole prefix.",
        "Examples: extratrait* , moretcon.extratrait2* , tconstruct.ecological"
    })
    @Config.Name("Hidden Trait Rules")
    public static String[] hiddenTraitRules = {
        "extratrait*",
        "moretcon.extratrait2*"
    };

    public static boolean isTraitHidden(String traitId) {
        if (traitId == null || traitId.isEmpty()) {
            return false;
        }

        String normalizedTraitId = traitId.toLowerCase(Locale.ROOT);
        for (String rule : hiddenTraitRules) {
            if (rule == null) {
                continue;
            }

            String normalizedRule = rule.trim().toLowerCase(Locale.ROOT);
            if (normalizedRule.isEmpty()) {
                continue;
            }

            if (normalizedRule.endsWith("*")) {
                String prefix = normalizedRule.substring(0, normalizedRule.length() - 1);
                if (!prefix.isEmpty() && normalizedTraitId.startsWith(prefix)) {
                    return true;
                }
                continue;
            }

            if (normalizedTraitId.equals(normalizedRule)) {
                return true;
            }
        }
        return false;
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MOD_ID)) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}