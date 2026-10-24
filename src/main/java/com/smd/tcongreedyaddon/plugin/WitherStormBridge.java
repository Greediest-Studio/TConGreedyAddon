package com.smd.tcongreedyaddon.plugin;

import com.smd.tcongreedyaddon.traits.modifiers.base.something.TraitEraseCommand;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;

public final class WitherStormBridge {
    private WitherStormBridge() {}

    public static void register() {
        try {
            Class.forName("com.wdcftgg.witherstormmod.api.common.registry.CommandBlockTools")
                    .getMethod("register", Predicate.class)
                    .invoke(null, (Predicate<ItemStack>) TraitEraseCommand::isCommandBlockTool);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法注册权限核心的命令工具资格", exception);
        }
    }
}
