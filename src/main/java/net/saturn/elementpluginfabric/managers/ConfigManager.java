package net.saturn.elementpluginfabric.managers;

import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.ElementType;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    @SuppressWarnings("unused")
    private final ElementPluginFabric plugin;
    private final Path configFile;
    private final Yaml yaml = new Yaml();
    private Map<String, Object> config;

    public ConfigManager(ElementPluginFabric plugin) {
        this.plugin = plugin;
        
        Path configDir = Path.of(System.getProperty("user.dir"), "config", "elementplugin");
        this.configFile = configDir.resolve("config.yml");
        
        try {
            Files.createDirectories(configDir);
            if (!Files.exists(configFile)) {
                createDefaultConfig();
            }
            loadConfig();
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.error("Failed to load plugin configuration", e);
            throw new RuntimeException("Could not load plugin configuration", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadConfig() {
        try {
            if (Files.exists(configFile)) {
                try (FileInputStream fis = new FileInputStream(configFile.toFile())) {
                    config = yaml.load(fis);
                    if (config == null) config = new HashMap<>();
                }
            } else {
                config = new HashMap<>();
            }
        } catch (IOException e) {
            ElementPluginFabric.LOGGER.error("Failed to load config", e);
            config = new HashMap<>();
        }
    }

    private void createDefaultConfig() throws IOException {
        Map<String, Object> defaultConfig = new HashMap<>();
        
        Map<String, Object> mana = new HashMap<>();
        mana.put("max", 100);
        mana.put("regen_per_second", 1);
        defaultConfig.put("mana", mana);
        
        Map<String, Object> recipes = new HashMap<>();
        recipes.put("advanced_reroller_enabled", true);
        defaultConfig.put("recipes", recipes);
        
        Map<String, Object> costs = new HashMap<>();
        for (ElementType type : ElementType.values()) {
            Map<String, Object> elementCosts = new HashMap<>();
            elementCosts.put("ability1", 50);
            elementCosts.put("ability2", 75);
            elementCosts.put("item_use", 75);
            elementCosts.put("item_throw", 25);
            costs.put(type.name().toLowerCase(), elementCosts);
        }
        defaultConfig.put("costs", costs);
        
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            yaml.dump(defaultConfig, writer);
        }
    }

    public void reload() {
        try {
            loadConfig();
            ElementPluginFabric.LOGGER.info("Configuration reloaded successfully");
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.error("Failed to reload configuration", e);
        }
    }

    @SuppressWarnings("unchecked")
    private int getInt(String path, int defaultValue) {
        try {
            String[] parts = path.split("\\.");
            Object current = config;
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(part);
                } else {
                    return defaultValue;
                }
            }
            if (current instanceof Number) {
                return ((Number) current).intValue();
            }
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.warn("Error reading " + path + " from config, using default value " + defaultValue, e);
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private boolean getBoolean(String path, boolean defaultValue) {
        try {
            String[] parts = path.split("\\.");
            Object current = config;
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(part);
                } else {
                    return defaultValue;
                }
            }
            if (current instanceof Boolean) {
                return (Boolean) current;
            }
        } catch (Exception e) {
            ElementPluginFabric.LOGGER.warn("Error reading " + path + " from config, using default value " + defaultValue, e);
        }
        return defaultValue;
    }

    // Mana settings
    public int getMaxMana() {
        return getInt("mana.max", 100);
    }

    public int getManaRegenPerSecond() {
        return getInt("mana.regen_per_second", 1);
    }

    // Ability costs
    public int getAbility1Cost(ElementType type) {
        return getInt("costs." + type.name().toLowerCase() + ".ability1", 50);
    }

    public int getAbility2Cost(ElementType type) {
        return getInt("costs." + type.name().toLowerCase() + ".ability2", 75);
    }

    public int getItemUseCost(ElementType type) {
        return getInt("costs." + type.name().toLowerCase() + ".item_use", 75);
    }

    public int getItemThrowCost(ElementType type) {
        return getInt("costs." + type.name().toLowerCase() + ".item_throw", 25);
    }

    public boolean isAdvancedRerollerRecipeEnabled() {
        return getBoolean("recipes.advanced_reroller_enabled", true);
    }

    @SuppressWarnings("unchecked")
    public void setAdvancedRerollerRecipeEnabled(boolean enabled) {
        try {
            Map<String, Object> recipes = (Map<String, Object>) config.computeIfAbsent("recipes", k -> new HashMap<>());
            recipes.put("advanced_reroller_enabled", enabled);
            try (FileWriter writer = new FileWriter(configFile.toFile())) {
                yaml.dump(config, writer);
            }
        } catch (IOException e) {
            ElementPluginFabric.LOGGER.error("Failed to save config", e);
        }
    }
}

