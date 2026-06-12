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
        "Entry visibility rules used by the JEI trait overview.",
        "Rules apply to both traits and modifiers.",
        "Use an exact identifier to hide a single entry, or add '*' at the end to hide a whole prefix.",
        "Examples: extratrait* , moretcon.extratrait2* , tconstruct.ecological"
    })
    @Config.Name("Hidden Entry Rules")
    public static String[] hiddenEntryRules = {
        "extratrait*",
        "moretcon.extratrait2*"
    };

    public static boolean isEntryHidden(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        String normalizedIdentifier = identifier.toLowerCase(Locale.ROOT);
        for (String rule : hiddenEntryRules) {
            if (rule == null) {
                continue;
            }

            String normalizedRule = rule.trim().toLowerCase(Locale.ROOT);
            if (normalizedRule.isEmpty()) {
                continue;
            }

            if (normalizedRule.endsWith("*")) {
                String prefix = normalizedRule.substring(0, normalizedRule.length() - 1);
                if (!prefix.isEmpty() && normalizedIdentifier.startsWith(prefix)) {
                    return true;
                }
                continue;
            }

            if (normalizedIdentifier.equals(normalizedRule)) {
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
