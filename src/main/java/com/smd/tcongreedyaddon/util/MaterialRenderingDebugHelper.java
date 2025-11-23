package com.smd.tcongreedyaddon.util;

import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for debugging and logging material rendering fixes applied by MixinToolPart.
 * This class provides utilities to identify which materials benefit from the shader fix.
 */
public class MaterialRenderingDebugHelper {
    
    private static final Logger LOGGER = LogManager.getLogger("TConGreedyAddon/MaterialShaderFix");
    
    private static final Set<String> CUSTOM_STAT_TYPES = new HashSet<>(Arrays.asList(
        "laser_medium", "battery_cell", "tconevo.magic", "moretcon.explosive_charge"
    ));
    
    private static final Set<String> STANDARD_STAT_TYPES = new HashSet<>(Arrays.asList(
        "head", "handle", "extra", "bow", "bowstring", "projectile", "shaft", "fletching"
    ));
    
    /**
     * Logs a summary of which materials will benefit from the shader rendering fix.
     * Call this during post-initialization to see the effects.
     */
    public static void logMaterialShaderFixSummary() {
        if (!com.smd.tcongreedyaddon.config.MaterialShaderFixConfig.enableDebugLogging) {
            return;
        }
        
        LOGGER.info("======= Material Shader Fix Summary =======");
        
        Map<String, Set<Material>> customPartToFixedMaterials = new HashMap<>();
        
        // Find all custom parts
        Set<IToolPart> customParts = new HashSet<>();
        for (ToolCore tool : TinkerRegistry.getTools()) {
            for (PartMaterialType pmt : tool.getRequiredComponents()) {
                for (String customType : CUSTOM_STAT_TYPES) {
                    if (pmt.usesStat(customType)) {
                        customParts.addAll(pmt.getPossibleParts());
                        customPartToFixedMaterials.putIfAbsent(customType, new HashSet<>());
                    }
                }
            }
        }
        
        if (customParts.isEmpty()) {
            LOGGER.info("No custom part types detected (PlusTiC/Tinkers-Evolution may not be loaded)");
            LOGGER.info("===========================================");
            return;
        }
        
        // Check each material
        int totalMaterials = 0;
        int fixedMaterials = 0;
        
        for (Material material : TinkerRegistry.getAllMaterials()) {
            totalMaterials++;
            boolean hasCustomStat = false;
            boolean hasStandardStat = false;
            
            // Check what stats this material has
            for (String customType : CUSTOM_STAT_TYPES) {
                if (material.hasStats(customType)) {
                    hasCustomStat = true;
                    break;
                }
            }
            
            for (String standardType : STANDARD_STAT_TYPES) {
                if (material.hasStats(standardType)) {
                    hasStandardStat = true;
                    break;
                }
            }
            
            // Material benefits from the fix if it has standard stats but not custom stats
            if (!hasCustomStat && hasStandardStat) {
                fixedMaterials++;
                
                // Add to each custom stat type's list
                for (String customType : CUSTOM_STAT_TYPES) {
                    customPartToFixedMaterials.get(customType).add(material);
                }
            }
        }
        
        LOGGER.info("Total materials registered: {}", totalMaterials);
        LOGGER.info("Materials benefiting from shader fix: {}", fixedMaterials);
        
        for (Map.Entry<String, Set<Material>> entry : customPartToFixedMaterials.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                LOGGER.info("  [{}] Fixed materials count: {}", entry.getKey(), entry.getValue().size());
                
                // Log first few material names as examples
                List<String> examples = entry.getValue().stream()
                    .limit(5)
                    .map(m -> m.getLocalizedName())
                    .sorted()
                    .collect(Collectors.toList());
                
                if (!examples.isEmpty()) {
                    LOGGER.info("    Examples: {}", String.join(", ", examples));
                    if (entry.getValue().size() > 5) {
                        LOGGER.info("    ... and {} more", entry.getValue().size() - 5);
                    }
                }
            }
        }
        
        LOGGER.info("===========================================");
    }
    
    /**
     * Logs detailed information about a specific material's compatibility with custom parts.
     * Useful for debugging why a specific material may or may not render correctly.
     * 
     * @param materialId the identifier of the material to check
     */
    public static void logMaterialDetails(String materialId) {
        Material material = TinkerRegistry.getMaterial(materialId);
        if (material == Material.UNKNOWN) {
            LOGGER.warn("Material '{}' not found!", materialId);
            return;
        }
        
        LOGGER.info("=== Material Details: {} ===", material.getLocalizedName());
        LOGGER.info("Identifier: {}", material.identifier);
        
        // Check for custom stats
        LOGGER.info("Custom Stats:");
        boolean hasAnyCustomStat = false;
        for (String customType : CUSTOM_STAT_TYPES) {
            boolean has = material.hasStats(customType);
            LOGGER.info("  - {}: {}", customType, has ? "YES" : "NO");
            if (has) hasAnyCustomStat = true;
        }
        
        // Check for standard stats
        LOGGER.info("Standard Stats:");
        boolean hasAnyStandardStat = false;
        for (String standardType : STANDARD_STAT_TYPES) {
            boolean has = material.hasStats(standardType);
            LOGGER.info("  - {}: {}", standardType, has ? "YES" : "NO");
            if (has) hasAnyStandardStat = true;
        }
        
        // Determine if fix applies
        boolean fixApplies = !hasAnyCustomStat && hasAnyStandardStat;
        LOGGER.info("Shader Fix Applies: {}", fixApplies ? "YES - Will render on custom parts" : "NO");
        
        if (fixApplies) {
            LOGGER.info("This material will now render correctly on:");
            LOGGER.info("  - PlusTiC laser medium parts");
            LOGGER.info("  - PlusTiC battery cell parts");
            LOGGER.info("  - Tinkers-Evolution magic parts");
        }
        
        LOGGER.info("=====================================");
    }
}
