package net.saturn.elementpluginfabric.managers;

import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.minecraft.item.ItemStack;

public class ItemManager {
    @SuppressWarnings("unused")
    private final ElementPluginFabric plugin;
    @SuppressWarnings("unused")
    private final ManaManager mana;
    @SuppressWarnings("unused")
    private final ConfigManager configManager;
    // Note: ElementItem interface needs to be created for Fabric
    // private final Map<ElementType, ElementItem> items = new EnumMap<>(ElementType.class);

    public ItemManager(ElementPluginFabric plugin, ManaManager mana, ConfigManager configManager) {
        this.plugin = plugin;
        this.mana = mana;
        this.configManager = configManager;
    }

    // TODO: Implement item registration and handling once ElementItem interface is created
    /*
    public void register(ElementItem item) {
        items.put(item.getElementType(), item);
        item.registerRecipe(plugin);
    }

    public void handleUse(PlayerInteractEvent e) {
        for (ElementItem item : items.values()) {
            if (item.handleUse(e, plugin, mana, configManager)) {
                return;
            }
        }
    }

    public void handleDamage(EntityDamageByEntityEvent e) {
        for (ElementItem item : items.values()) {
            item.handleDamage(e, plugin);
        }
    }

    public void handleLaunch(ProjectileLaunchEvent e) {
        for (ElementItem item : items.values()) {
            item.handleLaunch(e, plugin, mana, configManager);
        }
    }
    */
    
    /**
     * Creates an Upgrader1 item
     * @return The created ItemStack
     */
    public ItemStack createUpgrader1() {
        // TODO: Implement Upgrader1Item for Fabric
        return ItemStack.EMPTY;
    }
    
    /**
     * Creates an Upgrader2 item
     * @return The created ItemStack
     */
    public ItemStack createUpgrader2() {
        // TODO: Implement Upgrader2Item for Fabric
        return ItemStack.EMPTY;
    }
}

