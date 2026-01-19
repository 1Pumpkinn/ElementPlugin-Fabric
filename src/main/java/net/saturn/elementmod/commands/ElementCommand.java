package net.saturn.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ElementManager;
import net.saturn.elementmod.gui.ElementSelectionGUI;

public class ElementCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("element")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("element", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (ElementType type : ElementType.values()) {
                                        builder.suggest(type.name().toLowerCase());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ElementCommand::setElement)))
                .then(CommandManager.literal("roll")
                        .executes(ElementCommand::rollElement))
                .then(CommandManager.literal("reroll")
                        .executes(ElementCommand::rerollElement))
                .then(CommandManager.literal("select")
                        .executes(ElementCommand::selectElement))
                .then(CommandManager.literal("info")
                        .executes(ElementCommand::infoElement)));
    }

    private static int selectElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        ElementSelectionGUI.open(player);

        return 1;
    }

    private static int setElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        String elementName = StringArgumentType.getString(context, "element");

        ElementType type;
        try {
            type = ElementType.valueOf(elementName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendError(Text.literal("Invalid element: " + elementName)
                    .formatted(Formatting.RED));
            return 0;
        }

        ElementManager elementManager = ElementMod.getInstance().getElementManager();
        elementManager.assignElement(player, type);

        source.sendFeedback(() -> Text.literal("Element set to: ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(type.name()).formatted(Formatting.AQUA)), false);

        return 1;
    }

    private static int rollElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        ElementManager elementManager = ElementMod.getInstance().getElementManager();
        elementManager.rollAndAssign(player);

        source.sendFeedback(() -> Text.literal("Rolling for a random element...")
                .formatted(Formatting.GOLD), false);

        return 1;
    }

    private static int rerollElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        ElementManager elementManager = ElementMod.getInstance().getElementManager();
        elementManager.assignRandomDifferentElement(player);

        source.sendFeedback(() -> Text.literal("Element rerolled!")
                .formatted(Formatting.GOLD), false);

        return 1;
    }

    private static int infoElement(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        var pd = ElementMod.getInstance().getDataStore().getPlayerData(player.getUuid());
        if (pd == null) return 0;

        int maxMana = ElementMod.getInstance().getConfigManager().getMaxMana();

        source.sendFeedback(() -> Text.literal("--- Element Info ---").formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("Current Element: ").formatted(Formatting.YELLOW)
                .append(Text.literal(pd.getCurrentElement() != null ? pd.getCurrentElement().name() : "None").formatted(Formatting.AQUA)), false);
        source.sendFeedback(() -> Text.literal("Mana: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.format("%d/%d", pd.getMana(), maxMana)).formatted(Formatting.AQUA)), false);
        source.sendFeedback(() -> Text.literal("Upgrade Level: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(pd.getCurrentElementUpgradeLevel())).formatted(Formatting.AQUA)), false);

        return 1;
    }
}