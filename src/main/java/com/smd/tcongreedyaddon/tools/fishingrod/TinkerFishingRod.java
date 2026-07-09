package com.smd.tcongreedyaddon.tools.fishingrod;

import com.smd.tcongreedyaddon.util.ToolAttackHelper;
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
import net.minecraft.util.math.MathHelper;
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
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.tools.ProjectileLauncherNBT;
import slimeknights.tconstruct.library.tools.ranged.ProjectileLauncherCore;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nullable;
import java.util.List;

public class TinkerFishingRod extends ProjectileLauncherCore {

    private static final ResourceLocation CAST_PROPERTY = new ResourceLocation("cast");
    private static final float BASE_HOOK_DAMAGE = 1.0F;
    private static final float VANILLA_HOOK_SPEED = 1.1F;
    private static final float MIN_HOOK_SPEED_MULTIPLIER = 0.25F;
    private static final float MAX_HOOK_SPEED_MULTIPLIER = 3.0F;

    private final boolean combatDamage;
    private final double hookInitialSpeedMultiplier;

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return true;
    }

    public TinkerFishingRod(boolean combatDamage, double hookInitialSpeedMultiplier) {
        super(PartMaterialType.bow(TinkerTools.bowLimb),
              PartMaterialType.bowstring(TinkerTools.bowString),
              PartMaterialType.arrowHead(TinkerTools.arrowHead));

        this.combatDamage = combatDamage;
        this.hookInitialSpeedMultiplier = hookInitialSpeedMultiplier;

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
                applyHookVelocity(stack, hook);
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
        ToolAttackHelper.attackEntityRight(stack, this, player, target,
                1.5F * getHookSpeedDamageMultiplier(hook), damageSource);
    }

    private float getHookDamage(ItemStack stack) {
        ProjectileLauncherNBT data = new ProjectileLauncherNBT(TagUtil.getToolTag(stack));
        return Math.max(BASE_HOOK_DAMAGE, data.attack + data.bonusDamage);
    }

    private void applyHookVelocity(ItemStack stack, EntityFishHook hook) {
        ProjectileLauncherNBT data = new ProjectileLauncherNBT(TagUtil.getToolTag(stack));
        double currentSpeed = Math.sqrt(hook.motionX * hook.motionX
                + hook.motionY * hook.motionY
                + hook.motionZ * hook.motionZ);
        if (currentSpeed <= 1.0E-7D) {
            return;
        }

        double drawSpeed = Math.max(0.01D, data.drawSpeed);
        double targetBlocksPerSecond = hookInitialSpeedMultiplier * 120 / drawSpeed / 3.0D;
        double targetBlocksPerTick = targetBlocksPerSecond / 20.0D;
        double velocityMultiplier = targetBlocksPerTick / currentSpeed;

        hook.motionX *= velocityMultiplier;
        hook.motionY *= velocityMultiplier;
        hook.motionZ *= velocityMultiplier;
    }

    private float getHookSpeedDamageMultiplier(EntityFishHook hook) {
        double speed = FishingRodHooks.getHitSpeed(hook);
        if (speed <= 0.0D) {
            speed = VANILLA_HOOK_SPEED;
        }

        return MathHelper.clamp((float) (speed / VANILLA_HOOK_SPEED),
                MIN_HOOK_SPEED_MULTIPLIER, MAX_HOOK_SPEED_MULTIPLIER);
    }

    private int getLureSpeed(ItemStack stack) {
        return getModifierLevel(stack, TinkerModifiers.modHaste == null ? null : TinkerModifiers.modHaste.getIdentifier());
    }

    private int getLuck(ItemStack stack) {
        return TinkerModifiers.modLuck == null ? 0 : Math.max(0, TinkerModifiers.modLuck.getLuckLevel(stack));
    }

    private int getModifierLevel(ItemStack stack, @Nullable String modifier) {
        if (modifier == null || modifier.isEmpty()) {
            return 0;
        }

        return Math.max(0, ModifierNBT.readTag(TinkerUtil.getModifierTag(stack, modifier)).level);
    }
}
