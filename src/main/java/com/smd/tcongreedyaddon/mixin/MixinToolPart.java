package com.smd.tcongreedyaddon.mixin;

import com.smd.tcongreedyaddon.config.MaterialShaderFixConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.TinkerRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Mixin to fix material rendering for custom part types from PlusTiC, Tinkers-Evolution, and Moar-TCon.
 * 
 * <h2>The Problem:</h2>
 * When CraftTweaker or other mods register materials, they typically only add vanilla TConstruct 
 * stat types (head, handle, extra, etc.) and don't add custom stat types like "laser_medium", 
 * "battery_cell", "tconevo.magic", or "moretcon.explosive_charge".
 * 
 * <p>This causes {@link slimeknights.tconstruct.library.tools.ToolPart#canUseMaterial(Material)} 
 * to return false for these materials when checking custom parts, which prevents 
 * {@link slimeknights.tconstruct.library.client.CustomTextureCreator} from generating proper 
 * colored textures for those materials on custom part types.</p>
 * 
 * <h2>The Solution:</h2>
 * We override canUseMaterialForRendering() in the ToolPart class using @Overwrite.
 * This allows materials with standard stats to be used for rendering on custom parts 
 * even if they don't have the exact stat type required.
 * 
 * <p>This allows materials registered via CraftTweaker with standard stats to display properly 
 * colored textures on custom parts, even though they can't actually be used to craft tools 
 * with those parts (since they lack the required custom stats).</p>
 * 
 * @author TConGreedyAddon
 * @see slimeknights.tconstruct.library.tools.ToolPart
 * @see slimeknights.tconstruct.library.client.CustomTextureCreator
 * @see MaterialShaderFixConfig
 */
@Mixin(value = slimeknights.tconstruct.library.tools.ToolPart.class, remap = false)
public abstract class MixinToolPart implements IToolPart {
    
    /**
     * Shadow the canUseMaterial method from ToolPart so we can call it.
     */
    @Shadow
    public abstract boolean canUseMaterial(Material mat);
    
    /**
     * Standard TConstruct material stat types that are commonly present in materials.
     * If a material has any of these, it's considered a valid material for rendering purposes.
     */
    private static final Set<String> STANDARD_STAT_TYPES = new HashSet<>(Arrays.asList(
        "head",       // MaterialTypes.HEAD - Tool heads (pickaxe head, axe head, etc.)
        "handle",     // MaterialTypes.HANDLE - Tool handles
        "extra",      // MaterialTypes.EXTRA - Tool extras/binding
        "bow",        // MaterialTypes.BOW - Bow limbs
        "bowstring",  // MaterialTypes.BOWSTRING - Bow strings
        "projectile", // MaterialTypes.PROJECTILE - Arrow heads
        "shaft",      // MaterialTypes.SHAFT - Arrow shafts
        "fletching"   // MaterialTypes.FLETCHING - Arrow fletching
    ));
    
    /**
     * Gets the set of custom stat types from config.
     * Cached as a Set for efficient lookup.
     */
    private static Set<String> getCustomStatTypes() {
        return new HashSet<>(Arrays.asList(MaterialShaderFixConfig.customStatTypes));
    }
    
    /**
     * Override canUseMaterialForRendering from the IToolPart interface.
     * 
     * By implementing this method in the Mixin class, it provides a concrete implementation
     * that takes precedence over the interface's default implementation when ToolPart is loaded.
     * 
     * This method is called by CustomTextureCreator to determine which materials should have
     * colored textures generated for this tool part.
     * 
     * @param mat the material to check
     * @return true if the material can be used for rendering this part
     */
    @Override
    public boolean canUseMaterialForRendering(Material mat) {
        // If the fix is disabled in config, use original behavior (call canUseMaterial)
        if (!MaterialShaderFixConfig.enableShaderFix) {
            return this.canUseMaterial(mat);
        }
        
        // First, check if the material can be used normally (has the required custom stats)
        boolean canUse = this.canUseMaterial(mat);
        if (canUse) {
            return true;
        }
        
        // For parts that use custom stat types, allow rendering with any material that has
        // standard stats. This enables shader generation for CraftTweaker materials.
        boolean usesCustom = this.usesCustomStatType();
        boolean hasStandard = this.hasAnyStandardStat(mat);
        
        // DEBUG logging if enabled
        if (MaterialShaderFixConfig.enableDebugLogging) {
            IToolPart thisPart = (IToolPart)(Object)this;
            String unlocalizedName = thisPart.toString();
            
            // Log for all custom stat type parts
            if (usesCustom && hasStandard && !canUse) {
                System.out.println("[MaterialShaderFix] Enabling " + mat.identifier + 
                    " rendering for custom part: " + unlocalizedName);
            }
        }
        
        if (usesCustom) {
            return hasStandard;
        }
        
        // For standard parts, use the original behavior
        return false;
    }
    
    /**
     * Determines if this tool part requires any custom (non-vanilla) stat type.
     * Uses the custom stat types defined in the config.
     * 
     * IMPORTANT: This method checks if the part's unlocalized name contains any custom stat type identifier.
     * This is more reliable than checking registered tools because some tools may not be registered
     * depending on config settings (e.g., Moar-TCon's Bomb tool depends on enableBomb config).
     * 
     * @return true if this part is likely used for a custom stat type
     */
    private boolean usesCustomStatType() {
        Set<String> customTypes = getCustomStatTypes();
        IToolPart thisPart = (IToolPart)(Object)this;
        
        // Get the part's unlocalized name (e.g., "item.moretcon.explosive_charge.name")
        String unlocalizedName = thisPart.toString();
        
        // Check if the unlocalized name contains any custom stat type identifier
        // This handles cases where the stat type is part of the item name
        for (String customType : customTypes) {
            // Remove namespace prefix (e.g., "moretcon.explosive_charge" -> "explosive_charge")
            String simpleType = customType.contains(".") ? 
                customType.substring(customType.lastIndexOf('.') + 1) : customType;
            
            if (unlocalizedName.contains(simpleType)) {
                if (MaterialShaderFixConfig.enableDebugLogging) {
                    System.out.println("[MaterialShaderFix] Part " + unlocalizedName + 
                        " identified as custom stat type: " + customType);
                }
                return true;
            }
        }
        
        // Fallback: Check registered tools (for parts that don't have the stat type in their name)
        for (ToolCore tool : TinkerRegistry.getTools()) {
            for (PartMaterialType pmt : tool.getRequiredComponents()) {
                if (pmt.isValidItem(thisPart)) {
                    for (String customType : customTypes) {
                        if (pmt.usesStat(customType)) {
                            if (MaterialShaderFixConfig.enableDebugLogging) {
                                System.out.println("[MaterialShaderFix] Part " + unlocalizedName + 
                                    " found in tool " + tool.getIdentifier() + " using custom stat: " + customType);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the material has any of the standard TConstruct stat types.
     * Materials with at least one standard stat are considered valid for rendering.
     * 
     * @param mat the material to check
     * @return true if the material has at least one standard stat type
     */
    private boolean hasAnyStandardStat(Material mat) {
        for (String statType : STANDARD_STAT_TYPES) {
            if (mat.hasStats(statType)) {
                return true;
            }
        }
        return false;
    }
}
