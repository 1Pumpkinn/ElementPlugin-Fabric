package net.saturn.elementpluginfabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.saturn.elementpluginfabric.network.AbilityPacket;
import org.lwjgl.glfw.GLFW;

public class ElementpluginfabricClient implements ClientModInitializer {

    private static KeyMapping ability1Key;
    private static KeyMapping ability2Key;

    @Override
    public void onInitializeClient() {
        // REMOVED: PayloadTypeRegistry registration - this is already done in AbilityPacket.register()

        // Create custom category for abilities
        KeyMapping.Category category = KeyMapping.Category.register(
                ResourceLocation.fromNamespaceAndPath("elementplugin", "abilities")
        );

        // Register keybinds
        ability1Key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.elementplugin.ability1",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                category
        ));

        ability2Key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.elementplugin.ability2",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                category
        ));

        // Handle keybind presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (ability1Key.consumeClick()) {
                if (client.getConnection() != null) {
                    ClientPlayNetworking.send(new AbilityPacket(1));
                }
            }

            while (ability2Key.consumeClick()) {
                if (client.getConnection() != null) {
                    ClientPlayNetworking.send(new AbilityPacket(2));
                }
            }
        });
    }
}