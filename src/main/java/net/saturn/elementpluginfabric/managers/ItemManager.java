package net.saturn.elementpluginfabric.managers;

import net.minecraft.world.item.ItemStack;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.items.ItemRegistry;

public class ItemManager {
    @SuppressWarnings("unused")
    private final ElementPluginFabric plugin;
    @SuppressWarnings("unused")
    private final ManaManager mana;
    @SuppressWarnings("unused")
    private final ConfigManager configManager;
    
    // Reference to registered items from ItemRegistry
    public static final net.minecraft.world.item.Item UPGRADER_1 = ItemRegistry.UPGRADER_1;
    public static final net.minecraft.world.item.Item UPGRADER_2 = ItemRegistry.UPGRADER_2;
    public static final net.minecraft.world.item.Item REROLLER = ItemRegistry.REROLLER;
    public static final net.minecraft.world.item.Item ADVANCED_REROLLER = ItemRegistry.ADVANCED_REROLLER;

    public ItemManager(ElementPluginFabric plugin, ManaManager mana, ConfigManager configManager) {
        this.plugin = plugin;
        this.mana = mana;
        this.configManager = configManager;
    }
    
    /**
     * Creates an Upgrader1 item
     * @return The created ItemStack
     */
    public net.minecraft.world.item.ItemStack createUpgrader1() {
        return new net.minecraft.world.item.ItemStack(UPGRADER_1);
    }
    
    /**
     * Creates an Upgrader2 item
     * @return The created ItemStack
     */
    public net.minecraft.world.item.ItemStack createUpgrader2() {
        return new net.minecraft.world.item.ItemStack(UPGRADER_2);
    }
    
    /**
     * Creates a Reroller item
     * @return The created ItemStack
     */
    public net.minecraft.world.item.ItemStack createReroller() {
        return new net.minecraft.world.item.ItemStack(REROLLER);
    }
    
    /**
     * Creates an Advanced Reroller item
     * @return The created ItemStack
     */
    public net.minecraft.world.item.ItemStack createAdvancedReroller() {
        return new net.minecraft.world.item.ItemStack(ADVANCED_REROLLER);
    }
}

