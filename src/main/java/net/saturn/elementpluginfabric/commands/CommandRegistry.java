package net.saturn.elementpluginfabric.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.saturn.elementpluginfabric.ElementPluginFabric;

public class CommandRegistry {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ElementPluginFabric.LOGGER.info("Registering commands...");
            ElementCommand.register(dispatcher, registryAccess, environment);
            ManaCommand.register(dispatcher, registryAccess, environment);
            ElementPluginFabric.LOGGER.info("Commands registered successfully");
        });
    }
}

