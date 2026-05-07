package com.smd.tcongreedyaddon.gui;

import com.smd.tcongreedyaddon.TConGreedyAddon;
import com.smd.tcongreedyaddon.Tags;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public class TConConfigGui extends GuiConfig {

    public TConConfigGui(GuiScreen parent) {
        super(parent, getConfigElements(), Tags.MOD_ID, false, false,
                "TConGreedyAddon Configuration");
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();
        if (TConGreedyAddon.modulemanager == null) return list;
        Configuration config = TConGreedyAddon.modulemanager.getConfig();
        if (config != null) {
            list.add(new ConfigElement(config.getCategory("modules")));
            for (String category : config.getCategoryNames()) {
                if (category.equals("modules")) continue;
                list.add(new ConfigElement(config.getCategory(category)));
            }
        }
        return list;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Configuration config = TConGreedyAddon.modulemanager.getConfig();
        if (config != null && config.hasChanged()) {
            config.save();
            TConGreedyAddon.modulemanager.reloadConfig();
        }
    }
}