package net.saturn.elementpluginfabric.data;

import net.saturn.elementpluginfabric.ElementPluginFabric;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DataStore {
    @SuppressWarnings("unused")
    private final ElementPluginFabric plugin;
    private final Path dataDir;
    private final Path playerFile;
    private final Path serverFile;
    private final Yaml yaml = new Yaml();
    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();

    public DataStore(ElementPluginFabric plugin) {
        this.plugin = plugin;
        
        // Get mod data directory (Fabric uses .minecraft/config/modid/)
        Path configDir = Path.of(System.getProperty("user.dir"), "config", "elementplugin");
        this.dataDir = configDir.resolve("data");
        
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            ElementPluginFabric.LOGGER.error("Failed to create data directory: " + dataDir, e);
            throw new RuntimeException("Could not create data directory", e);
        }

        this.playerFile = dataDir.resolve("players.yml");
        this.serverFile = dataDir.resolve("server.yml");
        
        // Create files if they don't exist
        try {
            if (!Files.exists(playerFile)) {
                Files.createFile(playerFile);
            }
            if (!Files.exists(serverFile)) {
                Files.createFile(serverFile);
            }
        } catch (IOException e) {
            ElementPluginFabric.LOGGER.error("Failed to create data files", e);
            throw new RuntimeException("Could not create data files", e);
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        // Check cache first
        if (playerDataCache.containsKey(uuid)) {
            return playerDataCache.get(uuid);
        }

        // Load from file if not in cache
        PlayerData data = loadPlayerDataFromFile(uuid);
        playerDataCache.put(uuid, data);
        return data;
    }

    @SuppressWarnings("unchecked")
    private PlayerData loadPlayerDataFromFile(UUID uuid) {
        try {
            if (!Files.exists(playerFile)) {
                return new PlayerData(uuid);
            }
            
            Map<String, Object> root = yaml.load(new FileInputStream(playerFile.toFile()));
            if (root == null) {
                return new PlayerData(uuid);
            }

            String uuidString = uuid.toString();
            Map<String, Object> section = null;

            // Try "players.<uuid>" format FIRST
            Object playersObj = root.get("players");
            if (playersObj instanceof Map) {
                section = (Map<String, Object>) ((Map<?, ?>) playersObj).get(uuidString);
            }

            // Fallback: Try root level (legacy format)
            if (section == null) {
                section = (Map<String, Object>) root.get(uuidString);
                if (section != null) {
                    ElementPluginFabric.LOGGER.warn("Found player data for " + uuidString + " at root level. Consider migrating to 'players.' format.");
                }
            }

            if (section == null) {
                return new PlayerData(uuid);
            }

            return new PlayerData(uuid, section);
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.error("Failed to reload player configuration", e);
            return new PlayerData(uuid);
        }
    }

    public synchronized PlayerData load(UUID uuid) {
        return getPlayerData(uuid);
    }

    @SuppressWarnings("unchecked")
    public synchronized void save(PlayerData pd) {
        try {
            Map<String, Object> root = new HashMap<>();
            if (Files.exists(playerFile)) {
                try (FileInputStream fis = new FileInputStream(playerFile.toFile())) {
                    root = yaml.load(fis);
                    if (root == null) root = new HashMap<>();
                }
            }

            // ALWAYS use "players.<uuid>" format for consistency
            Map<String, Object> players = (Map<String, Object>) root.computeIfAbsent("players", k -> new HashMap<>());
            players.put(pd.getUuid().toString(), pd.toMap());

            // Update cache
            playerDataCache.put(pd.getUuid(), pd);

            // Save to disk
            try (FileWriter writer = new FileWriter(playerFile.toFile())) {
                yaml.dump(root, writer);
            }
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.error("Failed to save player data for " + pd.getUuid(), e);
        }
    }

    public synchronized void invalidateCache(UUID uuid) {
        playerDataCache.remove(uuid);
    }

    public synchronized void flushAll() {
        // Data is saved immediately, so this is a no-op
        // But we can ensure all cached data is persisted
        for (PlayerData pd : playerDataCache.values()) {
            save(pd);
        }
    }

    // TRUST store - now delegates to PlayerData
    public synchronized Set<UUID> getTrusted(UUID owner) {
        PlayerData pd = getPlayerData(owner);
        return pd.getTrustedPlayers();
    }

    public synchronized void setTrusted(UUID owner, Set<UUID> trusted) {
        PlayerData pd = getPlayerData(owner);
        pd.setTrustedPlayers(trusted);
        save(pd);
    }

    // Server-wide restrictions
    @SuppressWarnings("unchecked")
    public synchronized boolean isLifeElementCrafted() {
        try {
            if (!Files.exists(serverFile)) {
                return false;
            }
            Map<String, Object> root = yaml.load(new FileInputStream(serverFile.toFile()));
            if (root == null) return false;
            Object value = root.get("life_crafted");
            return value instanceof Boolean ? (Boolean) value : false;
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.error("Failed to check life element crafted status", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void setLifeElementCrafted(boolean crafted) {
        try {
            Map<String, Object> root = new HashMap<>();
            if (Files.exists(serverFile)) {
                try (FileInputStream fis = new FileInputStream(serverFile.toFile())) {
                    root = yaml.load(fis);
                    if (root == null) root = new HashMap<>();
                }
            }
            root.put("life_crafted", crafted);
            try (FileWriter writer = new FileWriter(serverFile.toFile())) {
                yaml.dump(root, writer);
            }
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.error("Failed to set life element crafted status", e);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean isDeathElementCrafted() {
        try {
            if (!Files.exists(serverFile)) {
                return false;
            }
            Map<String, Object> root = yaml.load(new FileInputStream(serverFile.toFile()));
            if (root == null) return false;
            Object value = root.get("death_crafted");
            return value instanceof Boolean ? (Boolean) value : false;
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.error("Failed to check death element crafted status", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void setDeathElementCrafted(boolean crafted) {
        try {
            Map<String, Object> root = new HashMap<>();
            if (Files.exists(serverFile)) {
                try (FileInputStream fis = new FileInputStream(serverFile.toFile())) {
                    root = yaml.load(fis);
                    if (root == null) root = new HashMap<>();
                }
            }
            root.put("death_crafted", crafted);
            try (FileWriter writer = new FileWriter(serverFile.toFile())) {
                yaml.dump(root, writer);
            }
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.error("Failed to set death element crafted status", e);
        }
    }
}

