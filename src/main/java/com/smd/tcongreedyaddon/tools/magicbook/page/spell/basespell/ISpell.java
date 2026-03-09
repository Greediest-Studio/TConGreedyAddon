package com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collections;
import java.util.List;

/**
 * 统一法术接口，所有效果实现此接口。
 */
public interface ISpell {
    /**
     * 判断在当前上下文中是否应该触发。
     * @param context 执行上下文
     * @return true 表示应该触发
     */
    boolean canTrigger(SpellContext context);

    /**
     * 执行法术效果。
     * @param context 执行上下文
     * @return true 表示成功执行（可能消耗耐久）
     */
    boolean execute(SpellContext context);

    /**
     * 是否可被切换（参与索引）。仅对主动法术有意义。
     */
    boolean isSelectable();

    /**
     * 本地化名称键。
     */
    String getNameKey();

    /**
     * 图标资源，可能为 null。
     */
    ResourceLocation getIcon();

    /**
     * 冷却时间（刻），0 表示无冷却。
     */
    int getCooldownTicks();

    /**
     * 返回该法术监听的事件类型列表。
     * 默认返回空列表，表示不监听任何特定事件（但仍可能通过 canTrigger 判断事件）。
     * 重写此方法可优化事件分发性能。
     */
    default List<Class<? extends Event>> getListeningEvents() {
        return Collections.emptyList();
    }

    /**
     * 是否在 HUD 上显示该法术的图标和冷却。
     * 对于主动技能通常返回 true，对于被动技能可根据需要决定。
     * 默认返回 true（兼容原有主动技能）。
     */
    default boolean shouldRenderInOverlay() {
        return true;
    }
}