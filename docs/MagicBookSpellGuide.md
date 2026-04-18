# Magic Book 法术与书签指南

本指南帮助开发者在当前的架构下快速落地新的魔术书法术与书签，同时介绍事件/蓝图/Builder 之间如何组合使用、新的动态图标能力，以及如何把多事件法术嵌入页面。

## 架构概览
1. **SpellBlueprint** 负责把名字 key、图标、冷却、施法动作锁（`castActionTicks`）、默认是否可选/渲染、监听的 Forge 事件等 metadata 用链式调用组织成一个不可变配置。
2. **AbstractSpell** 把 blueprint 拿来当默认 impl：子类在构造器里 `super(BLUEPRINT)` 即可复用 getter、metadata，自己只需 override `canTriggerInternal(SpellContext)`、`executeInternal(SpellContext)`、必要时 override `computeCooldownTicks`/`onEvent` 来补充行为。
3. **UnifiedMagicPage.Builder** 负责收集左右法术，每个 `addLeftSpell/addRightSpell` 会把 `ISpell` 实例按顺序加入。`build()` 会构造 `UnifiedMagicPage` 并自动把页面连同左右法术注册到 **SpellRegistry**，让游戏 UI/冷却显示/调试视图都能统一读取。页面类（如 `RangePulsePage`）构造完成后只需 `setRegistryName`/`setTranslationKey` 并在模块中 `event.getRegistry().register` 即可。
4. **SpellContext** 负责把 `world`/`player`/`bookStack`/`pageStack`/`pageData`/`slot`/`trigger`（`TriggerSource`）/`gesture`/`target` 传给法术，内置懒加载 helper：`getRange()`/`getCritChance()`/`getSpellSpeed()`/`getLeftSlotCount()`/`getRightSlotCount()`/`getCurrentSlotCount()` 均来自 `MagicBookToolNBT`，可用于让法术随核心 Stats 与页面插槽动态变化。
5. **MagicBookEventHandler** 订阅 Forge 事件（目前包括 `LivingJumpEvent` 与 `LivingAttackEvent`），把持书玩家转交给 `handleEvent`，再由 `UnifiedMagicPage` 查找注册了对应事件的法术并调用 `executeSpellWithRawIndex`。
6. **MagicBook** 本体管理冷却计时（`TAG_COOLDOWNS`）、客户端同步、图标渲染、`player.swingArm` 动画与 `SpellOverlayRenderer` 渲染数据，只要 blueprint + `AbstractSpell` 实现没问题，视觉与逻辑就保持一致。

## TriggerSource 与 SpellContext
- `TriggerSource.Type` 当前包括 `LEFT_CLICK`/`RIGHT_CLICK`/`TICK`/`HOLD_TICK`/`HOLD_RELEASE`/`KEY_GESTURE`，`TriggerSource.event(Event)` 可把 Forge 事件包装进 `SpellContext`。`canTriggerInternal` 可通过 `context.trigger.isType`/`context.gesture`/`context.slot`/`context.trigger.getEvent()` 精细区分来源。
- `SpellContext` 中的 `pageData` 是当前页面的 NBT，开发者可用它跨帧保存状态，`target` 只有在近战触发时才有实体，`bookStack` 是魔导书本体。`getRange/getCritChance/getSpellSpeed` 反映核心 Stats、`getLeftSlotCount/getRightSlotCount/getCurrentSlotCount` 允许按插槽数调整威力或反馈。
- **动态图标**：`UnifiedMagicPage` 在 HUD 渲染与 `SpellOverlayRenderer` 中会调用 `ISpell.getDisplayIcon(NBTTagCompound pageData, int rawIndex)`（默认返回 `getIcon()`）。你可以在 `executeInternal`/`onEvent` 里把 `pageData` 某个 key（比如 `adaptive_guard_icon`）写入不同状态，再在 override 的 `getDisplayIcon` 里根据 `pageData` 内容返回不同的 `ResourceLocation`，让 GUI 在事件触发后即时切换图标。

