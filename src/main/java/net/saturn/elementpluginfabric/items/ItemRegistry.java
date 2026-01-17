package net.saturn.elementpluginfabric.items;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.saturn.elementpluginfabric.ElementPluginFabric;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ItemRegistry {
    
    // Item instances - created and registered here
    public static final Item UPGRADER_I = registerItem("upgrader_I", UpgraderIItem::new);
    public static final Item UPGRADER_II = registerItem("upgrader_II", UpgraderIIItem::new);
    public static final Item REROLLER = registerItem("reroller", RerollerItem::new);
    public static final Item ADVANCED_REROLLER = registerItem("advanced_reroller", AdvancedRerollerItem::new);
    
    public static void register() {
        ElementPluginFabric.LOGGER.info("Starting item registration...");
        
        // Items are already registered via static field initialization above
        // Force initialization by accessing the fields
        ElementPluginFabric.LOGGER.info("Item UPGRADER_1 registered: {}", BuiltInRegistries.ITEM.getKey(UPGRADER_I));
        ElementPluginFabric.LOGGER.info("Item UPGRADER_2 registered: {}", BuiltInRegistries.ITEM.getKey(UPGRADER_II));
        ElementPluginFabric.LOGGER.info("Item REROLLER registered: {}", BuiltInRegistries.ITEM.getKey(REROLLER));
        ElementPluginFabric.LOGGER.info("Item ADVANCED_REROLLER registered: {}", BuiltInRegistries.ITEM.getKey(ADVANCED_REROLLER));
        
        // Now add them to creative inventory
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(UPGRADER_I);
            entries.accept(UPGRADER_II);
            entries.accept(REROLLER);
            entries.accept(ADVANCED_REROLLER);
        });
        
        ElementPluginFabric.LOGGER.info("Registered custom items successfully");
    }
    
    private static <T extends Item> T registerItem(String name, java.util.function.Function<Item.Properties, T> itemFactory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("elementplugin", name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        
        Item.Properties properties = new Item.Properties();
        
        // Try multiple approaches to set the ResourceKey
        boolean keySet = false;
        
        // Approach 1: Try field named "id"
        try {
            java.lang.reflect.Field idField = Item.Properties.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(properties, key);
            keySet = true;
            ElementPluginFabric.LOGGER.debug("Set ResourceKey using field 'id'");
        } catch (NoSuchFieldException e) {
            // Try other possible field names
            for (String fieldName : new String[]{"f_41374_", "key", "resourceKey", "itemKey"}) {
                try {
                    java.lang.reflect.Field field = Item.Properties.class.getDeclaredField(fieldName);
                    if (field.getType() == ResourceKey.class || field.getType().getTypeName().contains("ResourceKey")) {
                        field.setAccessible(true);
                        field.set(properties, key);
                        keySet = true;
                        ElementPluginFabric.LOGGER.debug("Set ResourceKey using field '{}'", fieldName);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.warn("Failed to set ResourceKey using field reflection: {}", e.getMessage());
        }
        
        // Approach 2: Try method named "id"
        if (!keySet) {
            try {
                Method setIdMethod = Item.Properties.class.getDeclaredMethod("id", ResourceKey.class);
                setIdMethod.setAccessible(true);
                setIdMethod.invoke(properties, key);
                keySet = true;
                ElementPluginFabric.LOGGER.debug("Set ResourceKey using method 'id'");
            } catch (Exception e) {
                ElementPluginFabric.LOGGER.warn("Failed to set ResourceKey using method reflection: {}", e.getMessage());
            }
        }
        
        if (!keySet) {
            ElementPluginFabric.LOGGER.error("CRITICAL: Could not set ResourceKey for item {}. This will cause a crash!", id);
            throw new RuntimeException("Cannot register item without ResourceKey: " + id + ". The Item.Properties class structure may have changed in this Minecraft version.");
        }
        
        // Create the item with the properties that now have the key set
        T item = itemFactory.apply(properties);
        
        // Register the item
        Registry.register(BuiltInRegistries.ITEM, id, item);
        ElementPluginFabric.LOGGER.info("Successfully registered item: {}", id);
        return item;
    }
}

