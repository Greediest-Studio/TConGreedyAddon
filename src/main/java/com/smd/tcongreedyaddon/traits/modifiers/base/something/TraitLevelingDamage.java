package com.smd.tcongreedyaddon.traits.modifiers.base.something;

import com.smd.tcongreedyaddon.traits.ITraitBookProvider;
import com.smd.tcongreedyaddon.util.BookContentBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import slimeknights.tconstruct.library.book.content.ContentModifier;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.TagUtil;
import java.util.List;
import java.util.ArrayList;

public class TraitLevelingDamage extends ModifierTrait implements ITraitBookProvider {

    public TraitLevelingDamage() {
        super("levelingdamage", 0x7e57c2, 3, 0);
    }

    public void initItem() {
        Item commandCore = ForgeRegistries.ITEMS.getValue(new ResourceLocation("additions", "plate_of_honor"));
        if (commandCore != null) {
            addItem(commandCore);
        }
    }

    private int getToolLevel(ItemStack tool) {
        NBTTagList modifiers = TagUtil.getModifiersTagList(tool);
        for (int i = 0; i < modifiers.tagCount(); i++) {
            NBTTagCompound tag = modifiers.getCompoundTagAt(i);
            if (tag.getString("identifier").equals("toolleveling")) {
                return tag.getInteger("level");
            }
        }
        return 0;
    }

    private float getMultiplier(int toolLevel, int modifierLevel) {
        float multiplier = 1.0f;
        if (toolLevel > 0) {
            multiplier += 0.05f * toolLevel;
            if (multiplier > 2.0f) {
                multiplier = 2.0f + (multiplier - 2.0f) / 4.0f;
            }
        }

        return (multiplier - 1.0f) * ((float) modifierLevel / 3.0f) + 1.0f;
    }

    @Override
    public List<String> getExtraInfo(ItemStack tool, NBTTagCompound modifierTag) {
        List<String> info = new ArrayList<>(super.getExtraInfo(tool, modifierTag));
        int level = modifierTag.getInteger("level");
        float multiplier = getMultiplier(getToolLevel(tool), level);
        int percentage = Math.round((multiplier - 1.0f) * 100.0f);

        info.add(I18n.format("tooltip.damage_increase", percentage));
        return info;
    }

    @Override
    public float damage(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, float newDamage, boolean isCritical) {
        NBTTagCompound modifierTag = TinkerUtil.getModifierTag(tool, identifier);
        float multiplier = getMultiplier(getToolLevel(tool), modifierTag.getInteger("level"));
        return newDamage * multiplier;
    }

    @Override
    public ContentModifier getBookContent() {
        return BookContentBuilder.create(this.getIdentifier())
                .addText("测试用强化")
                .addEffect("第一行")
                .addEffect("第二行")
                .withDefaultDemoTools()
                .build();
    }
}