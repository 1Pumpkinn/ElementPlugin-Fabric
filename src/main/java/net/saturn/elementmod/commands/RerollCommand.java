package net.saturn.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.managers.ElementManager;

public class RerollCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reroll")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(RerollCommand::reroll));
    }

    private static int reroll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        ElementManager elementManager = ElementMod.getInstance().getElementManager();
        
        // Check if player is already rolling
        if (elementManager.isCurrentlyRolling(player)) {
            source.sendError(Text.literal("You are already rolling!")
                    .formatted(Formatting.RED));
            return 0;
        }

        elementManager.assignRandomDifferentElement(player);

        source.sendFeedback(() -> Text.literal("Element rerolled!")
                .formatted(Formatting.GOLD), false);

        return 1;
    }
}
