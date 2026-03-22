package com.smd.tcongreedyaddon.integration.jei;

import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import slimeknights.tconstruct.library.utils.TagUtil;

public class TraitOverviewToolSubtypeInterpreter implements ISubtypeInterpreter {

    @Override
    public String apply(ItemStack itemStack) {
        StringBuilder builder = new StringBuilder();
        builder.append(itemStack.getItemDamage());

        NBTTagList materials = TagUtil.getBaseMaterialsTagList(itemStack);
        if (materials.getTagType() == TagUtil.TAG_TYPE_STRING) {
            builder.append(':');
            for (int i = 0; i < materials.tagCount(); i++) {
                if (i != 0) {
                    builder.append(',');
                }
                builder.append(materials.getStringTagAt(i));
            }
        }

        return builder.toString();
    }
}