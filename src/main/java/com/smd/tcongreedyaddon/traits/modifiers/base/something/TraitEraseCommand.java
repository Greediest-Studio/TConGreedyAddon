package com.smd.tcongreedyaddon.traits.modifiers.base.something;

import com.smd.tcongreedyaddon.traits.ITraitBookProvider;
import com.smd.tcongreedyaddon.util.BookContentBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.item.Item;
import slimeknights.tconstruct.library.book.content.ContentModifier;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

public class TraitEraseCommand extends ModifierTrait implements ITraitBookProvider {

    public TraitEraseCommand() {
        super("erase_command", 0xf64700, 5, 1);
    }

    public void initItem() {
        Item commandCore = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gct_additions", "command_core"));
        if (commandCore != null) {
            addItem(commandCore);
        }
    }

    public static boolean isCommandBlockTool(ItemStack tool) {
        return !tool.isEmpty() && tool.hasTagCompound() && TinkerUtil.hasTrait(tool.getTagCompound(), "erase_command");
    }

    @Override
    public ContentModifier getBookContent() {
        return BookContentBuilder.create(this.getIdentifier())
                .addText("权限核心使工具具备挑战资格，可参与凋灵风暴战斗。")
                .addEffect("对凋零风暴造成有效伤害")
                .withDefaultDemoTools()
                .build();
    }
}