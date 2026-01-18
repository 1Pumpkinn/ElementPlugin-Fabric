package net.saturn.elementpluginfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.managers.ItemManager;

public class ElementItemCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                 CommandBuildContext registryAccess,
                                 Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("elementitem")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("upgrader_i")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "upgrader_i"))))
                                .then(Commands.literal("upgrader_ii")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "upgrader_ii"))))
                                .then(Commands.literal("reroller")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "reroller"))))
                                .then(Commands.literal("advanced_reroller")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "advanced_reroller"))))
                                .then(Commands.literal("element_core")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> giveItem(ctx, "element_core")))))));
    }

    private static int giveItem(CommandContext<CommandSourceStack> context, String type) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");

        ItemManager itemManager = ElementPluginFabric.getInstance().getItemManager();
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
                source.sendFailure(Component.literal("Unknown item type: " + type));
                return 0;
        }

        stack.setCount(amount);
        if (target.getInventory().add(stack)) {
            source.sendSuccess(() -> Component.literal("Gave " + amount + "x ")
                    .append(Component.literal(type).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" to " + target.getScoreboardName()))
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } else {
            source.sendFailure(Component.literal(target.getScoreboardName() + "'s inventory is full!"));
            return 0;
        }
    }
}
