package net.saturn.elementmod.recipes;

import net.saturn.elementmod.ElementMod;

/**
 * Handles recipe registration and management
 */
public class UtilRecipes {
    
    public static void register(ElementMod plugin) {
        // In Fabric, recipes are typically handled via JSON files in data/elementplugin/recipe/
        // This class can be used for any code-based recipe logic if needed in the future.
        ElementMod.LOGGER.info("Recipe registration handled via JSON data files.");
    }
}
