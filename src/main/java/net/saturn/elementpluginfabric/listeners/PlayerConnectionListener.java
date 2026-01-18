package net.saturn.elementpluginfabric.listeners;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.data.PlayerData;
import net.saturn.elementpluginfabric.managers.ElementManager;

import java.util.UUID;

public class PlayerConnectionListener {
    private final ElementPluginFabric plugin;

    public PlayerConnectionListener(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            UUID uuid = player.getUUID();
            
            // Load data when player joins
            PlayerData pd = plugin.getDataStore().getPlayerData(uuid);
            
            // Apply passive effects if needed (some are handled in tick listeners, 
            // but we might want to re-apply things like attributes here if we had any)
            ElementManager elementManager = plugin.getElementManager();
            if (pd.getCurrentElement() != null) {
                // Ensure upsides are applied (e.g., if they were lost during disconnect)
                elementManager.applyUpsides(player);
            }
            
            ElementPluginFabric.LOGGER.info("Loaded element data for player: " + player.getName().getString());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            UUID uuid = player.getUUID();
            
            // Save data when player leaves
            PlayerData pd = plugin.getDataStore().getPlayerData(uuid);
            plugin.getDataStore().save(pd);
            
            // Clean up temporary data if necessary
            plugin.getElementManager().cancelRolling(player);
            
            ElementPluginFabric.LOGGER.info("Saved element data for player: " + player.getName().getString());
        });
    }

    public static void register(ElementPluginFabric plugin) {
        new PlayerConnectionListener(plugin).register();
    }
}
