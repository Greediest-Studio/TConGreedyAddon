package com.smd.tcongreedyaddon.tools.solidarytinker;

import com.smd.tcongreedyaddon.init.SoundsHandler;
import com.smd.tcongreedyaddon.plugin.solidarytinker.solidarytinker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SoulGe extends SwordCore {

    public static final String ENTITY_TAG_TARGETED = "SoulGeTargeted";
    public static final String ENTITY_TAG_READY_TO_DIE = "SoulGeReadyToDie";
    public static final String ENTITY_TAG_KILLER = "SoulGeKiller";
    public static final String ENTITY_TAG_OWNER = "SoulGeOwner";
    public static final String ENTITY_TAG_EXECUTION_STAGE = "SoulGeExecutionStage";
    public static final String ENTITY_TAG_EXECUTION_TIMER = "SoulGeExecutionTimer";
    public static final String ENTITY_TAG_EXECUTION_START_Y = "SoulGeExecutionStartY";

    private static final int MODE_STACK_GAIN_SCALE = 3;
    private static final int MODE_LINK_TIMEOUT = 16;
    private static final int TEMPERATURE_STEP_INTERVAL = 5;
    private static final int TEMPERATURE_COOLDOWN_RESET = 12;
    private static final int HIGH_TEMPERATURE_THRESHOLD = 24;
    private static final int MID_TEMPERATURE_THRESHOLD = 8;
    private static final int MULTI_TARGET_LIMIT = 11;
    private static final UUID SINGLE_MODE_DAMAGE_BOOST_UUID = UUID.fromString("8d078e0d-68bb-4c21-a330-ea7cccd0cfbb");

    public SoulGe() {
        super(
                PartMaterialType.head(TinkerTools.knifeBlade),
                SoulGeTypes.soulgeHeart(solidarytinker.soulgeHeart),
                PartMaterialType.head(TinkerTools.knifeBlade),
                PartMaterialType.head(TinkerTools.broadAxeHead),
                PartMaterialType.handle(TinkerTools.toughToolRod)
        );
        addCategory(Category.WEAPON);
        setTranslationKey("soulge").setRegistryName("soulge");
    }

    @Override
    public float damagePotential() {
        return 1.2f;
    }

    @Override
    public double attackSpeed() {
        return 0.8d;
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{0, 2, 3};
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote) {
                boolean singleMode = !SoulGeState.isSingleMode(stack);
                SoulGeState.setSingleMode(stack, singleMode);
                SoulGeRuntimeStateHelper.clear(player, hand);
                player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                        singleMode ? "tooltip.soulge.mode.single" : "tooltip.soulge.mode.multi"), true);
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        }
        if (ToolHelper.isBroken(stack)) {
            return ActionResult.newResult(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        if (!(living instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) living;
        World world = player.world;
        SoulGeToolNBT toolData = SoulGeToolNBT.from(stack);
        int detectionRange = Math.max(1, Math.round(toolData.detectionRange));
        int attackFrequency = Math.max(1, toolData.attackFrequency);
        int exertTimes = Math.max(1, toolData.exertTimes);
        EnumHand hand = player.getActiveHand();
        if (hand == null) {
            hand = EnumHand.MAIN_HAND;
        }
        EntityLivingBase target = getPointedLivingEntity(player, detectionRange);
        if (target instanceof EntityPlayer) {
            return;
        }

        boolean singleMode = SoulGeState.isSingleMode(stack);
        if (world.isRemote) {
            if (singleMode) {
                SoulGeClientState.updateSingleMode(player, hand, stack, target, true);
            } else {
                SoulGeClientState.updateMultiMode(player, hand, stack, target, collectVisualTargets(player, detectionRange, target), false);
            }
            return;
        }

        if (singleMode) {
            if (target != null && target.isEntityAlive()) {
                handleSingleMode(stack, player, hand, target, attackFrequency, toolData.killThreshold);
            }
        } else {
            markMultipleTargets(player, target, detectionRange, exertTimes);
            if (player.ticksExisted % attackFrequency == 0) {
                attackMultipleTargets(stack, player, detectionRange, toolData.killThreshold);
                restoreActiveHandIfNeeded(stack, player, hand);
            }
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            EnumHand hand = player.getActiveHand() == null ? EnumHand.MAIN_HAND : player.getActiveHand();
            SoulGeRuntimeStateHelper.clear(player, hand);
            if (world.isRemote) {
                SoulGeClientState.clear(player, hand);
            }
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (!(entity instanceof EntityPlayer) || entity.ticksExisted % TEMPERATURE_STEP_INTERVAL != 0) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        EnumHand hand = resolveHoldingHand(player, stack);
        if (hand == null) {
            return;
        }

        if (world.isRemote) {
            SoulGeClientState.tickPassive(player, hand, stack);
            return;
        }

        int cooldownTick = SoulGeRuntimeStateHelper.getTemperatureCooldownTick(player, hand, stack);
        if (cooldownTick > 0) {
            SoulGeRuntimeStateHelper.setTemperatureCooldownTick(player, hand, stack, cooldownTick - 1);
        } else {
            SoulGeRuntimeStateHelper.setTemperatureRiseTick(player, hand, stack, 0);
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    public boolean isEffective(IBlockState state) {
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!slotChanged && !oldStack.isEmpty() && !newStack.isEmpty() && oldStack.getItem() == newStack.getItem()) {
            return false;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        if (!oldStack.isEmpty() && !newStack.isEmpty() && oldStack.getItem() == newStack.getItem()) {
            return false;
        }
        return super.shouldCauseBlockBreakReset(oldStack, newStack);
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        HeadMaterialStats headOne = materials.get(0).getStatsOrUnknown("head");
        SoulGeHeartStats heartStats = materials.get(1).getStatsOrUnknown(SoulGeTypes.SOULGE_HEART);
        HeadMaterialStats headTwo = materials.get(2).getStatsOrUnknown("head");
        HeadMaterialStats headThree = materials.get(3).getStatsOrUnknown("head");
        HandleMaterialStats handle = materials.get(4).getStatsOrUnknown("handle");

        SoulGeToolNBT data = new SoulGeToolNBT();
        data.head(headOne, headTwo, headThree);
        data.handle(handle);
        data.attack += 1.2f;
        data.attackSpeedMultiplier = 1.4f;
        data.modifiers = 3;
        data.durability = Math.round(data.durability * 1.5f);
        data.detectionRange = heartStats.detectionRange;
        data.exertTimes = heartStats.exertTimes;
        data.attackFrequency = heartStats.attackFrequency;
        data.killThreshold = heartStats.killThreshold;
        return data;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        SoulGeToolNBT toolData = SoulGeToolNBT.from(stack);
        tooltip.add(TextFormatting.GOLD + I18n.format("tooltip.soulge.mode",
                I18n.format(SoulGeState.isSingleMode(stack) ? "tooltip.soulge.mode.single" : "tooltip.soulge.mode.multi")));
        tooltip.add(TextFormatting.AQUA + I18n.format("stat.soulge.detection_range.name", Math.round(toolData.detectionRange)));
        tooltip.add(TextFormatting.AQUA + I18n.format("stat.soulge.exert_times.name", toolData.exertTimes));
        tooltip.add(TextFormatting.AQUA + I18n.format("stat.soulge.attack_frequency.name", toolData.attackFrequency));
        tooltip.add(TextFormatting.AQUA + I18n.format("stat.soulge.kill_threshold.name", Math.round(toolData.killThreshold * 100.0f)));
        tooltip.add(TextFormatting.GRAY + I18n.format("item.soulge.desc"));
    }

    private void handleSingleMode(ItemStack stack, EntityPlayer player, EnumHand hand,
                                  EntityLivingBase target, int attackFrequency, float killThreshold) {
        if (player.ticksExisted % TEMPERATURE_STEP_INTERVAL == 0) {
            int temperature = SoulGeRuntimeStateHelper.getTemperatureRiseTick(player, hand, stack) + 1;
            SoulGeRuntimeStateHelper.setTemperatureRiseTick(player, hand, stack, temperature);
            SoulGeRuntimeStateHelper.setTemperatureCooldownTick(player, hand, stack, TEMPERATURE_COOLDOWN_RESET);
            if (temperature == MID_TEMPERATURE_THRESHOLD || temperature == HIGH_TEMPERATURE_THRESHOLD) {
                player.world.playSound(null, player.posX, player.posY, player.posZ,
                        SoundsHandler.SOULGE_BEAM_UP != null ? SoundsHandler.SOULGE_BEAM_UP : net.minecraft.init.SoundEvents.BLOCK_FIRE_AMBIENT,
                        SoundCategory.PLAYERS, 1.6f, temperature >= HIGH_TEMPERATURE_THRESHOLD ? 1.35f : 1.0f);
            }
        }

        if (player.ticksExisted % attackFrequency == 0) {
            if (target instanceof EntityMob && target.getHealth() < target.getMaxHealth() * killThreshold) {
                NBTTagCompound data = target.getEntityData();
                if (!isExecuting(data)) {
                    beginExecution(player, target, data);
                    restoreActiveHandIfNeeded(stack, player, hand);
                    return;
                }
            }
            int temperature = SoulGeRuntimeStateHelper.getTemperatureRiseTick(player, hand, stack);
            float multiplier = getSingleModeDamageMultiplier(temperature);
            attackWithTemporaryDamageBoost(stack, player, hand, target, multiplier);
            if (target instanceof EntityMob && target.getHealth() < target.getMaxHealth() * killThreshold) {
                NBTTagCompound data = target.getEntityData();
                if (target.isEntityAlive() && !isExecuting(data)) {
                    beginExecution(player, target, data);
                }
            }
        }
    }

    private void attackWithTemporaryDamageBoost(ItemStack stack, EntityPlayer player, EnumHand hand,
                                                EntityLivingBase target, float multiplier) {
        IAttributeInstance attackDamage = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        AttributeModifier modifier = null;
        if (attackDamage != null && multiplier > 1.0f) {
            double baseDamage = attackDamage.getAttributeValue();
            modifier = new AttributeModifier(SINGLE_MODE_DAMAGE_BOOST_UUID,
                    "SoulGe single mode temperature damage", baseDamage * (multiplier - 1.0f), 0).setSaved(false);
            attackDamage.removeModifier(SINGLE_MODE_DAMAGE_BOOST_UUID);
            attackDamage.applyModifier(modifier);
        }
        try {
            ToolHelper.attackEntity(stack, this, player, target);
        } finally {
            if (attackDamage != null && modifier != null) {
                attackDamage.removeModifier(SINGLE_MODE_DAMAGE_BOOST_UUID);
            }
            restoreActiveHandIfNeeded(stack, player, hand);
        }
    }

    private float getSingleModeDamageMultiplier(int temperature) {
        if (temperature >= HIGH_TEMPERATURE_THRESHOLD) {
            return 1.5f;
        }
        if (temperature >= MID_TEMPERATURE_THRESHOLD) {
            return 1.12f;
        }
        return 1.0f;
    }

    private void attackMultipleTargets(ItemStack stack, EntityPlayer player, int detectionRange, float killThreshold) {
        List<EntityMob> mobs = collectOwnedMarkedTargets(player, detectionRange);

        int attacked = 0;
        for (EntityMob mob : mobs) {
            if (attacked >= MULTI_TARGET_LIMIT) {
                break;
            }
            NBTTagCompound data = mob.getEntityData();

            if (isExecuting(data)) {
                continue;
            }

            int targeted = data.getInteger(ENTITY_TAG_TARGETED);
            if (targeted <= 0 || !mob.isEntityAlive()) {
                clearTargetOwnership(data);
                continue;
            }

            if (mob.getHealth() < mob.getMaxHealth() * killThreshold) {
                beginExecution(player, mob, data);
                attacked++;
                continue;
            }

            ToolHelper.attackEntity(stack, this, player, mob);
            data.setInteger(ENTITY_TAG_TARGETED, targeted - 1);

            if (mob.getHealth() < mob.getMaxHealth() * killThreshold && !isExecuting(data)) {
                beginExecution(player, mob, data);
            }
            if (isExecuting(data)) {
                attacked++;
                continue;
            }
            if (!mob.isEntityAlive() || data.getInteger(ENTITY_TAG_TARGETED) <= 0) {
                clearTargetOwnership(data);
            }
            attacked++;
        }
    }

    private void markMultipleTargets(EntityPlayer owner, @Nullable EntityLivingBase pointed, int detectionRange, int exertTimes) {
        List<EntityMob> candidates = collectLinkableMobs(owner, detectionRange, pointed);
        int marked = 0;
        for (EntityMob mob : candidates) {
            if (marked >= MULTI_TARGET_LIMIT) {
                break;
            }
            markTarget(owner, mob, exertTimes);
            marked++;
        }
    }

    private void markTarget(EntityPlayer owner, EntityLivingBase target, int exertTimes) {
        NBTTagCompound data = target.getEntityData();
        if (isExecuting(data)) {
            return;
        }
        data.setString(ENTITY_TAG_OWNER, owner.getUniqueID().toString());
        data.setInteger(ENTITY_TAG_EXECUTION_TIMER, MODE_LINK_TIMEOUT);
        int current = data.getInteger(ENTITY_TAG_TARGETED);
        int max = Math.max(1, exertTimes * MODE_STACK_GAIN_SCALE);
        int next = Math.min(max, current + Math.max(1, exertTimes));
        data.setInteger(ENTITY_TAG_TARGETED, next);
    }

    private void restoreActiveHandIfNeeded(ItemStack stack, EntityPlayer player, EnumHand hand) {
        if (!player.world.isRemote
                && !ToolHelper.isBroken(stack)
                && player.getHeldItem(hand) == stack
                && !player.isHandActive()) {
            player.setActiveHand(hand);
        }
    }

    @Nullable
    private EntityLivingBase getPointedLivingEntity(EntityPlayer player, int distance) {
        Vec3d eye = player.getPositionEyes(1.0f);
        Vec3d look = player.getLook(1.0f);
        Vec3d reach = eye.add(look.scale(distance));

        EntityLivingBase pointed = null;
        double closest = distance;

        RayTraceResult blockHit = player.world.rayTraceBlocks(eye, reach, false, true, false);
        if (blockHit != null) {
            closest = eye.distanceTo(blockHit.hitVec);
        }

        List<EntityLivingBase> candidates = player.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                player.getEntityBoundingBox().grow(distance).expand(look.x * distance, look.y * distance, look.z * distance));

        for (EntityLivingBase candidate : candidates) {
            if (candidate == player || !candidate.canBeCollidedWith()) {
                continue;
            }
            AxisAlignedBB box = candidate.getEntityBoundingBox().grow(candidate.getCollisionBorderSize() + 0.5d);
            RayTraceResult intercept = box.calculateIntercept(eye, reach);
            if (box.contains(eye)) {
                if (closest >= 0.0d) {
                    pointed = candidate;
                    closest = 0.0d;
                }
            } else if (intercept != null) {
                double hitDistance = eye.distanceTo(intercept.hitVec);
                if (hitDistance < closest || closest == 0.0d) {
                    pointed = candidate;
                    closest = hitDistance;
                }
            }
        }

        return pointed instanceof EntityLivingBase ? pointed : null;
    }

    @Nullable
    private EnumHand resolveHoldingHand(EntityPlayer player, ItemStack stack) {
        if (player.getHeldItemMainhand() == stack) {
            return EnumHand.MAIN_HAND;
        }
        if (player.getHeldItemOffhand() == stack) {
            return EnumHand.OFF_HAND;
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private List<EntityLivingBase> collectVisualTargets(EntityPlayer player, int detectionRange, @Nullable EntityLivingBase pointed) {
        return new ArrayList<>(collectLinkableMobs(player, detectionRange, pointed));
    }

    private List<EntityMob> collectOwnedMarkedTargets(EntityPlayer player, int detectionRange) {
        AxisAlignedBB area = buildDetectionBox(player, detectionRange);
        List<EntityMob> mobs = player.world.getEntitiesWithinAABB(EntityMob.class, area);
        mobs.removeIf(mob -> !isOwnedByPlayer(mob.getEntityData(), player));
        mobs.sort(Comparator.comparingDouble(player::getDistanceSq));
        return mobs;
    }

    private List<EntityMob> collectLinkableMobs(EntityPlayer player, int detectionRange, @Nullable EntityLivingBase pointed) {
        AxisAlignedBB area = buildDetectionBox(player, detectionRange);
        List<EntityMob> mobs = player.world.getEntitiesWithinAABB(EntityMob.class, area);
        double maxDistanceSq = detectionRange * detectionRange;
        UUID pointedId = pointed == null ? null : pointed.getUniqueID();
        mobs.removeIf(mob -> !mob.isEntityAlive()
                || mob.getDistanceSq(player) > maxDistanceSq
                || isExecuting(mob.getEntityData()));
        mobs.sort((left, right) -> {
            boolean leftPointed = pointedId != null && pointedId.equals(left.getUniqueID());
            boolean rightPointed = pointedId != null && pointedId.equals(right.getUniqueID());
            if (leftPointed != rightPointed) {
                return leftPointed ? -1 : 1;
            }
            return Double.compare(player.getDistanceSq(left), player.getDistanceSq(right));
        });
        return mobs;
    }

    private AxisAlignedBB buildDetectionBox(EntityPlayer player, int detectionRange) {
        return new AxisAlignedBB(
                player.posX - detectionRange, player.posY - detectionRange, player.posZ - detectionRange,
                player.posX + detectionRange, player.posY + detectionRange, player.posZ + detectionRange);
    }

    private boolean isOwnedByPlayer(NBTTagCompound data, EntityPlayer player) {
        return data.hasKey(ENTITY_TAG_OWNER) && player.getUniqueID().toString().equals(data.getString(ENTITY_TAG_OWNER));
    }

    private boolean isExecuting(NBTTagCompound data) {
        return data.hasKey(ENTITY_TAG_READY_TO_DIE) || data.getInteger(ENTITY_TAG_EXECUTION_STAGE) > 0;
    }

    private void beginExecution(EntityPlayer player, EntityLivingBase living, NBTTagCompound data) {

        if (data.getInteger(ENTITY_TAG_EXECUTION_STAGE) > 0) {
            return;
        }

        data.setString(ENTITY_TAG_KILLER, player.getUniqueID().toString());
        data.setInteger(ENTITY_TAG_EXECUTION_STAGE, 1);
        data.setInteger(ENTITY_TAG_EXECUTION_TIMER, 8);
        data.setDouble(ENTITY_TAG_EXECUTION_START_Y, living.posY);
        data.removeTag(ENTITY_TAG_TARGETED);
        living.motionX = 0.0d;
        living.motionY = 0.0d;
        living.motionZ = 0.0d;
        living.fallDistance = 0.0f;
        living.velocityChanged = true;
    }

    private void clearTargetOwnership(NBTTagCompound data) {
        data.removeTag(ENTITY_TAG_TARGETED);
        data.removeTag(ENTITY_TAG_OWNER);
        data.removeTag(ENTITY_TAG_EXECUTION_TIMER);
        data.removeTag(ENTITY_TAG_EXECUTION_STAGE);
        data.removeTag(ENTITY_TAG_READY_TO_DIE);
        data.removeTag(ENTITY_TAG_KILLER);
        data.removeTag(ENTITY_TAG_EXECUTION_START_Y);
    }
}
