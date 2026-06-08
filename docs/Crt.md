# CraftTweaker API

## Material Stats

```zenscript
mods.tcongreedy.TConGreedy.setBookPageStats(materialId as string, leftSlots as int, rightSlots as int, spellSpeed as int);
mods.tcongreedy.TConGreedy.setMagicCoreStats(materialId as string, range as float, critChance as float);
```

- `setBookPageStats`：给指定材料添加魔法书页面部件属性。
- `setMagicCoreStats`：给指定材料添加魔法核心部件属性。

## TicTool

入口类：

```zenscript
mods.tcongreedy.TicTool
```

这组 API 用 Java 原生 NBT/TiC 操作替代旧脚本库。方法不会兼容旧 `TicLib`/`TicTraitLib` 命名，只提供这一套统一入口。

### 工具与护甲查询

```zenscript
mods.tcongreedy.TicTool.isTool(stack as IItemStack) as bool
mods.tcongreedy.TicTool.getAllItems() as IItemStack[]
mods.tcongreedy.TicTool.isArmor(stack as IItemStack) as bool
mods.tcongreedy.TicTool.getArmorType(stack as IItemStack) as string
mods.tcongreedy.TicTool.getArmorSlot(stack as IItemStack) as IEntityEquipmentSlot
mods.tcongreedy.TicTool.getMaterials(stack as IItemStack) as string[]
```

- `isTool`：判断是否是 TiC 工具或本项目 TiC 工具。
- `getAllItems`：返回当前已知 TiC 工具与 ConArm 护甲物品。
- `isArmor`：判断是否是 ConArm 匠魂护甲。
- `getArmorType`：返回 `helmet`、`chestplate`、`leggings`、`boots`，非匠魂护甲返回空字符串。
- `getArmorSlot`：返回对应装备槽，非匠魂护甲返回 `null`。
- `getMaterials`：读取 `TinkerData.Materials`，非匠魂物品返回空数组。

### 词条查询与修改

```zenscript
mods.tcongreedy.TicTool.getTraits(stack as IItemStack) as string[]
mods.tcongreedy.TicTool.hasTrait(stack as IItemStack, traitId as string) as bool
mods.tcongreedy.TicTool.getTraitColor(stack as IItemStack, traitId as string) as int
mods.tcongreedy.TicTool.getTraitLevel(stack as IItemStack, traitId as string) as int
mods.tcongreedy.TicTool.addTrait(stack as IItemStack, traitId as string, color as int, level as int) as bool
mods.tcongreedy.TicTool.removeTrait(stack as IItemStack, traitId as string) as bool
mods.tcongreedy.TicTool.withTrait(stack as IItemStack, traitId as string, color as int, level as int) as IItemStack
mods.tcongreedy.TicTool.withoutTrait(stack as IItemStack, traitId as string) as IItemStack
```

- `getTraits`：读取工具 `Traits` 列表；保留重复词条。
- `hasTrait`：判断工具是否拥有指定词条。
- `getTraitColor`：从 `Modifiers` 中读取词条颜色；不存在返回 `0xffffff`。
- `getTraitLevel`：从 `Modifiers` 中读取词条等级；不存在返回 `1`。
- `addTrait`：修改传入物品，向 `Modifiers`、`Traits` 和存在时的 `TinkerData.Modifiers` 写入词条。
- `removeTrait`：修改传入物品，从上述 NBT 中移除词条。
- `withTrait`：返回添加词条后的物品副本，不修改原物品。
- `withoutTrait`：返回移除词条后的物品副本，不修改原物品。

### 属性修改

```zenscript
mods.tcongreedy.TicTool.setBroken(stack as IItemStack, broken as bool) as bool
mods.tcongreedy.TicTool.addMiningSpeed(stack as IItemStack, amount as float, token as string) as bool
mods.tcongreedy.TicTool.addAttack(stack as IItemStack, amount as float, token as string) as bool
mods.tcongreedy.TicTool.addFreeModifiers(stack as IItemStack, amount as int, token as string) as bool
mods.tcongreedy.TicTool.addDefense(stack as IItemStack, amount as float, token as string) as bool
mods.tcongreedy.TicTool.addToughness(stack as IItemStack, amount as float, token as string) as bool
mods.tcongreedy.TicTool.addHarvestLevel(stack as IItemStack, amount as int, token as string) as bool
mods.tcongreedy.TicTool.addDrawSpeed(stack as IItemStack, amount as float, token as string) as bool
mods.tcongreedy.TicTool.addAttackSpeedMultiplier(stack as IItemStack, amount as float, token as string) as bool
```

- `setBroken`：设置 `Stats.Broken` 为 `1` 或 `0`。
- `addMiningSpeed`：增加 `Stats.MiningSpeed`。
- `addAttack`：增加 `Stats.Attack`。
- `addFreeModifiers`：增加 `Stats.FreeModifiers`。
- `addDefense`：增加 ConArm 护甲防御；按部位换算到内部 `Stats.Defense`。
- `addToughness`：增加 `Stats.Toughness`。
- `addHarvestLevel`：增加 `Stats.HarvestLevel`。
- `addDrawSpeed`：降低 `Stats.DrawSpeed`，最低为 `0.01`。
- `addAttackSpeedMultiplier`：增加 `Stats.AttackSpeedMultiplier`。

`token` 是一次性应用标记。相同物品上相同 token 只会生效一次，适合在事件中防止属性重复叠加。

### 玩家护甲词条缓存

```zenscript
mods.tcongreedy.TicTool.getArmorTraits(player as IPlayer) as string[]
mods.tcongreedy.TicTool.getArmorSlotTraits(player as IPlayer, slotName as string) as string[]
mods.tcongreedy.TicTool.hasArmorTrait(player as IPlayer, traitId as string) as bool
mods.tcongreedy.TicTool.hasArmorSlotTrait(player as IPlayer, slotName as string, traitId as string) as bool
mods.tcongreedy.TicTool.refreshArmorCache(player as IPlayer) as bool
```

- `getArmorTraits`：返回玩家四件匠魂护甲的词条合集。
- `getArmorSlotTraits`：返回指定槽位匠魂护甲的词条。
- `hasArmorTrait`：判断四件匠魂护甲中是否存在某词条。
- `hasArmorSlotTrait`：判断指定槽位匠魂护甲中是否存在某词条。
- `refreshArmorCache`：手动刷新玩家护甲词条缓存。

`slotName` 支持：

- `head` / `helmet`
- `chest` / `chestplate`
- `legs` / `leggings`
- `feet` / `boots`

性能说明：

- 玩家登录时会预先解析四件护甲并建立缓存。
- 穿脱或替换护甲时会自动刷新缓存。
- 查询时如果缓存缺失，会自动补建一次缓存。
- 普通查询只读缓存中的数组或集合，适合在 tick 事件中高频调用。
- 玩家混穿普通非匠魂护甲时，非匠魂护甲槽位视为空槽，不会解析或返回其词条。

示例：

```zenscript
if (mods.tcongreedy.TicTool.hasArmorTrait(player, "speed")) {
    print("player has speed on Tinkers armor");
}

val chestTraits = mods.tcongreedy.TicTool.getArmorSlotTraits(player, "chest");
```