## 普通书签与按键书签
- **普通书签**：主要通过左键/右键/蓄力/事件触发，遵循原有施法主链路（`LEFT_CLICK`/`RIGHT_CLICK`/`HOLD_*`/`TICK`/`event`）。
- **按键书签**：通过 `KEY_GESTURE` 触发，客户端只上传输入边沿，服务端重建手势后再执行法术。
- 两类书签都使用 `UnifiedMagicPage` 承载法术；区别在于触发源、注册标记和放置约束。

## 按键书签手势与放置策略
- 客户端只发送按键边沿输入（`PRESS/RELEASE`），服务端状态机重建手势并触发法术。当前支持手势：
  - 边沿：`PRESS_A`/`PRESS_B`/`RELEASE_A`/`RELEASE_B`
  - 单键：`TAP_A`/`TAP_B`/`LONG_A`/`LONG_B`
  - 组合：`CHORD_TAP`/`CHORD_LONG`/`HOLD_A_TAP_B`/`HOLD_B_TAP_A`
- `LONG_A/LONG_B` 当前语义为“达到长按阈值即触发一次”，不再等到抬手触发；同一次按住仅触发一次，抬手后重置。
- 键绑法术实现 `IKeybindGestureSpell`，在 `supportsGesture(slotType, gesture)` 声明支持手势，并在 `onGestureTriggered` 中执行逻辑。
- 若需要“按住持续生效，抬手结束”的技能，可实现 `IKeybindHoldSpell`（`onKeyHoldStart/onKeyHoldTick/onKeyHoldEnd`）；服务端会在按住期间按 tick 驱动。
- 关键手感阈值可在 `specialweapons` 模块配置：`keybindLongPressTicks`、`keybindTapMaxTicks`、`keybindChordLongTicks`、`keybindChordTapMaxTicks`、`keybindHoldTriggerTicks`、`keybindActionLockMinTicks`。
- `MagicPageItem` 新增放置策略：`LEFT_ONLY`/`RIGHT_ONLY`/`BOTH`。`BookInventory` 通过 `supportsSlot` 校验可放置侧别；若页面配置 `BOTH`，会按安装侧读取对应侧法术集合。
- `UnifiedMagicPage.Builder.keybindPage(true)` 可把页面注册为按键书签；放置时每侧（左/右）最多允许 1 个按键书签，防止手势冲突。普通书签不受这条限制。
- 对于 `0` 冷却法术，建议在 blueprint 配置 `castActionTicks`。服务端会在键绑触发中应用动作锁，防止持续连发。
- 旧按键协议 `SkillKeyPacket` 与独立近战包 `GrappleMeleePacket` 已移除，统一使用 `KeybindInputPacket + KEY_GESTURE`。

## 缚丝书签（当前实现）
- 缚丝书签是按键书签，当前标记为 `keybindPage(true)`，并配置在左侧法术集合。
- 当前触发语义：
  - `PRESS_A`：立即尝试生成节点并连接（若已有连接则不重复连接）
  - `RELEASE_A`：结束连接
  - `PRESS_B`/`TAP_B`/`HOLD_A_TAP_B`：触发缚丝近战
- 近战触发窗口来自 `StrandConnectionManager` 的连接状态与 `MELEE_GRACE_TICKS`，窗口内图标会切换为近战就绪图标。

## 热能抽离书签（当前实现）
- 热能抽离是左侧按键书签，使用 `TAP_A`（火）与 `LONG_A`（冰），长按阈值当前为 `10 tick`。
- 火模式给范围内敌对生物叠加 `gauss_heat`（最多 5 层、持续 8 秒），并点燃 5 秒。
- `gauss_heat` 每秒按层数指数倍率结算一次：`0.1 * 2^(层数-1)`，对应 `0.1/0.2/0.4/0.8/1.6`。
- 冰模式给范围内敌对生物施加 `Slowness`（持续 8 秒）。
- 当目标同时具有热与冰时，立即触发融合：结算剩余 DoT 总伤（`每秒倍率 * 剩余秒数`）、移除热与缓慢、并给非玩家目标附加一次性 `-50%` 护甲修饰符（固定 UUID 去重）。
- 该法术配置 `cooldown(0) + castActionTicks(3)`，并在触发时根据模式播放红/蓝粒子环与 actionbar 文案。

