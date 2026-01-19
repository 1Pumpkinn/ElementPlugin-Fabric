package net.saturn.elementmod.items;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.items.custom.*;

import java.util.function.Function;

public class ModItems {
    
    // Item instances
    public static final Item UPGRADER_I = registerItem("upgrader_i", UpgraderIItem::new);
    public static final Item UPGRADER_II = registerItem("upgrader_ii", UpgraderIIItem::new);
    public static final Item REROLLER = registerItem("reroller", RerollerItem::new);
    public static final Item ADVANCED_REROLLER = registerItem("advanced_reroller", AdvancedRerollerItem::new);
    public static final Item ELEMENT_CORE = registerItem("element_core", ElementCoreItem::new);

    private static Item registerItem(String name, Function<Item.Settings, Item> function) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("elementmod", name));
        return Registry.register(Registries.ITEM, key, function.apply(new Item.Settings().registryKey(key)));
    }

    public static void registerModItems() {
        ElementMod.LOGGER.info("Registering Mod Items for " + ElementMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(UPGRADER_I);
            entries.add(UPGRADER_II);
            entries.add(REROLLER);
            entries.add(ADVANCED_REROLLER);
            entries.add(ELEMENT_CORE);
        });
    }
}

