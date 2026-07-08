package com.smd.tcongreedyaddon.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class MixinPlugin implements IMixinConfigPlugin {

    private static final Map<String, BooleanSupplier> MIXIN_GROUPS = new HashMap<>();

    private String mixinPackage;

    static {
//        addMixinGroup(
//                () -> GCTMixinConfig.enableDamageParticleMixin,
//                "damageparticle.MixinEntityPlayer",
//                "damageparticle.MixinEntity"
//        );
    }

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return isMixinEnabled(mixinClassName);
    }

    private boolean isMixinEnabled(String mixinClassName) {
        String relativeMixinName = getRelativeMixinName(mixinClassName);
        BooleanSupplier condition = MIXIN_GROUPS.get(relativeMixinName);
        return condition == null || condition.getAsBoolean();
    }

    private String getRelativeMixinName(String mixinClassName) {
        if (mixinPackage != null && mixinClassName.startsWith(mixinPackage + ".")) {
            return mixinClassName.substring(mixinPackage.length() + 1);
        }
        return mixinClassName;
    }

    private static void addMixinGroup(BooleanSupplier condition, String... mixins) {
        for (String mixin : mixins) {
            MIXIN_GROUPS.put(mixin, condition);
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
