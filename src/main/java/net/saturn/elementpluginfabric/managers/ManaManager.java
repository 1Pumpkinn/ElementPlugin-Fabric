package net.saturn.elementpluginfabric.managers;

import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.data.DataStore;
import net.saturn.elementpluginfabric.data.PlayerData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManaManager {
    @SuppressWarnings("unused")
    private final ElementPluginFabric plugin;
    private final DataStore store;
    @SuppressWarnings("unused")
    private final ConfigManager configManager;
    private boolean taskRunning = false;

    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public ManaManager(ElementPluginFabric plugin, DataStore store, ConfigManager configManager) {
        this.plugin = plugin;
        this.store = store;
        this.configManager = configManager;
    }

    public void start() {
        if (taskRunning) return;
        taskRunning = true;
        
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!taskRunning) return;
            
            int maxMana = configManager.getMaxMana();
            int regenRate = configManager.getManaRegenPerSecond();

            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                PlayerData pd = get(p.getUuid());

                // Creative mode players have infinite mana
                if (p.isCreative()) {
                    pd.setMana(maxMana);
                } else {
                    // Normal mana regen for survival/adventure/spectator
                    int before = pd.getMana();
                    if (before < maxMana) {
                        pd.addMana(regenRate);
                        // Ensure we don't exceed max mana
                        if (pd.getMana() > maxMana) {
                            pd.setMana(maxMana);
                        }
                        store.save(pd);
                    }
                }

                // Action bar display with mana
                String manaDisplay = p.isCreative() ? "∞" : String.valueOf(pd.getMana());
                p.sendMessage(Text.literal("Ⓜ Mana: ")
                    .formatted(Formatting.AQUA)
                    .append(Text.literal(manaDisplay).formatted(Formatting.WHITE))
                    .append(Text.literal("/" + maxMana).formatted(Formatting.GRAY)), true);
            }
        });
    }

    public void stop() {
        taskRunning = false;
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, store::load);
    }

    public void save(UUID uuid) {
        PlayerData pd = cache.get(uuid);
        if (pd != null) store.save(pd);
    }

    public boolean spend(ServerPlayerEntity player, int amount) {
        // Creative mode players don't spend mana
        if (player.isCreative()) {
            return true;
        }

        PlayerData pd = get(player.getUuid());
        if (pd.getMana() < amount) return false;
        pd.addMana(-amount);
        store.save(pd);
        return true;
    }
    
    /**
     * Check if player has enough mana without spending it
     * @param player The player to check
     * @param amount The amount of mana required
     * @return true if player has enough mana, false otherwise
     */
    public boolean hasMana(ServerPlayerEntity player, int amount) {
        // Creative mode players always have mana
        if (player.isCreative()) {
            return true;
        }
        
        PlayerData pd = get(player.getUuid());
        return pd.getMana() >= amount;
    }
}