## 样例法术与书签
- `DefaultAttackPage`/`BeamAttackPage`/`FireballPage`/`JumpBoostPage` 分别组合了 `StandardAttackSpell`（左键近战）、`BeamAttackSpell`/`SmallFireballSpell`/`LargeFireballSpell`（右键远程）、`JumpBoostSpell`（监听 `LivingJumpEvent` 的被动）与 `PassiveMessageSpell`（每 40 tick 发消息）。这些都可作为不同 `TriggerSource.Type` 与 `SpellContext` 的参考。
- 新增的 `RangePulseSpell` 展示了右键法术如何利用 `context.getRange()`、`context.getCritChance()`、`context.getSpellSpeed()` 与 `player.getHealth()` 计算范围伤害与动态冷却，并通过 `MagicBookToolNBT` 的属性适配核心/页面装备。
- `DeflectiveWardSpell` 是事件法术：监听 `LivingAttackEvent`、验证 `context.trigger.isEvent()`、用 `context.getCurrentSlotCount()` 决定反击力度；只要 `MagicBookEventHandler` 有对应 `@SubscribeEvent`，它就能在玩家受击时自动执行并走冷却流程。
- `AdaptiveGuardSpell` 同时监听 `LivingAttackEvent` 与 `LivingJumpEvent`，在 `executeInternal` 里分别设置 `pageData` 的 `adaptive_guard_icon` 为 `attack` 或 `jump`，再 override `getDisplayIcon(pageData, rawIndex)` 返回 `iron_sword`/`feather` 图标，从而在 GUI 中根据最近一次事件类型自动切换图标。
- 这些法术都被加入了 `RangePulsePage`（右槽），Builder 会在 `build()` 时自动把页面与所有 `ISpell` 注册到 `SpellRegistry`，`SpecialWeapons.initItems` 只要 `event.getRegistry().register(new RangePulsePage())` 就能让玩家拿到这组法术。

## 添加新法术
1. `SpellBlueprint.builder("your_spell_key")` 依次用 `.icon(...)`/`.cooldown(...)`/`.selectable(...)`/`.renderInOverlay(...)`/`.listeningEvents(...)` 描述 metadata。
2. 创建 `AbstractSpell` 子类，在构造器里 `super(BLUEPRINT)`，即可复用 getter，剩下只需 override `canTriggerInternal` 与 `executeInternal` 实现逻辑；需要动态冷却可 override `computeCooldownTicks`。
3. 在 `canTriggerInternal` 里结合 `context.trigger` 与 `context.slot` 判断当前是否要发动，事件法术可以再加 `context.trigger.isEvent()` + `context.trigger.getEvent()` 判断类。
4. `executeInternal` 可直接读取 `context.pageData`/`pageStack`、对目标施加效果、播放粒子/音效，并在必要时写入 `pageData` 保存状态（比如图标切换、计数器）。
5. 需要在 HUD 上显示状态时，override `getDisplayIcon(NBTTagCompound pageData, int rawIndex)`，根据 `pageData` 里的 key 返回不同 `ResourceLocation`。在 `executeInternal` 内设置 `pageData.setString("your_icon_key", "state")` 即可配合上面的动态渲染。

