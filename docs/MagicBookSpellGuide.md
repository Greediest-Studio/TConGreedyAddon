以下是精简后的开发者指南，聚焦核心流程与关键约定。

# Magic Book 法术与书签开发指南

本文档面向需要在当前架构下新增法术或书签页面的开发者，解释核心类职责、法术创建步骤、触发源处理、按键手势、动态图标及事件监听方式。

## 核心类职责速览

| 类 / 接口 | 角色 |
|-----------|------|
| `SpellBlueprint` | 法术元数据：名称 key、图标、冷却、动作锁、是否可选、是否显示在 HUD、监听的事件类型 |
| `AbstractSpell` | 法术基类，接收一个 `SpellBlueprint`，子类只需实现 `canTriggerInternal` 和 `executeInternal` |
| `ISpell` | 法术接口，定义 `canTrigger`、`execute`、`getCooldownTicks`、`getDisplayIcon` 等 |
| `SpellContext` | 施法时的动态信息：`world`、`player`、`bookStack`、`pageStack`、`pageData`、`slot`、`trigger`（`TriggerSource`）、`gesture`、`target`；提供 `getRange()`、`getCritChance()` 等工具方法 |
| `TriggerSource` | 触发原因枚举：`LEFT_CLICK`、`RIGHT_CLICK`、`HOLD_TICK`、`HOLD_RELEASE`、`KEY_GESTURE`、`event(Event)` 等 |
| `UnifiedMagicPage` | 书页基类，用 `Builder` 收集左右侧法术，自动注册到 `SpellRegistry`，管理索引和 HUD 显示 |
| `MagicBookCastingCore` | 施法调度中心，将输入事件路由到对应法术 |
| `MagicBookStateHelper` | 持握状态、按键手势解析、法术目标解析、索引校验 |
| `KeybindGestureState` | 服务端按键手势识别（A/B 按键的短按、长按、组合） |
| `MagicBookEventHandler` | 监听 Forge 事件并调用页面/法术 |

## 1. 创建法术

### 1.1 定义 `SpellBlueprint`
在法术类中将以下静态字段作为蓝图常量：

```java
private static final SpellBlueprint BLUEPRINT = SpellBlueprint.builder("spell.key")
    .icon(new ResourceLocation("modid", "textures/spell_icon.png"))
    .cooldown(20)                // 基础冷却（刻），0 表示无冷却
    .castActionTicks(0)          // 施法动作锁（刻），0 冷却法术应设置 >0 防止连发
    .selectable(true)            // 是否可通过切换选中
    .renderInOverlay(true)       // 是否在 HUD 上显示图标
    .listeningEvents(LivingAttackEvent.class) // 监听的事件（可多个，用于法术被动触发）
    .build();
```

### 1.2 实现 `AbstractSpell` 子类
子类构造函数中传入蓝图，然后实现必要的 `@Override` 方法：

```java
public class MySpell extends AbstractSpell {

    public MySpell() {
        super(BLUEPRINT);
    }

    @Override
    protected boolean canTriggerInternal(SpellContext context) {
        // 判断触发条件，常用写法：
        // - 主动右键：context.trigger.isType(TriggerSource.Type.RIGHT_CLICK) && context.slot == SlotType.RIGHT
        // - 事件：context.trigger.isEvent() && context.trigger.getEvent() instanceof LivingAttackEvent
        // - 按键手势：context.trigger.isType(TriggerSource.Type.KEY_GESTURE) && context.gesture != null
        return /* your condition */;
    }

    @Override
    protected boolean executeInternal(SpellContext context) {
        if (context.world.isRemote) return true; // 客户端直接返回 true 表示已处理
        // 在此处执行法术效果（攻击、生成实体、粒子、音效等）
        // 可写入 context.pageData 保存跨帧状态
        return true; // 返回 true 表示成功，将触发冷却和耐久消耗
    }
}
```

**可选覆写方法：**
- `int computeCooldownTicks(EntityPlayer player, ItemStack bookStack)` – 动态冷却（如基于生命值）
- `ResourceLocation getDisplayIcon(NBTTagCompound pageData, int rawIndex)` – 动态图标（常在 `executeInternal` 中修改 `pageData` 后再返回不同图标）
- `void onEvent(Event event, SpellContext context, int rawIndex)` – 对监听的事件做额外预处理

### 1.3 按键手势法术
若法术由手势触发，需额外实现 `IKeybindGestureSpell`：

```java
public class GestureSpell extends AbstractSpell implements IKeybindGestureSpell {

    @Override
    public boolean supportsGesture(MagicPageItem.SlotType slotType, GestureType gesture) {
        return slotType == SlotType.LEFT && gesture == GestureType.TAP_A;
    }

    @Override
    public GestureResult onGestureTriggered(SpellContext context, GestureType gesture, boolean onCooldown) {
        // 执行逻辑
        // 返回 SUCCESS_NO_COOLDOWN / SUCCESS_APPLY_COOLDOWN / PASS
    }
}
```

### 1.4 按键保持法术（按住持续，抬起结束）
可实现 `IKeybindHoldSpell`，其生命周期：`onKeyHoldStart` → `onKeyHoldTick` → `onKeyHoldEnd`，无需手动操作状态机。

## 2. 创建书签页面
在 `tools/magicbook/page/` 下继承 `UnifiedMagicPage`：

