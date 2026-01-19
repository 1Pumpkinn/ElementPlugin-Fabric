package net.saturn.elementmod.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.saturn.elementmod.ElementMod;

public class CommandRegistry {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ElementMod.LOGGER.info("Registering commands...");
            ElementCommand.register(dispatcher);
            ManaCommand.register(dispatcher);
            TrustCommand.register(dispatcher);
            ConfigCommand.register(dispatcher);
            ElementItemCommand.register(dispatcher);
            RerollCommand.register(dispatcher);
            ElementMod.LOGGER.info("Commands registered successfully");
        });
    }
}

