package net.saturn.elementpluginfabric.managers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.items.AdvancedRerollerItem;
import net.saturn.elementpluginfabric.items.RerollerItem;
import net.saturn.elementpluginfabric.items.Upgrader1Item;
import net.saturn.elementpluginfabric.items.Upgrader2Item;

public class ItemManager {
    @SuppressWarnings("unused")
    private final ElementPluginFabric plugin;
    @SuppressWarnings("unused")
    private final ManaManager mana;
    @SuppressWarnings("unused")
    private final ConfigManager configManager;
    
    // Item instances - these will be registered in ItemRegistry
    public static final net.minecraft.world.item.Item UPGRADER_1 = new Upgrader1Item(new net.minecraft.world.item.Item.Properties());
    public static final net.minecraft.world.item.Item UPGRADER_2 = new Upgrader2Item(new net.minecraft.world.item.Item.Properties());
    public static final net.minecraft.world.item.Item REROLLER = new RerollerItem(new net.minecraft.world.item.Item.Properties());
    public static final net.minecraft.world.item.Item ADVANCED_REROLLER = new AdvancedRerollerItem(new net.minecraft.world.item.Item.Properties());

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

