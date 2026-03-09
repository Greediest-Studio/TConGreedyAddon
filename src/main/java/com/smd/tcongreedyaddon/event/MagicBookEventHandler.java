package com.smd.tcongreedyaddon.event;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicBook;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

/**
 * 魔法书事件处理器。
 * 负责监听玩家触发的各种事件（攻击、右键点击等），并将事件转发给玩家手持的魔导书执行对应法术。
 * 魔导书的实际执行逻辑封装在 {@link MagicBook#executeSpell} 方法中。
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class MagicBookEventHandler {

    // ==================== 左键攻击相关事件 ====================

    /**
     * 当玩家造成伤害时触发（LivingHurtEvent）。
     * 适用于大多数攻击法术。
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        handleSpellTrigger(player, MagicPageItem.SlotType.LEFT, event.getEntity());
    }

    /**
     * 当玩家发起攻击时触发（LivingAttackEvent），发生在伤害计算之前。
     * 适用于需要在伤害前修改属性或执行特殊逻辑的法术。
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        handleSpellTrigger(player, MagicPageItem.SlotType.LEFT, event.getEntity());
    }

    /**
     * 当玩家杀死实体时触发（LivingDeathEvent）。
     * 适用于击杀触发的法术（如吸取灵魂、掉落加成等）。
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        handleSpellTrigger(player, MagicPageItem.SlotType.LEFT, event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isRemote) return;
        if (event.getEntityPlayer().isSneaking()) return;
        handleSpellTrigger(event.getEntityPlayer(), MagicPageItem.SlotType.RIGHT, null);
    }

    // ==================== 右键点击相关事件 ====================

    /**
     * 当玩家右键点击方块时触发。
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) return;
        // 注意：如果玩家同时潜行，魔导书的 onItemRightClick 会处理打开 GUI，此处不应再触发法术
        if (event.getEntityPlayer().isSneaking()) return;
        handleSpellTrigger(event.getEntityPlayer(), MagicPageItem.SlotType.RIGHT, null);
    }

    /**
     * 当玩家右键点击空气时触发。
     */
    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getWorld().isRemote) return;
        if (event.getEntityPlayer().isSneaking()) return;
        handleSpellTrigger(event.getEntityPlayer(), MagicPageItem.SlotType.RIGHT, null);
    }

    /**
     * 当玩家右键点击实体时触发（可选，如需要可添加）。
     * 注意：Minecraft 中右键实体通常由 {@link PlayerInteractEvent.EntityInteract} 处理。
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getWorld().isRemote) return;
        if (event.getEntityPlayer().isSneaking()) return;
        handleSpellTrigger(event.getEntityPlayer(), MagicPageItem.SlotType.RIGHT, event.getTarget());
    }

    // ==================== 左键点击方块事件（可选）====================

    /**
     * 当玩家左键点击方块（开始破坏）时触发。
     * 可用于在破坏方块前触发效果。
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getWorld().isRemote) return;
        // 左键点击方块通常不触发左键攻击法术（除非目标为空），这里可根据需求决定是否处理
        // 如果希望左键点击方块也触发法术，可以调用 handleSpellTrigger 并传入目标实体 null
        // handleSpellTrigger(event.getEntityPlayer(), MagicPageItem.SlotType.LEFT, null);
    }

    // ==================== 通用触发方法 ====================

    /**
     * 统一触发逻辑：检查玩家主手的魔导书，并调用其 executeSpell 方法执行法术。
     *
     * @param player 玩家
     * @param slot   槽位类型（左/右）
     * @param target 目标实体（左键攻击事件时需要，右键可为 null）
     */
    private static void handleSpellTrigger(EntityPlayer player, MagicPageItem.SlotType slot, @Nullable Entity target) {
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.getItem() instanceof MagicBook) {
            MagicBook book = (MagicBook) mainHand.getItem();
            book.executeSpell(mainHand, player, slot, target);
        }
    }
}