package net.saturn.elementmod.managers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.items.ModItems;

public class ItemManager {
    @SuppressWarnings("unused")
    private final ElementMod plugin;
    @SuppressWarnings("unused")
    private final ManaManager mana;
    @SuppressWarnings("unused")
    private final ConfigManager configManager;
    
    // Reference to registered items from ItemRegistry
    public static final Item UPGRADER_I = ModItems.UPGRADER_I;
    public static final Item UPGRADER_II = ModItems.UPGRADER_II;
    public static final Item REROLLER = ModItems.REROLLER;
    public static final Item ADVANCED_REROLLER = ModItems.ADVANCED_REROLLER;
    public static final Item ELEMENT_CORE = ModItems.ELEMENT_CORE;

    public ItemManager(ElementMod plugin, ManaManager mana, ConfigManager configManager) {
        this.plugin = plugin;
        this.mana = mana;
        this.configManager = configManager;
    }
    
    /**
     * Creates an Upgrader I item
     * @return The created ItemStack
     */
    public ItemStack createUpgraderI() {
        return new ItemStack(UPGRADER_I);
    }
    
    /**
     * Creates an Upgrader II item
     * @return The created ItemStack
     */
    public ItemStack createUpgraderII() {
        return new ItemStack(UPGRADER_II);
    }
    
    /**
     * Creates a Reroller item
     * @return The created ItemStack
     */
    public ItemStack createReroller() {
        return new ItemStack(REROLLER);
    }
    
    /**
     * Creates an Advanced Reroller item
     * @return The created ItemStack
     */
    public ItemStack createAdvancedReroller() {
        return new ItemStack(ADVANCED_REROLLER);
    }

    /**
     * Creates an Element Core item
     * @return The created ItemStack
     */
    public ItemStack createElementCore() {
        return new ItemStack(ELEMENT_CORE);
    }

    public boolean isUpgraderI(ItemStack stack) {
        return stack != null && stack.isOf(UPGRADER_I);
    }

    public boolean isUpgraderII(ItemStack stack) {
        return stack != null && stack.isOf(UPGRADER_II);
    }

    public boolean isReroller(ItemStack stack) {
        return stack != null && stack.isOf(REROLLER);
    }

    public boolean isAdvancedReroller(ItemStack stack) {
        return stack != null && stack.isOf(ADVANCED_REROLLER);
    }

    public boolean isElementCore(ItemStack stack) {
        return stack != null && stack.isOf(ELEMENT_CORE);
    }
}