## 添加新书签页面
1. 在 `tools/magicbook/page/` 下创建继承 `UnifiedMagicPage` 的类，构造器用 `new UnifiedMagicPage.Builder(SlotType.LEFT/RIGHT)` 开始，依序 `addLeftSpell/addRightSpell` 加入 `ISpell` 实例，最后 `displayName("page_key")`。
2. 若这是按键书签页面，需在 builder 上调用 `.keybindPage(true)`，并在页面法术中实现 `IKeybindGestureSpell`。
3. `Builder.build()` 会自动产生 `UnifiedMagicPage`、把当前页面与左右法术列表注册到 `SpellRegistry`，开发时可通过 `SpellRegistry.getRegisteredPages()` 验证。页面构造完成后在 `SpecialWeapons.initItems` 或对应模块里 `event.getRegistry().register(new YourPage())`。
4. 选择放置侧策略：
   - 左侧专用：`Builder(SlotType.LEFT)`
   - 右侧专用：`Builder(SlotType.RIGHT)`
   - 双侧可放：`Builder(PlacementPolicy.BOTH)` 并分别 `addLeftSpell/addRightSpell`
5. 按键书签在运行时每侧最多一个，设计双侧可放页面时应保证左右法术映射清晰，避免手势歧义。
6. 如果页面包含主动/被动混合法术，Builder 还能同时挂载 `selectable(false)` 的被动法术，它们会在元数据里显示为“被动”，HUD 仍然能渲染冷却/图标。

## 事件监听与 Builder 混合说明
- 所有 Forge 事件现在集中在 `MagicBookEventHandler`：在 blueprint 里 `listeningEvents(LivingJumpEvent.class, LivingAttackEvent.class)` 后，`UnifiedMagicPage` 会把事件 class 映射到对应法术，事件触发时只要 `MagicBookEventHandler` 有相应的 `@SubscribeEvent`（如 `onLivingJump`/`onLivingAttack`）就会执行 `spell.onEvent` → `executeSpellWithRawIndex`。
- 事件法术的 `canTriggerInternal` 应在 `context.trigger.isEvent()` 为 `true` 且 `context.trigger.getEvent()` 与 `listeningEvents` 中类型一致时返回 `true`，再结合 `context.slot` 或 `context.player` 状态做额外限制。
- 若需要新的事件（例如 `LivingFallEvent`），除了在 blueprint 声明，也必须在 `MagicBookEventHandler` 增加对应 `@SubscribeEvent` 方法并调 `handleEvent`，否则事件不会被传递。
- Builder 负责注册/metadata，`AbstractSpell` 负责行为：通过 `getDisplayIcon(NBTTagCompound pageData, int rawIndex)` 与 `pageData` 协作可以在事件之中切换图标，同时 `executeInternal` 中修改 NBT 会被 `UnifiedMagicPage` 读回并持久化到页面 stack。

## 冷却与 GUI 同步
- `MagicBook.executeSpellWithRawIndex` 在 `executeInternal` 返回 `true` 时写入 `TAG_COOLDOWNS` 并调用 `MagicBook.syncHeldBook`，确保 `ItemStack` 与 `SpellOverlayRenderer` 的 GUI 冷却状态一致；客户端右键若在冷却中会直接返回 `PASS`，成功触发后服务端 `swingArm` 并同步 NBT。
- `SpellRegistry` 中的页面定义可用于调试或在 `SpellOverlayRenderer` 中获得 `SpellDisplayData`（以及 `getDisplayIcon` 所决定的当前图标），因此 `pageData` 里的状态改动即刻影响 HUD。

## 参考路径
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/MagicBook.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/UnifiedMagicPage.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/spell/AbstractSpell.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/spell/SpellBlueprint.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/spell/SpellRegistry.java`
- `src/main/java/com/smd/tcongreedyaddon/event/MagicBookEventHandler.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/RangePulsePage.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/spell/impl/RangePulseSpell.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/spell/impl/DeflectiveWardSpell.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/spell/impl/AdaptiveGuardSpell.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/ThermalSunderPage.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/spell/impl/ThermalSunderSpell.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/effect/ThermalHeatPotion.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/effect/ThermalSunderRuntime.java`
- `src/main/java/com/smd/tcongreedyaddon/tools/magicbook/page/spell/basespell/IKeybindHoldSpell.java`
