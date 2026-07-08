package com.smd.tcongreedyaddon.tools.fishingrod;

import com.smd.tcongreedyaddon.util.ToolAttackHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.materials.BowMaterialStats;
import slimeknights.tconstruct.library.materials.BowStringMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.ProjectileLauncherNBT;
import slimeknights.tconstruct.library.tools.ranged.ProjectileLauncherCore;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nullable;
import java.util.List;

public class TinkerFishingRod extends ProjectileLauncherCore {

    private static final ResourceLocation CAST_PROPERTY = new ResourceLocation("cast");
    private static final float BASE_HOOK_DAMAGE = 1.0F;

    private final boolean combatDamage;

    public TinkerFishingRod(boolean combatDamage) {
        super(PartMaterialType.bow(TinkerTools.bowLimb),
              PartMaterialType.bowstring(TinkerTools.bowString),
              PartMaterialType.arrowHead(TinkerTools.arrowHead));

        this.combatDamage = combatDamage;

        addCategory(Category.PROJECTILE);
        addCategory(Category.LAUNCHER);
        addCategory(Category.WEAPON);

        addPropertyOverride(CAST_PROPERTY, new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                if (entityIn == null) {
                    return 0.0F;
                }

                boolean mainhand = entityIn.getHeldItemMainhand() == stack;
                boolean offhand = entityIn.getHeldItemOffhand() == stack;

                if (entityIn.getHeldItemMainhand().getItem() instanceof TinkerFishingRod) {
                    offhand = false;
                }

                return (mainhand || offhand)
                        && entityIn instanceof EntityPlayer
                        && ((EntityPlayer) entityIn).fishEntity != null ? 1.0F : 0.0F;
            }
        });

        setTranslationKey("fishingrod").setRegistryName("fishingrod");
    }

    @Override
    public float damagePotential() {
        return 2.0F;
    }

    @Override
    public double attackSpeed() {
        return 2.0D;
    }

    @Override
    public int[] getRepairParts() {
        return new int[] {0, 2};
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFull3D() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldRotateAroundWhenRendering() {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (ToolHelper.isBroken(stack)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        EntityFishHook activeHook = playerIn.fishEntity;
        if (activeHook != null) {
            damageHookedEntity(stack, playerIn, activeHook);
            int totalDamage = activeHook.handleHookRetraction();

            if (totalDamage > 0) {
                ToolHelper.damageTool(stack, totalDamage, playerIn);
            }

            playerIn.swingArm(handIn);
            worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ,
                              SoundEvents.ENTITY_BOBBER_RETRIEVE, SoundCategory.NEUTRAL,
                              1.0F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        } else {
            worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ,
                              SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL,
                              0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if (!worldIn.isRemote) {
                int lure = getLureSpeed(stack);
                int luck = getLuck(stack);

                EntityFishHook hook = new EntityFishHook(worldIn, playerIn);
                if (lure > 0) {
                    hook.setLureSpeed(lure);
                }
                if (luck > 0) {
                    hook.setLuck(luck);
                }

                worldIn.spawnEntity(hook);
            }

            playerIn.swingArm(handIn);
            playerIn.addStat(StatList.getObjectUseStats(this));
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ProjectileLauncherNBT buildTagData(List<Material> materials) {
        BowMaterialStats limb = materials.get(0).getStatsOrUnknown(MaterialTypes.BOW);
        BowStringMaterialStats string = materials.get(1).getStatsOrUnknown(MaterialTypes.BOWSTRING);
        HeadMaterialStats hook = materials.get(2).getStatsOrUnknown(MaterialTypes.HEAD);

        ProjectileLauncherNBT data = new ProjectileLauncherNBT();
        data.limb(limb);
        data.bowstring(string);
        data.head(hook);

        data.durability = Math.max(1, Math.round(data.durability * 1.5F));
        data.attack = Math.max(BASE_HOOK_DAMAGE, hook.attack + limb.bonusDamage);
        data.speed = 0.0F;

        return data;
    }

    private void damageHookedEntity(ItemStack stack, EntityPlayer player, EntityFishHook hook) {
        if (!combatDamage || player.world.isRemote || hook.caughtEntity == null || hook.caughtEntity instanceof EntityItem) {
            return;
        }
        Entity target = hook.caughtEntity;
        DamageSource damageSource = DamageSource.causeThrownDamage(hook, player);
        ToolAttackHelper.attackEntityRight(stack, this, player, target, 1.5F, damageSource);
    }

    private float getHookDamage(ItemStack stack) {
        ProjectileLauncherNBT data = new ProjectileLauncherNBT(TagUtil.getToolTag(stack));
        return Math.max(BASE_HOOK_DAMAGE, data.attack + data.bonusDamage);
    }

    private int getLureSpeed(ItemStack stack) {
        int enchantmentBonus = EnchantmentHelper.getFishingSpeedBonus(stack);
        ProjectileLauncherNBT data = new ProjectileLauncherNBT(TagUtil.getToolTag(stack));
        int materialBonus = data.drawSpeed > 1.0F ? 1 : 0;
        return Math.max(0, enchantmentBonus + materialBonus);
    }

    private int getLuck(ItemStack stack) {
        int enchantmentBonus = EnchantmentHelper.getFishingLuckBonus(stack);
        ProjectileLauncherNBT data = new ProjectileLauncherNBT(TagUtil.getToolTag(stack));
        int materialBonus = data.range > 1.1F ? 1 : 0;
        return Math.max(0, enchantmentBonus + materialBonus);
    }
}