```java
public class MyPage extends UnifiedMagicPage {
    public MyPage() {
        super(new Builder(SlotType.RIGHT)        // 可 LEFT / RIGHT / PlacementPolicy.BOTH
            .addRightSpell(new MySpell())        // 按序添加
            .keybindPage(true)                   // 若为按键书签则标记
            .displayName("page_key")
        );
        setRegistryName("my_page");
        setTranslationKey("my_page");
    }
}
```

然后在 `SpecialWeapons.initItems` 或对应注册处调用 `event.getRegistry().register(new MyPage())`。

**重要规则：**
- 左右侧法术分别在构建时用 `addLeftSpell` / `addRightSpell` 添加
- 若页面允许双侧放置（`PlacementPolicy.BOTH`），需分别为左右侧添加法术，运行时会依据安装侧解析对应的法术列表
- 标记为按键书签的页面，魔导书每侧最多允许 1 个，以避免手势冲突

## 3. 触发源说明
常用触发源在 `TriggerSource.Type` 中定义：

- `LEFT_CLICK` / `RIGHT_CLICK`：鼠标左/右键（`castSelectedSpell` 调用）
- `HOLD_TICK` / `HOLD_RELEASE`：长按蓄力/释放（`IHoldTriggerSpell` 或 `IChannelReleaseSpell` 法术使用）
- `KEY_GESTURE`：按键手势识别后触发（`IKeybindGestureSpell`）
- `event(Event)`：Forge 事件包装，用于被动触发

在 `canTriggerInternal` 中可通过以下方式判断来源：
```java
context.trigger.isType(TriggerSource.Type.RIGHT_CLICK)
context.trigger.isEvent()
context.trigger.getEvent() instanceof LivingJumpEvent
context.gesture == GestureType.LONG_A
```

## 4. 按键手势与动作锁
客户端仅发送按键的 `PRESS/RELEASE` 事件，服务端 `KeybindGestureState` 重建手势：

**当前支持的手势：**
- 边沿：`PRESS_A`、`PRESS_B`、`RELEASE_A`、`RELEASE_B`
- 单键：`TAP_A`、`TAP_B`、`LONG_A`、`LONG_B`  
  *注：`LONG` 在按住达到阈值时触发一次，不等抬起。*
- 组合：`CHORD_TAP`、`CHORD_LONG`、`HOLD_A_TAP_B`、`HOLD_B_TAP_A`

**手感阈值**通过 `KeybindTuningConfig` 配置，默认值见配置类，可在 `specialweapons` 模块覆盖。

**动作锁**：对于 0 冷却的法术，建议在 `SpellBlueprint` 中设置 `castActionTicks(3)`，服务端会依此防止同一法术在手势连打时触发太快。

## 5. 动态图标
若希望 HUD 中法术图标随状态变化（例如 `AdaptiveGuardSpell` 根据最近一次事件显示不同图标），在法术类中：
1. 在 `executeInternal` / `onEvent` 里向 `context.pageData` 写入字符串（如 `pageData.setString("adaptive_guard_icon", "attack")`）
2. 覆写 `getDisplayIcon(NBTTagCompound pageData, int rawIndex)`，按 `pageData` 中的 key 返回不同的 `ResourceLocation`

`UnifiedMagicPage` 与渲染器会自动调用 `getDisplayIcon` 获取当前图标。

## 6. 事件监听
要在魔导书持有时自动响应 Forge 事件：
1. 在 `SpellBlueprint` 中通过 `listeningEvents(LivingJumpEvent.class, ...)` 声明
2. 确保 `MagicBookEventHandler` 中有对应的 `@SubscribeEvent` 方法（目前已有 `LivingJumpEvent` 和 `LivingAttackEvent`），并在其中调用 `handleEvent`
3. 法术的 `canTriggerInternal` 返回 `true` 且 `executeInternal` 执行效果

新增事件类型时，除了蓝图声明和法术实现，还需要在 `MagicBookEventHandler` 中添加对应的 `@SubscribeEvent`+`handleEvent` 调用。

## 7. 内置法术参考
以下法术覆盖了主流用法，可作为模板：
- **`BeamAttackSpell`** – 右键范围攻击，体现 `getRange()` 和冷却
- **`ChargedHoldFireballSpell`** – 长按蓄力连射，`IHoldTriggerSpell` 典型实现
- **`FreezeRaySpell`** – 长按持续射线，`IHoldTriggerSpell`，使用第三方激光 API
- **`AdaptiveGuardSpell`** – 监听多个事件并切换图标
- **`RangePulseSpell`** – 右键 AOE，动态冷却基于玩家生命值
- **`ThermalSunderSpell`** – 按键手势法术，`TAP_A`/`LONG_A`
- **`StrandGrappleSpell`** – 按键手势与组合键

## 8. 注意事项
- `pageData`（`NBTTagCompound`）用于跨帧保存状态，系统会自动同步，无需手动序列化。
- 若法术需要持握触发的特殊模型，应实现相应的 `IHoldTriggerSpell` 或 `IChannelReleaseSpell`，`MagicBookCastingCore` 会自动处理。
- 按键书签每侧仅限一个，设计双侧页面时务必使左右法术手势不冲突。
- 测试时可通过 `SpellRegistry.getRegisteredPages()` 检查页面和法术注册情况。

---

遵循以上模式即可快速开发新法术，无需深入修改核心调度逻辑。