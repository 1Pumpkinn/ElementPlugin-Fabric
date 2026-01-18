package net.saturn.elementpluginfabric.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.saturn.elementpluginfabric.ElementPluginFabric;

public class CommandRegistry {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ElementPluginFabric.LOGGER.info("Registering commands...");
            ElementCommand.register(dispatcher, registryAccess, environment);
            ManaCommand.register(dispatcher, registryAccess, environment);
            TrustCommand.register(dispatcher, registryAccess, environment);
            ConfigCommand.register(dispatcher, registryAccess, environment);
            ElementItemCommand.register(dispatcher, registryAccess, environment);
            RerollCommand.register(dispatcher, registryAccess, environment);
            ElementPluginFabric.LOGGER.info("Commands registered successfully");
        });
    }
}

