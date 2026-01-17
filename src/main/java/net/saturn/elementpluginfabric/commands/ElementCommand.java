package net.saturn.elementpluginfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ElementManager;

public class ElementCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("element")
            .requires(source -> source.hasPermission(0))
            .then(Commands.literal("set")
                .then(Commands.argument("element", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        for (ElementType type : ElementType.values()) {
                            builder.suggest(type.name().toLowerCase());
                        }
                        return builder.buildFuture();
                    })
                    .executes(ElementCommand::setElement)))
            .then(Commands.literal("roll")
                .executes(ElementCommand::rollElement))
            .then(Commands.literal("reroll")
                .executes(ElementCommand::rerollElement))
            .then(Commands.literal("info")
                .executes(ElementCommand::infoElement)));
    }
    
    private static int setElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrThrow();
        String elementName = StringArgumentType.getString(context, "element");
        
        ElementType type;
        try {
            type = ElementType.valueOf(elementName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid element: " + elementName)
                .withStyle(ChatFormatting.RED));
            return 0;
        }
        
        ElementManager elementManager = ElementPluginFabric.getInstance().getElementManager();
        elementManager.assignElement(player, type);
        
        source.sendSuccess(() -> Component.literal("Element set to: ")
            .withStyle(ChatFormatting.GREEN)
            .append(Component.literal(type.name()).withStyle(ChatFormatting.AQUA)), false);
        
        return 1;
    }
    
    private static int rollElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrThrow();
        
        ElementManager elementManager = ElementPluginFabric.getInstance().getElementManager();
        elementManager.rollAndAssign(player);
        
        source.sendSuccess(() -> Component.literal("Rolling for a random element...")
            .withStyle(ChatFormatting.GOLD), false);
        
        return 1;
    }
    
    private static int rerollElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrThrow();
        
        ElementManager elementManager = ElementPluginFabric.getInstance().getElementManager();
        elementManager.assignRandomDifferentElement(player);
        
        source.sendSuccess(() -> Component.literal("Element rerolled!")
            .withStyle(ChatFormatting.GOLD), false);
        
        return 1;
    }
    
    private static int infoElement(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrThrow();
        
        ElementManager elementManager = ElementPluginFabric.getInstance().getElementManager();
        ElementType current = elementManager.getPlayerElement(player);
        
        if (current == null) {
            source.sendSuccess(() -> Component.literal("You don't have an element assigned yet!")
                .withStyle(ChatFormatting.YELLOW), false);
        } else {
            source.sendSuccess(() -> Component.literal("Your current element: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(current.name()).withStyle(ChatFormatting.AQUA)), false);
        }
        
        return 1;
    }
}

