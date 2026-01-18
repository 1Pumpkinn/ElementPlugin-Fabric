package net.saturn.elementpluginfabric.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.ElementType;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side GUI for selecting elements.
 * Uses a standard 9-slot chest menu.
 */
public class ElementSelectionGUI {

    private static final Map<Integer, ElementType> SLOT_TO_ELEMENT = new HashMap<>();

    static {
        SLOT_TO_ELEMENT.put(0, ElementType.AIR);
        SLOT_TO_ELEMENT.put(1, ElementType.WATER);
        SLOT_TO_ELEMENT.put(2, ElementType.FIRE);
        SLOT_TO_ELEMENT.put(3, ElementType.EARTH);
        SLOT_TO_ELEMENT.put(4, ElementType.LIFE);
        SLOT_TO_ELEMENT.put(5, ElementType.DEATH);
        SLOT_TO_ELEMENT.put(6, ElementType.METAL);
        SLOT_TO_ELEMENT.put(7, ElementType.FROST);
    }

    public static void open(ServerPlayer player) {
        SimpleContainer container = new SimpleContainer(9);
        
        // Fill container with element icons
        container.setItem(0, createIcon(Items.FEATHER, "§fAir Element", "§7Master the winds"));
        container.setItem(1, createIcon(Items.WATER_BUCKET, "§bWater Element", "§7Control the tides"));
        container.setItem(2, createIcon(Items.FLINT_AND_STEEL, "§cFire Element", "§7Wield the flames"));
        container.setItem(3, createIcon(Items.GRASS_BLOCK, "§2Earth Element", "§7Shape the world"));
        container.setItem(4, createIcon(Items.TOTEM_OF_UNDYING, "§aLife Element", "§7Nurture and heal"));
        container.setItem(5, createIcon(Items.WITHER_SKELETON_SKULL, "§8Death Element", "§7Command the shadows"));
        container.setItem(6, createIcon(Items.IRON_INGOT, "§7Metal Element", "§7Forged in steel"));
        container.setItem(7, createIcon(Items.ICE, "§3Frost Element", "§7Absolute zero"));

        player.openMenu(new SimpleMenuProvider((syncId, playerInventory, p) -> 
            new ElementMenu(syncId, playerInventory, container), 
            Component.literal("Select Your Element")
        ));
    }

    private static ItemStack createIcon(net.minecraft.world.item.Item item, String name, String lore) {
        ItemStack stack = new ItemStack(item);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(name));
        //Lore is a bit more complex in 1.20.5+, but for simplicity we just set the name
        return stack;
    }

    private static class ElementMenu extends ChestMenu {
        public ElementMenu(int syncId, Inventory playerInventory, SimpleContainer container) {
            super(MenuType.GENERIC_9x1, syncId, playerInventory, container, 1);
        }

        @Override
        public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
            // Check if it's one of our slots
            if (slotIndex >= 0 && slotIndex < 8 && player instanceof ServerPlayer serverPlayer) {
                ElementType element = SLOT_TO_ELEMENT.get(slotIndex);
                if (element != null) {
                    ElementPluginFabric.getInstance().getElementManager().assignElement(serverPlayer, element);
                    serverPlayer.closeContainer();
                }
            }
            // Prevent taking items out
            if (slotIndex >= 0 && slotIndex < 9) {
                // Return early to prevent default behavior
                return;
            }
            super.clicked(slotIndex, button, actionType, player);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
