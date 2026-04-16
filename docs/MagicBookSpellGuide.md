# Magic Book 法术与书签指南

本指南帮助开发者在当前的架构下快速落地新的魔术书法术与书签，同时介绍事件/蓝图/Builder 之间如何组合使用、新的动态图标能力，以及如何把多事件法术嵌入页面。

## 架构概览
1. **SpellBlueprint** 负责把名字 key、图标、冷却、默认是否可选/渲染、监听的 Forge 事件等 metadata 用链式调用组织成一个不可变配置。
2. **AbstractSpell** 把 blueprint 拿来当默认 impl：子类在构造器里 `super(BLUEPRINT)` 即可复用 getter、metadata，自己只需 override `canTriggerInternal(SpellContext)`、`executeInternal(SpellContext)`、必要时 override `computeCooldownTicks`/`onEvent` 来补充行为。
3. **UnifiedMagicPage.Builder** 负责收集左右法术，每个 `addLeftSpell/addRightSpell` 会把 `ISpell` 实例按顺序加入。`build()` 会构造 `UnifiedMagicPage` 并自动把页面连同左右法术注册到 **SpellRegistry**，让游戏 UI/冷却显示/调试视图都能统一读取。页面类（如 `RangePulsePage`）构造完成后只需 `setRegistryName`/`setTranslationKey` 并在模块中 `event.getRegistry().register` 即可。
4. **SpellContext** 负责把 `world`/`player`/`bookStack`/`pageStack`/`pageData`/`slot`/`trigger`（`TriggerSource`）/`target` 传给法术，内置懒加载 helper：`getRange()`/`getCritChance()`/`getSpellSpeed()`/`getLeftSlotCount()`/`getRightSlotCount()`/`getCurrentSlotCount()` 均来自 `MagicBookToolNBT`，可用于让法术随核心 Stats 与页面插槽动态变化。
5. **MagicBookEventHandler** 订阅 Forge 事件（目前包括 `LivingJumpEvent` 与 `LivingAttackEvent`），把持书玩家转交给 `handleEvent`，再由 `UnifiedMagicPage` 查找注册了对应事件的法术并调用 `executeSpellWithRawIndex`。
6. **MagicBook** 本体管理冷却计时（`TAG_COOLDOWNS`）、客户端同步、图标渲染、`player.swingArm` 动画与 `SpellOverlayRenderer` 渲染数据，只要 blueprint + `AbstractSpell` 实现没问题，视觉与逻辑就保持一致。

## TriggerSource 与 SpellContext
- `TriggerSource.Type` 目前只有 `LEFT_CLICK`（左键近战）、`RIGHT_CLICK`（右键释放）与 `TICK`（循环被动），`TriggerSource.event(Event)` 可把任意 Forge 事件包装进 `SpellContext`。`canTriggerInternal` 可通过 `context.trigger.isType`/`context.trigger.isEvent`/`context.slot`/`context.trigger.getEvent()` 精细区分按键/事件来源。
- `SpellContext` 中的 `pageData` 是当前页面的 NBT，开发者可用它跨帧保存状态，`target` 只有在近战触发时才有实体，`bookStack` 是魔导书本体。`getRange/getCritChance/getSpellSpeed` 反映核心 Stats、`getLeftSlotCount/getRightSlotCount/getCurrentSlotCount` 允许按插槽数调整威力或反馈。
- **动态图标**：`UnifiedMagicPage` 在 HUD 渲染与 `SpellOverlayRenderer` 中会调用 `ISpell.getDisplayIcon(NBTTagCompound pageData, int rawIndex)`（默认返回 `getIcon()`）。你可以在 `executeInternal`/`onEvent` 里把 `pageData` 某个 key（比如 `adaptive_guard_icon`）写入不同状态，再在 override 的 `getDisplayIcon` 里根据 `pageData` 内容返回不同的 `ResourceLocation`，让 GUI 在事件触发后即时切换图标。

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
2. `Builder.build()` 会自动产生 `UnifiedMagicPage`、把当前页面与左右法术列表注册到 `SpellRegistry`，开发时可通过 `SpellRegistry.getRegisteredPages()` 验证。页面构造完成后在 `SpecialWeapons.initItems` 或对应模块里 `event.getRegistry().register(new YourPage())`。
3. 如果页面包含主动/被动混合法术，Builder 还能同时挂载 `selectable(false)` 的被动法术，它们会在元数据里显示为“被动”，HUD 仍然能渲染冷却/图标。

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
