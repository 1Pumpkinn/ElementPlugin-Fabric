package net.saturn.elementpluginfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.managers.ElementManager;

public class RerollCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("reroll")
                .requires(source -> source.hasPermission(0))
                .executes(RerollCommand::reroll));
    }

    private static int reroll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        ElementManager elementManager = ElementPluginFabric.getInstance().getElementManager();
        
        // Check if player is already rolling
        if (elementManager.isCurrentlyRolling(player)) {
            source.sendFailure(Component.literal("You are already rolling!")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        elementManager.assignRandomDifferentElement(player);

        source.sendSuccess(() -> Component.literal("Element rerolled!")
                .withStyle(ChatFormatting.GOLD), false);

        return 1;
    }
}
