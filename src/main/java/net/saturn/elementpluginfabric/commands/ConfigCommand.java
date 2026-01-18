package net.saturn.elementpluginfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.managers.ConfigManager;

public class ConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("elementconfig")
                .requires(source -> source.hasPermission(2)) // Require op
                .then(Commands.literal("reload")
                        .executes(ConfigCommand::reloadConfig)));
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            ConfigManager configManager = ElementPluginFabric.getInstance().getConfigManager();
            configManager.reload();
            
            source.sendSuccess(() -> Component.literal("Configuration reloaded successfully!")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload configuration: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            ElementPluginFabric.LOGGER.error("Error reloading config via command", e);
            return 0;
        }
    }
}
