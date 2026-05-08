package com.smd.tcongreedyaddon.traits.modifiers.base.something;

import com.smd.tcongreedyaddon.traits.ITraitBookProvider;
import com.smd.tcongreedyaddon.util.BookContentBuilder;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
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
        Item commandCore = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gct_ores", "command_core"));
        if (commandCore != null) {
            addItem(commandCore);
        }
    }

    @Override
    public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, boolean isCritical) {
        if (player instanceof EntityPlayer) {
            ResourceLocation targetId = EntityList.getKey(target);
            if (targetId != null && targetId.toString().contains("witherstorm")) {
                NBTTagCompound tag = TinkerUtil.getModifierTag(tool, identifier);
                int level = tag.getInteger("level");
                if (level <= 0) level = 1;

                NBTTagCompound stormyTag = TinkerUtil.getModifierTag(tool, "stormy");
                if (!stormyTag.isEmpty() && stormyTag.getInteger("level") == 1) {
                    level *= 2;
                }

                if (target.getHealth() > 100 * level) {
                    target.setHealth(target.getHealth() - (100 * level));
                } else {
                    DamageSource dmg = new EntityDamageSource("chaos", player).setDamageIsAbsolute();
                    target.attackEntityFrom(dmg, 100.0f);
                }
            }
        }
    }

    @Override
    public ContentModifier getBookContent() {
        return BookContentBuilder.create(this.getIdentifier())
                .addText("测试用强化")
                .addEffect("第一行")
                .withDefaultDemoTools()
                .build();
    }
}