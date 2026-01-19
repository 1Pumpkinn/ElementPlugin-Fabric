package net.saturn.elementmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.saturn.elementmod.network.AbilityPacket;
import org.lwjgl.glfw.GLFW;

public class ElementpluginfabricClient implements ClientModInitializer {

    private static KeyBinding ability1Key;
    private static KeyBinding ability2Key;

    @Override
    public void onInitializeClient() {
        // Register keybinds
        Category category = Category.create(Identifier.of("elementplugin", "main"));

        ability1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementplugin.ability1",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                category
        ));

        ability2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elementplugin.ability2",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                category
        ));

        // Handle keybind presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (ability1Key.wasPressed()) {
                if (client.getNetworkHandler() != null) {
                    ClientPlayNetworking.send(new AbilityPacket(1));
                }
            }

            while (ability2Key.wasPressed()) {
                if (client.getNetworkHandler() != null) {
                    ClientPlayNetworking.send(new AbilityPacket(2));
                }
            }
        });
    }
}