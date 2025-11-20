package com.smd.tcongreedyaddon.mixin;

import com.smd.tcongreedyaddon.config.MaterialShaderFixConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.TinkerRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Mixin to fix material rendering for custom part types from PlusTiC and Tinkers-Evolution.
 * 
 * <h2>The Problem:</h2>
 * When CraftTweaker or other mods register materials, they typically only add vanilla TConstruct 
 * stat types (head, handle, extra, etc.) and don't add custom stat types like "laser_medium", 
 * "battery_cell", or "tconevo.magic".
 * 
 * <p>This causes {@link slimeknights.tconstruct.library.tools.ToolPart#canUseMaterial(Material)} 
 * to return false for these materials when checking custom parts, which prevents 
 * {@link slimeknights.tconstruct.library.client.CustomTextureCreator} from generating proper 
 * colored textures for those materials on custom part types.</p>
 * 
 * <h2>The Solution:</h2>
 * We add a concrete implementation of canUseMaterialForRendering to the ToolPart class,
 * overriding the default interface implementation. This allows materials with standard stats 
 * to be used for rendering on custom parts even if they don't have the exact stat type required.
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
     * By implementing this method in the Mixin, it will override the default interface implementation
     * for all ToolPart instances, allowing materials with standard stats to be used for rendering
     * on custom parts even if they don't have the exact stat type required.
     * 
     * Note: This is NOT using @Overwrite because the method doesn't exist in ToolPart class itself.
     * Instead, we're providing a concrete implementation that takes precedence over the interface default.
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
        if (this.canUseMaterial(mat)) {
            return true;
        }
        
        // For parts that use custom stat types, allow rendering with any material that has
        // standard stats. This enables shader generation for CraftTweaker materials.
        if (this.usesCustomStatType()) {
            return this.hasAnyStandardStat(mat);
        }
        
        // For standard parts, use the original behavior
        return false;
    }
    
    /**
     * Determines if this tool part requires any custom (non-vanilla) stat type.
     * Uses the custom stat types defined in the config.
     * 
     * @return true if this part is used in any tool that requires a custom stat type
     */
    private boolean usesCustomStatType() {
        Set<String> customTypes = getCustomStatTypes();
        
        for (ToolCore tool : TinkerRegistry.getTools()) {
            for (PartMaterialType pmt : tool.getRequiredComponents()) {
                // Check if this part material type includes this part
                if (pmt.isValidItem((IToolPart)(Object)this)) {
                    // Check if this PMT requires any custom stat types
                    for (String customType : customTypes) {
                        if (pmt.usesStat(customType)) {
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
