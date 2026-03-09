package com.smd.tcongreedyaddon.tools.magicbook.page;

import com.smd.tcongreedyaddon.Tags;
import com.smd.tcongreedyaddon.tools.magicbook.MagicPageItem;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.ISpell;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.SpellContext;
import com.smd.tcongreedyaddon.tools.magicbook.page.spell.basespell.TriggerSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;

public class DefaultAttackPage extends UnifiedMagicPage {

    public DefaultAttackPage() {
        super(new UnifiedMagicPage.Builder(MagicPageItem.SlotType.LEFT)
                // 默认攻击（原 default_attack）
                .addLeftSpell(new StandardAttackSpell())
                // 测试攻击1
                .addLeftSpell(new TestAttack1Spell())
                // 测试攻击2
                .addLeftSpell(new TestAttack2Spell())
                // 测试攻击3
                .addLeftSpell(new TestAttack3Spell())
                // 测试攻击4
                .addLeftSpell(new TestAttack4Spell())
                // 测试攻击5
                .addLeftSpell(new TestAttack5Spell())
                // 被动效果（显示在下方区域）
                .addLeftSpell(new PassiveMessageSpell())
                .displayName("default_attack_page")
        );

        setRegistryName("default_attack_page");
        setTranslationKey("default_attack_page");
    }

    // ==================== 内部法术类 ====================

    // 默认攻击（标准攻击）
    private static class StandardAttackSpell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            return context.trigger.isType(TriggerSource.Type.LEFT_CLICK) &&
                    context.slot == MagicPageItem.SlotType.LEFT;
        }

        @Override
        public boolean execute(SpellContext context) {
            ItemStack toolStack = context.bookStack;
            EntityPlayer player = context.player;
            Entity target = context.target;
            if (toolStack.getItem() instanceof TinkerToolCore) {
                return ToolHelper.attackEntity(toolStack, (TinkerToolCore) toolStack.getItem(), player, target);
            }
            return false;
        }

        @Override
        public boolean isSelectable() { return true; }

        @Override
        public String getNameKey() { return "default_attack"; }

        @Override
        public ResourceLocation getIcon() {
            return new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/3.png");
        }

        @Override
        public int getCooldownTicks() { return 0; }

        @Override
        public boolean shouldRenderInOverlay() { return true; }
    }

    // 测试攻击1
    private static class TestAttack1Spell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            return context.trigger.isType(TriggerSource.Type.LEFT_CLICK) &&
                    context.slot == MagicPageItem.SlotType.LEFT;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (!context.world.isRemote) {
                context.target.attackEntityFrom(DamageSource.causePlayerDamage(context.player), 1.0f);
                context.player.sendMessage(new TextComponentString("Test attack 1 used!"));
                context.target.setFire(60);
            }
            return true;
        }

        @Override
        public boolean isSelectable() { return true; }

        @Override
        public String getNameKey() { return "test_attack_1"; }

        @Override
        public ResourceLocation getIcon() {
            return new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/1.png");
        }

        @Override
        public int getCooldownTicks() { return 10; }
    }

    // 测试攻击2
    private static class TestAttack2Spell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            return context.trigger.isType(TriggerSource.Type.LEFT_CLICK) &&
                    context.slot == MagicPageItem.SlotType.LEFT;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (!context.world.isRemote) {
                context.target.attackEntityFrom(DamageSource.causePlayerDamage(context.player), 3.0f);
                context.player.sendMessage(new TextComponentString("Test attack 2 used!"));
            }
            return true;
        }

        @Override
        public boolean isSelectable() { return true; }

        @Override
        public String getNameKey() { return "test_attack_2"; }

        @Override
        public ResourceLocation getIcon() {
            return new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/4.png");
        }

        @Override
        public int getCooldownTicks() { return 40; }
    }

    // 测试攻击3
    private static class TestAttack3Spell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            return context.trigger.isType(TriggerSource.Type.LEFT_CLICK) &&
                    context.slot == MagicPageItem.SlotType.LEFT;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (!context.world.isRemote) {
                context.target.attackEntityFrom(DamageSource.causePlayerDamage(context.player), 1.0f);
                context.player.sendMessage(new TextComponentString("Test attack 1 used!"));
                context.target.setFire(60);
            }
            return true;
        }

        @Override
        public boolean isSelectable() { return true; }

        @Override
        public String getNameKey() { return "test_attack_3"; }

        @Override
        public ResourceLocation getIcon() {
            return new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/2.png");
        }

        @Override
        public int getCooldownTicks() { return 10; }
    }

    // 测试攻击4
    private static class TestAttack4Spell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            return context.trigger.isType(TriggerSource.Type.LEFT_CLICK) &&
                    context.slot == MagicPageItem.SlotType.LEFT;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (!context.world.isRemote) {
                context.target.attackEntityFrom(DamageSource.causePlayerDamage(context.player), 3.0f);
                context.player.sendMessage(new TextComponentString("Test attack 2 used!"));
            }
            return true;
        }

        @Override
        public boolean isSelectable() { return true; }

        @Override
        public String getNameKey() { return "test_attack_4"; }

        @Override
        public ResourceLocation getIcon() {
            return new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/5.png");
        }

        @Override
        public int getCooldownTicks() { return 40; }
    }

    // 测试攻击5
    private static class TestAttack5Spell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            return context.trigger.isType(TriggerSource.Type.LEFT_CLICK) &&
                    context.slot == MagicPageItem.SlotType.LEFT;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (!context.world.isRemote) {
                context.target.attackEntityFrom(DamageSource.causePlayerDamage(context.player), 5.0f);
                context.player.sendMessage(new TextComponentString("Test attack 3 used!"));
                context.target.setFire(100);
            }
            return true;
        }

        @Override
        public boolean isSelectable() { return true; }

        @Override
        public String getNameKey() { return "test_attack_5"; }

        @Override
        public ResourceLocation getIcon() {
            return new ResourceLocation(Tags.MOD_ID, "textures/spell_icons/6.png");
        }

        @Override
        public int getCooldownTicks() { return 60; }
    }

    // 被动消息法术（显示在下方区域）
    private static class PassiveMessageSpell implements ISpell {
        @Override
        public boolean canTrigger(SpellContext context) {
            // 每40 tick 触发一次（原 interval=40）
            return context.trigger.isType(TriggerSource.Type.TICK) &&
                    context.player.ticksExisted % 40 == 0;
        }

        @Override
        public boolean execute(SpellContext context) {
            if (!context.world.isRemote) {
                context.player.sendMessage(new TextComponentString("Passive effect triggered!"));
            }
            return true;
        }

        @Override
        public boolean isSelectable() { return false; } // 不参与索引切换

        @Override
        public String getNameKey() { return "spell.6666"; }

        @Override
        public ResourceLocation getIcon() { return null; } // 无图标

        @Override
        public int getCooldownTicks() { return 0; } // 被动无冷却

        @Override
        public boolean shouldRenderInOverlay() { return true; } // 在 HUD 显示
    }
}