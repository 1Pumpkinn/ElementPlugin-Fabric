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
    public static final net.minecraft.world.item.Item UPGRADER_I = ItemRegistry.UPGRADER_I;
    public static final net.minecraft.world.item.Item UPGRADER_II = ItemRegistry.UPGRADER_II;
    public static final net.minecraft.world.item.Item REROLLER = ItemRegistry.REROLLER;
    public static final net.minecraft.world.item.Item ADVANCED_REROLLER = ItemRegistry.ADVANCED_REROLLER;
    public static final net.minecraft.world.item.Item ELEMENT_CORE = ItemRegistry.ELEMENT_CORE;

    public ItemManager(ElementPluginFabric plugin, ManaManager mana, ConfigManager configManager) {
        this.plugin = plugin;
        this.mana = mana;
        this.configManager = configManager;
    }
    
    /**
     * Creates an Upgrader I item
     * @return The created ItemStack
     */
    public net.minecraft.world.item.ItemStack createUpgraderI() {
        return new net.minecraft.world.item.ItemStack(UPGRADER_I);
    }
    
    /**
     * Creates an Upgrader II item
     * @return The created ItemStack
     */
    public net.minecraft.world.item.ItemStack createUpgraderII() {
        return new net.minecraft.world.item.ItemStack(UPGRADER_II);
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

    /**
     * Creates an Element Core item
     * @return The created ItemStack
     */
    public net.minecraft.world.item.ItemStack createElementCore() {
        return new net.minecraft.world.item.ItemStack(ELEMENT_CORE);
    }

    public boolean isUpgraderI(ItemStack stack) {
        return stack != null && stack.is(UPGRADER_I);
    }

    public boolean isUpgraderII(ItemStack stack) {
        return stack != null && stack.is(UPGRADER_II);
    }

    public boolean isReroller(ItemStack stack) {
        return stack != null && stack.is(REROLLER);
    }

    public boolean isAdvancedReroller(ItemStack stack) {
        return stack != null && stack.is(ADVANCED_REROLLER);
    }

    public boolean isElementCore(ItemStack stack) {
        return stack != null && stack.is(ELEMENT_CORE);
    }
}

