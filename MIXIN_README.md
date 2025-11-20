# TConGreedyAddon Mixin 修复说明

## 问题背景

PlusTiC 和 Tinkers-Evolution 模组添加了自定义的匠魂部件类型：
- **PlusTiC**: `laser_medium` (激光导体) 和 `battery_cell` (电池单元)
- **Tinkers-Evolution**: `tconevo.magic` (魔法属性)

然而，这些新部件类型无法正确识别由 CraftTweaker 等模组注册的额外材料的着色器，导致只显示默认材质而非材料特定的彩色纹理。

## 问题原因

### 工作流程

1. **材质生成** (`CustomTextureCreator.createMaterialTextures()`)
   - 遍历所有已注册的基础纹理
   - 对每个纹理，检查哪些材料可以使用该纹理
   - 调用 `IToolPart.canUseMaterialForRendering(material)` 判断
   - 只为可用的材料生成着色器纹理

2. **材料检查** (`ToolPart.canUseMaterial(mat)`)
   ```java
   public boolean canUseMaterial(Material mat) {
       for(ToolCore tool : TinkerRegistry.getTools()) {
           for(PartMaterialType pmt : tool.getRequiredComponents()) {
               if(pmt.isValid(this, mat)) {
                   return true;
               }
           }
       }
       return false;
   }
   ```
   - 遍历所有已注册的工具
   - 检查该部件是否在工具的所需组件中
   - 检查材料是否有该部件类型所需的材质统计 (stats)

3. **材质统计检查** (`PartMaterialType.isValidMaterial(mat)`)
   ```java
   public boolean isValidMaterial(Material material) {
       for(String type : neededTypes) {
           if(!material.hasStats(type)) {
               return false;
           }
       }
       return true;
   }
   ```

### 核心问题

当 CraftTweaker 等模组注册额外材料时：
- ✅ **会添加**: 原版匠魂的标准材质统计 (`head`, `handle`, `extra` 等)
- ❌ **不会添加**: 自定义材质统计 (`laser_medium`, `battery_cell`, `tconevo.magic`)

因此：
1. `material.hasStats("laser_medium")` → `false`
2. `isValidMaterial()` → `false`
3. `canUseMaterialForRendering()` → `false`
4. `CustomTextureCreator` 不生成着色器纹理
5. 只显示默认材质，无法识别材料着色器

## 解决方案

### Mixin 实现

通过 Mixin 重写 `ToolPart.canUseMaterialForRendering()` 方法：

```java
@Overwrite(remap = false)
public boolean canUseMaterialForRendering(Material mat) {
    // 1. 优先检查材料是否有所需的自定义统计（正常使用）
    if (this.canUseMaterial(mat)) {
        return true;
    }
    
    // 2. 对于使用自定义统计类型的部件，允许任何有标准统计的材料用于渲染
    if (this.usesCustomStatType()) {
        return this.hasAnyStandardStat(mat);
    }
    
    // 3. 标准部件使用原始行为
    return false;
}
```

### 工作原理

1. **识别自定义部件**: 检查部件是否用于需要自定义统计类型的工具
2. **标准统计检查**: 检查材料是否有任何标准 TConstruct 统计
3. **允许渲染**: 即使材料没有自定义统计，只要有标准统计就允许生成着色器

### 效果

- ✅ **有自定义统计的材料**: 正常工作（如模组自带材料）
- ✅ **只有标准统计的材料**: 现在也能生成正确的着色器纹理
- ✅ **CraftTweaker 材料**: 可以正确显示彩色纹理
- ⚠️ **注意**: 这些材料仍然不能实际用于制作工具（因为缺少自定义统计），但**可以正确渲染**

## 支持的自定义统计类型

当前 Mixin 支持以下自定义统计类型：
- `laser_medium` - PlusTiC 激光导体
- `battery_cell` - PlusTiC 电池单元
- `tconevo.magic` - Tinkers-Evolution 魔法属性

如需添加更多，修改 `MixinToolPart.CUSTOM_STAT_TYPES`。

## 技术细节

### 文件结构
```
TConGreedyAddon/
├── gradle.properties (启用 Mixin)
├── src/main/
│   ├── java/com/smd/tcongreedyaddon/mixin/
│   │   └── MixinToolPart.java (Mixin 实现)
│   └── resources/
│       └── mixins.tcongreedyaddon.json (Mixin 配置)
```

### Mixin 配置
```json
{
  "package": "com.smd.tcongreedyaddon.mixin",
  "required": true,
  "refmap": "mixins.tcongreedyaddon.refmap.json",
  "target": "@env(DEFAULT)",
  "minVersion": "0.8.5",
  "compatibilityLevel": "JAVA_8",
  "mixins": ["MixinToolPart"]
}
```

## 兼容性

- **Minecraft**: 1.12.2
- **Tinkers' Construct**: 所有版本
- **PlusTiC**: 所有版本
- **Tinkers-Evolution**: 所有版本
- **MixinBooter**: 10.2+
- **CraftTweaker**: 所有版本

## 构建说明

1. 确保 `gradle.properties` 中 `use_mixins = true`
2. 运行 `gradlew build`
3. Mixin 将在编译时自动应用

## 调试

如需调试 Mixin，在 JVM 参数中添加：
```
-Dmixin.debug.export=true
-Dmixin.debug.verbose=true
```

导出的类文件将位于 `run/.mixin.out/` 目录。

## 贡献者

- TConGreedyAddon Team
- Mixin 实现: AI Assistant

## 许可证

与 TConGreedyAddon 主项目相同
