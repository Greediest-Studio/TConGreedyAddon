package com.smd.tcongreedyaddon.traits.something; // 请替换为您的包名

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.events.ProjectileEvent;
import slimeknights.tconstruct.library.tools.ranged.BowCore;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

public class TraitOvercharge extends AbstractTrait {

    private static final String TAG_START_TICK = "overcharge_start";
    private static final String TAG_FULL_TICK = "overcharge_full";
    private static final String TAG_MULTIPLIER = "overcharge_multiplier"; // 箭上存储的倍数
    private static final float MAX_MULTIPLIER = 6.0f;
    private static final float MAX_OVERCHARGE_FACTOR = 3.0f;

    public TraitOvercharge() {
        super("overcharge", 0xFF5500);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private NBTTagCompound getPlayerData(EntityPlayer player) {
        return player.getEntityData();
    }

    @Override
    public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote) return;
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        NBTTagCompound data = getPlayerData(player);

        boolean isUsingThis = player.isHandActive() && player.getActiveItemStack() == tool;

        if (!isUsingThis) {
            data.removeTag(TAG_START_TICK);
            data.removeTag(TAG_FULL_TICK);
            return;
        }

        if (!data.hasKey(TAG_START_TICK)) {
            data.setLong(TAG_START_TICK, world.getTotalWorldTime());
        }

        if (tool.getItem() instanceof BowCore) {
            BowCore bow = (BowCore) tool.getItem();
            float progress = bow.getDrawbackProgress(tool, player);
            if (progress >= 1.0f && !data.hasKey(TAG_FULL_TICK)) {
                data.setLong(TAG_FULL_TICK, world.getTotalWorldTime());
            }
        }
    }

    @SubscribeEvent
    public void onProjectileLaunch(ProjectileEvent.OnLaunch event) {
        ItemStack launcher = event.launcher;
        if (launcher.isEmpty() || !(launcher.getItem() instanceof BowCore)) return;
        if (!TinkerUtil.hasTrait(launcher.getTagCompound(), this.identifier)) return;

        if (!(event.projectileEntity instanceof Entity)) return;
        Entity projectile = event.projectileEntity;

        if (!(event.shooter instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.shooter;

        NBTTagCompound data = getPlayerData(player);
        if (!data.hasKey(TAG_START_TICK) || !data.hasKey(TAG_FULL_TICK)) {
            return;
        }

        long startTick = data.getLong(TAG_START_TICK);
        long fullTick = data.getLong(TAG_FULL_TICK);
        long currentTick = projectile.getEntityWorld().getTotalWorldTime();

        int actualFullTime = (int) (fullTick - startTick);
        if (actualFullTime <= 0) return;

        int overchargeTicks = (int) (currentTick - fullTick);
        if (overchargeTicks <= 0) return;

        int maxTicks = (int) (actualFullTime * MAX_OVERCHARGE_FACTOR);
        float multiplier;
        if (overchargeTicks >= maxTicks) {
            multiplier = MAX_MULTIPLIER;
        } else {
            multiplier = 1.0f + (overchargeTicks / (float) maxTicks) * (MAX_MULTIPLIER - 1.0f);
        }

        projectile.getEntityData().setFloat(TAG_MULTIPLIER, multiplier);

        data.removeTag(TAG_START_TICK);
        data.removeTag(TAG_FULL_TICK);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().getEntityWorld().isRemote) return;

        if (!"arrow".equals(event.getSource().getDamageType())) return;

        Entity immediateSource = event.getSource().getImmediateSource();
        if (!(immediateSource instanceof Entity)) return;

        NBTTagCompound arrowData = immediateSource.getEntityData();
        if (!arrowData.hasKey(TAG_MULTIPLIER)) return;

        float multiplier = arrowData.getFloat(TAG_MULTIPLIER);
        event.setAmount(event.getAmount() * multiplier);
    }
}