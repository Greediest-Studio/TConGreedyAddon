package com.smd.tcongreedyaddon.traits.something;

import crazypants.enderio.api.teleport.ITravelAccessable;
import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.config.config.TeleportConfig;
import crazypants.enderio.base.teleport.TravelController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import java.util.HashMap;
import java.util.Map;

public class TraitTravel extends AbstractTrait {

    private static final Map<String, Long> playerBlinkCooldowns = new HashMap<>();

    public TraitTravel() {
        super("travel", 0xffffff);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        EnumHand hand = event.getHand();
        ItemStack heldItem = event.getItemStack();

        if (hand != EnumHand.MAIN_HAND || !player.world.isRemote) return;

        if (!(heldItem.getItem() instanceof ToolCore)) return;
        
        if (!TinkerUtil.hasTrait(heldItem.getTagCompound(), this.identifier)) return;

        handleTravelToAnchor(player, hand, heldItem, event);
        heldItem.damageItem(2, player);
    }

    /**
     * 旅行手杖的功能
     */
    private void handleTravelToAnchor(EntityPlayer player, EnumHand hand, ItemStack heldItem, PlayerInteractEvent.RightClickItem event) {
        if (player.isSneaking()) {

            if (!TeleportConfig.enableBlink.get()) return;

            long lastBlinkTick = getPlayerLastBlinkTick(player);
            long currentTick = EnderIO.proxy.getTickCount();
            long ticksSinceBlink = currentTick - lastBlinkTick;
            
            if (ticksSinceBlink < TeleportConfig.blinkDelay.get()) return;

            boolean success = TravelController.doBlink(heldItem, hand, player);
            
            if (success) {
                player.swingArm(hand);
                setPlayerLastBlinkTick(player, currentTick);
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.util.EnumActionResult.SUCCESS);
            }
        } else {
            RayTraceResult rayTrace = player.rayTrace(128.0D, 1.0F);
            if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.BLOCK) return;

                    
            BlockPos targetPos = rayTrace.getBlockPos();
            TileEntity te = player.world.getTileEntity(targetPos);

            if (!(te instanceof ITravelAccessable)) return;

            boolean success = TravelController.activateTravelAccessable(heldItem, hand, player.world, player, TravelSource.STAFF);

            if (!success) {
                double targetX = targetPos.getX() + 0.5D;
                double targetY = targetPos.getY() + 1.0D;
                double targetZ = targetPos.getZ() + 0.5D;

                player.setPositionAndUpdate(targetX, targetY, targetZ);
                player.fallDistance = 0.0F;
            }

            player.swingArm(hand);
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }

    /**
     * 获取玩家上次 Blink 的时间戳
     */
    private long getPlayerLastBlinkTick(EntityPlayer player) {
        return playerBlinkCooldowns.getOrDefault(player.getName(), 0L);
    }

    /**
     * 设置玩家上次 Blink 的时间戳
     */
    private void setPlayerLastBlinkTick(EntityPlayer player, long tick) {
        playerBlinkCooldowns.put(player.getName(), tick);
    }
}
