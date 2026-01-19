package net.saturn.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.managers.ConfigManager;

public class ConfigCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("elementconfig")
                .requires(source -> source.hasPermissionLevel(2)) // Require op
                .then(CommandManager.literal("reload")
                        .executes(ConfigCommand::reloadConfig)));
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            ConfigManager configManager = ElementMod.getInstance().getConfigManager();
            configManager.reload();
            
            source.sendFeedback(() -> Text.literal("Configuration reloaded successfully!")
                    .formatted(Formatting.GREEN), true);
            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("Failed to reload configuration: " + e.getMessage())
                    .formatted(Formatting.RED));
            ElementMod.LOGGER.error("Error reloading config via command", e);
            return 0;
        }
    }
}
