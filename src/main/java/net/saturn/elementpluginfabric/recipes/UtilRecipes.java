package net.saturn.elementpluginfabric.recipes;

import net.saturn.elementpluginfabric.ElementPluginFabric;

/**
 * Handles recipe registration and management
 */
public class UtilRecipes {
    
    public static void register(ElementPluginFabric plugin) {
        // In Fabric, recipes are typically handled via JSON files in data/elementplugin/recipe/
        // This class can be used for any code-based recipe logic if needed in the future.
        ElementPluginFabric.LOGGER.info("Recipe registration handled via JSON data files.");
    }
}
