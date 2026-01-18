package net.saturn.elementpluginfabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.saturn.elementpluginfabric.commands.CommandRegistry;
import net.saturn.elementpluginfabric.data.DataStore;
import net.saturn.elementpluginfabric.items.ItemRegistry;
import net.saturn.elementpluginfabric.listeners.ElementPassiveListener;
import net.saturn.elementpluginfabric.managers.*;
import net.saturn.elementpluginfabric.services.EffectService;
import net.saturn.elementpluginfabric.services.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementPluginFabric implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ElementPlugin");

    private static ElementPluginFabric instance;

    private DataStore dataStore;
    private ConfigManager configManager;
    private ElementManager elementManager;
    private ManaManager manaManager;
    private TrustManager trustManager;
    private ItemManager itemManager;
    private EffectService effectService;
    private ValidationService validationService;

    @Override
    public void onInitialize() {
        instance = this;

        try {
            LOGGER.info("Initializing ElementPlugin for Fabric 1.21.10...");
            initializeCore();
            initializeManagers();
            initializeServices();
            registerComponents();
            startBackgroundTasks();

            LOGGER.info("ElementPlugin v1.1.16 enabled successfully!");
        } catch (Exception e) {
            LOGGER.error("Failed to enable plugin", e);
            throw new RuntimeException("Failed to initialize ElementPlugin", e);
        }

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            try {
                stopBackgroundTasks();
                saveAllData();
                LOGGER.info("ElementPlugin disabled successfully!");
            } catch (Exception e) {
                LOGGER.error("Error during plugin shutdown", e);
            }
        });
    }

    private void initializeCore() {
        LOGGER.info("Initializing core components...");
        this.configManager = new ConfigManager(this);
        this.dataStore = new DataStore(this);
        LOGGER.info("Core components initialized");
    }

    private void initializeManagers() {
        LOGGER.info("Initializing managers...");

        this.trustManager = new TrustManager(this);
        this.manaManager = new ManaManager(this, dataStore, configManager);
        this.elementManager = new ElementManager(this, dataStore, manaManager, trustManager, configManager);
        this.itemManager = new ItemManager(this, manaManager, configManager);

        LOGGER.info("Managers initialized");
    }

    private void initializeServices() {
        LOGGER.info("Initializing services...");

        this.effectService = new EffectService(this, elementManager);
        this.validationService = new ValidationService(trustManager);

        LOGGER.info("Services initialized");
    }

    private void registerComponents() {
        registerItems();
        registerCommands();
        registerListeners();
        registerRecipes();
        registerNetworking();
    }

    private void registerNetworking() {
        LOGGER.info("Registering networking...");
        net.saturn.elementpluginfabric.network.AbilityPacket.register();
        LOGGER.info("Networking registered");
    }

    private void registerItems() {
        LOGGER.info("Registering items...");
        ItemRegistry.register();
        LOGGER.info("Items registered");
    }

    private void registerCommands() {
        LOGGER.info("Registering commands...");
        CommandRegistry.register();
        LOGGER.info("Commands registered");
    }

    private void registerListeners() {
        LOGGER.info("Registering listeners...");
        ElementPassiveListener.register(this);
        net.saturn.elementpluginfabric.listeners.PlayerConnectionListener.register(this);
        LOGGER.info("Listeners registered");
    }

    private void registerRecipes() {
        LOGGER.info("Registering recipes...");
        net.saturn.elementpluginfabric.recipes.UtilRecipes.register(this);
        LOGGER.info("Recipes registered");
    }

    private void startBackgroundTasks() {
        if (manaManager != null) {
            manaManager.start();
        }
    }

    private void stopBackgroundTasks() {
        if (manaManager != null) {
            manaManager.stop();
        }
    }

    private void saveAllData() {
        if (dataStore != null) {
            dataStore.flushAll();
        }
    }

    public static ElementPluginFabric getInstance() {
        return instance;
    }

    public DataStore getDataStore() { return dataStore; }
    public ConfigManager getConfigManager() { return configManager; }
    public ElementManager getElementManager() { return elementManager; }
    public ManaManager getManaManager() { return manaManager; }
    public TrustManager getTrustManager() { return trustManager; }
    public ItemManager getItemManager() { return itemManager; }
    public EffectService getEffectService() { return effectService; }
    public ValidationService getValidationService() { return validationService; }
}