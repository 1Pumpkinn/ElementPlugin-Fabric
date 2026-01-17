package net.saturn.elementpluginfabric.items;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.managers.ItemManager;

public class ItemRegistry {
    
    public static void register() {
        registerItem("upgrader_1", ItemManager.UPGRADER_1);
        registerItem("upgrader_2", ItemManager.UPGRADER_2);
        registerItem("reroller", ItemManager.REROLLER);
        registerItem("advanced_reroller", ItemManager.ADVANCED_REROLLER);
        
        // Add items to creative inventory
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(ItemManager.UPGRADER_1);
            entries.accept(ItemManager.UPGRADER_2);
            entries.accept(ItemManager.REROLLER);
            entries.accept(ItemManager.ADVANCED_REROLLER);
        });
        
        ElementPluginFabric.LOGGER.info("Registered custom items");
    }
    
    private static void registerItem(String name, Item item) {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("elementplugin", name), item);
    }
}

