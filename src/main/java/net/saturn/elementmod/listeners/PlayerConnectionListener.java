package net.saturn.elementmod.listeners;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.data.PlayerData;
import net.saturn.elementmod.managers.ElementManager;

import java.util.UUID;

public class PlayerConnectionListener {
    private final ElementMod plugin;

    public PlayerConnectionListener(ElementMod plugin) {
        this.plugin = plugin;
    }

    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();
            
            // Load data when player joins
            PlayerData pd = plugin.getDataStore().getPlayerData(uuid);
            
            // Apply passive effects if needed (some are handled in tick listeners, 
            // but we might want to re-apply things like attributes here if we had any)
            ElementManager elementManager = plugin.getElementManager();
            if (pd.getCurrentElement() != null) {
                // Ensure upsides are applied (e.g., if they were lost during disconnect)
                elementManager.applyUpsides(player);
            }
            
            ElementMod.LOGGER.info("Loaded element data for player: " + player.getName().getString());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();
            
            // Save data when player leaves
            PlayerData pd = plugin.getDataStore().getPlayerData(uuid);
            plugin.getDataStore().save(pd);
            
            // Clean up temporary data if necessary
            plugin.getElementManager().cancelRolling(player);
            
            ElementMod.LOGGER.info("Saved element data for player: " + player.getName().getString());
        });
    }

    public static void register(ElementMod plugin) {
        new PlayerConnectionListener(plugin).register();
    }
}
