package net.saturn.elementmod.gui;

import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.component.DataComponentTypes;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.ElementType;

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

    public static void open(ServerPlayerEntity player) {
        SimpleInventory inventory = new SimpleInventory(9);
        
        // Fill inventory with element icons
        inventory.setStack(0, createIcon(Items.FEATHER, "§fAir Element", "§7Master the winds"));
        inventory.setStack(1, createIcon(Items.WATER_BUCKET, "§bWater Element", "§7Control the tides"));
        inventory.setStack(2, createIcon(Items.FLINT_AND_STEEL, "§cFire Element", "§7Wield the flames"));
        inventory.setStack(3, createIcon(Items.GRASS_BLOCK, "§2Earth Element", "§7Shape the world"));
        inventory.setStack(4, createIcon(Items.TOTEM_OF_UNDYING, "§aLife Element", "§7Nurture and heal"));
        inventory.setStack(5, createIcon(Items.WITHER_SKELETON_SKULL, "§8Death Element", "§7Command the shadows"));
        inventory.setStack(6, createIcon(Items.IRON_INGOT, "§7Metal Element", "§7Forged in steel"));
        inventory.setStack(7, createIcon(Items.ICE, "§3Frost Element", "§7Absolute zero"));

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, p) -> 
            new ElementMenu(syncId, playerInventory, inventory), 
            Text.literal("Select Your Element")
        ));
    }

    private static ItemStack createIcon(Item item, String name, String lore) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        //Lore is a bit more complex in 1.20.5+, but for simplicity we just set the name
        return stack;
    }

    private static class ElementMenu extends GenericContainerScreenHandler {
        public ElementMenu(int syncId, PlayerInventory playerInventory, SimpleInventory inventory) {
            super(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory, inventory, 1);
        }

        @Override
        public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
            // Check if it's one of our slots
            if (slotIndex >= 0 && slotIndex < 8 && player instanceof ServerPlayerEntity serverPlayer) {
                ElementType element = SLOT_TO_ELEMENT.get(slotIndex);
                if (element != null) {
                    ElementMod.getInstance().getElementManager().assignElement(serverPlayer, element);
                    serverPlayer.closeHandledScreen();
                }
            }
            // Prevent taking items out
            if (slotIndex >= 0 && slotIndex < 9) {
                // Return early to prevent default behavior
                return;
            }
            super.onSlotClick(slotIndex, button, actionType, player);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }
}
