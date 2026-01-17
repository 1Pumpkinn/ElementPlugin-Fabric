package net.saturn.elementpluginfabric.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandRegistry {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ElementCommand.register(dispatcher, registryAccess, environment);
            ManaCommand.register(dispatcher, registryAccess, environment);
        });
    }
}

