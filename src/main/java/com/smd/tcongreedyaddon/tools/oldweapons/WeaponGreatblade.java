package com.smd.tcongreedyaddon.tools.oldweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.tools.TinkerTools;
import com.smd.tcongreedyaddon.plugin.oldweapons.oldweapons;

import javax.annotation.Nonnull;
import java.util.List;

public class WeaponGreatblade extends SwordCore {

    public WeaponGreatblade() {
        super(PartMaterialType.handle(TinkerTools.toughToolRod),
                PartMaterialType.head(TinkerTools.largeSwordBlade),
                PartMaterialType.head(TinkerTools.largeSwordBlade),
                PartMaterialType.extra(oldweapons.greatbladeCore),
                PartMaterialType.extra(TinkerTools.wideGuard));

        this.addCategory(Category.WEAPON);

        setTranslationKey("greatblade").setRegistryName("greatblade");
    }

    @Override
    public float damagePotential() {
        return 0.75F;
    }

    @Override
    public double attackSpeed() {
        return 1f;
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{1, 2};
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
    }

    @Override
    public boolean dealDamage(ItemStack stack, EntityLivingBase player, Entity entity, float damage) {

        return super.dealDamage(stack, player, entity, damage * 3/4);
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        HandleMaterialStats handle = materials.get(0).getStatsOrUnknown(MaterialTypes.HANDLE);
        HeadMaterialStats head0 = materials.get(1).getStatsOrUnknown(MaterialTypes.HEAD);
        HeadMaterialStats head1 = materials.get(2).getStatsOrUnknown(MaterialTypes.HEAD);
        ExtraMaterialStats binding = materials.get(3).getStatsOrUnknown(MaterialTypes.EXTRA);

        ToolNBT data = new ToolNBT();
        data.head(head0, head1);
        data.extra(binding);
        data.handle(handle);

        data.attack += 1;
        data.durability *= 1.2;
        return data;
    }
}
