package net.saturn.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.managers.ItemManager;

public class ElementItemCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("elementitem")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("give")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.literal("upgrader_i")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "upgrader_i"))))
                                .then(CommandManager.literal("upgrader_ii")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "upgrader_ii"))))
                                .then(CommandManager.literal("reroller")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "reroller"))))
                                .then(CommandManager.literal("advanced_reroller")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "advanced_reroller"))))
                                .then(CommandManager.literal("element_core")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "element_core")))))));
    }

    private static int giveItem(CommandContext<ServerCommandSource> context, String type) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        ItemManager itemManager = ElementMod.getInstance().getItemManager();
        ItemStack stack;

        switch (type) {
            case "upgrader_i":
                stack = itemManager.createUpgraderI();
                break;
            case "upgrader_ii":
                stack = itemManager.createUpgraderII();
                break;
            case "reroller":
                stack = itemManager.createReroller();
                break;
            case "advanced_reroller":
                stack = itemManager.createAdvancedReroller();
                break;
            case "element_core":
                stack = itemManager.createElementCore();
                break;
            default:
                source.sendError(Text.literal("Unknown item type: " + type));
                return 0;
        }

        stack.setCount(amount);
        if (target.getInventory().insertStack(stack)) {
            source.sendFeedback(() -> Text.literal("Gave " + amount + "x ")
                    .append(Text.literal(type).formatted(Formatting.AQUA))
                    .append(Text.literal(" to " + target.getName().getString()))
                    .formatted(Formatting.GREEN), true);
            return 1;
        } else {
            source.sendError(Text.literal(target.getName().getString() + "'s inventory is full!"));
            return 0;
        }
    }
}
