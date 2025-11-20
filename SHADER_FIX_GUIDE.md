# TConGreedyAddon - 材质着色器修复快速指南

## 问题
PlusTiC 和 Tinkers-Evolution 的自定义部件类型（laser_medium、battery_cell、tconevo.magic）无法正确显示 CraftTweaker 注册材料的着色器，只显示默认白色材质。

## 解决方案
TConGreedyAddon 通过 Mixin 修复了这个问题，让这些自定义部件也能正确显示所有材料的彩色纹理。

## 使用方法

### 1. 添加依赖关系
确保你的模组包加载顺序正确（在 `mods.toml` 或 `@Mod` 注解中）：
```java
dependencies = "after:tconstruct;after:plustic;after:tconevo"
```

### 2. 构建模组
```bash
gradlew build
```

### 3. 查看效果
启动游戏后，在日志中查找：
```
[TConGreedyAddon/MaterialShaderFix]: ======= Material Shader Fix Summary =======
[TConGreedyAddon/MaterialShaderFix]: Total materials registered: XX
[TConGreedyAddon/MaterialShaderFix]: Materials benefiting from shader fix: XX
```

### 4. 测试材料
创建一个 CraftTweaker 脚本（如 `scripts/test_materials.zs`）：
```zenscript
#loader contenttweaker
import mods.contenttweaker.tconstruct.Material;
import mods.contenttweaker.tconstruct.MaterialBuilder;

// 创建一个测试材料
val testMat = MaterialBuilder.create("test_material");
testMat.color = 0xFF5733; // 橙红色
testMat.craftable = true;
testMat.castable = true;
testMat.addHeadMaterialStats(100, 5.0, 4.0, 2); // 添加标准 head stats
testMat.register();
```

现在这个材料将会在：
- ✅ PlusTiC 的激光导体部件上显示橙红色
- ✅ PlusTiC 的电池单元部件上显示橙红色
- ✅ Tinkers-Evolution 的魔法聚焦部件上显示橙红色

## 调试

### 查看特定材料详情
在 PostInit 中添加：
```java
MaterialRenderingDebugHelper.logMaterialDetails("material_name");
```

### 示例输出
```
[TConGreedyAddon]: === Material Details: Iron ===
[TConGreedyAddon]: Identifier: iron
[TConGreedyAddon]: Custom Stats:
[TConGreedyAddon]:   - laser_medium: NO
[TConGreedyAddon]:   - battery_cell: NO
[TConGreedyAddon]:   - tconevo.magic: NO
[TConGreedyAddon]: Standard Stats:
[TConGreedyAddon]:   - head: YES
[TConGreedyAddon]:   - handle: YES
[TConGreedyAddon]:   - extra: YES
[TConGreedyAddon]: Shader Fix Applies: YES - Will render on custom parts
```

## 技术说明

### 修复原理
- **原始行为**: 只有具有 `laser_medium` 等自定义 stats 的材料才能在对应部件上生成着色器
- **修复后**: 具有任何标准 stats（head/handle/extra）的材料也可以在自定义部件上生成着色器
- **注意**: 这只影响**渲染**，材料仍然不能实际用于制作工具（除非添加了对应的自定义 stats）

### 支持的部件类型
- `laser_medium` - PlusTiC 激光枪导体
- `battery_cell` - PlusTiC 激光枪电池
- `tconevo.magic` - Tinkers-Evolution 魔法属性

### 如何添加新的自定义部件类型
编辑 `MixinToolPart.java` 中的 `CUSTOM_STAT_TYPES`：
```java
private static final Set<String> CUSTOM_STAT_TYPES = new HashSet<>(Arrays.asList(
    "laser_medium",
    "battery_cell",
    "tconevo.magic",
    "your_custom_type_here"  // 添加你的自定义类型
));
```

## 常见问题

### Q: 修复后材料还是显示白色？
A: 检查：
1. MixinBooter 是否正确加载（查看日志中的 "Mixin" 相关信息）
2. 材料是否至少有一个标准 stat（head/handle/extra 等）
3. 查看调试日志确认材料是否在修复列表中

### Q: 可以在已有存档中使用吗？
A: 可以！这个修复只影响渲染，不影响游戏数据。

### Q: 会影响性能吗？
A: 几乎没有影响。只在材质生成阶段（游戏启动时）进行额外检查。

### Q: 与其他模组兼容吗？
A: 是的。这个 Mixin 不会破坏原有功能，只是扩展了材料渲染的兼容性。

## 更多信息

详细技术说明请参考 [MIXIN_README.md](MIXIN_README.md)

## 反馈

如遇到问题，请提供：
1. 完整的日志文件
2. 使用的模组列表和版本
3. MaterialRenderingDebugHelper 的输出
