package net.saturn.elementpluginfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.data.PlayerData;
import net.saturn.elementpluginfabric.managers.ManaManager;

public class ManaCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("mana")
                .requires(source -> source.hasPermission(0))
                .executes(ManaCommand::checkMana)
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ManaCommand::setMana))));
    }

    private static int checkMana(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        ManaManager manaManager = ElementPluginFabric.getInstance().getManaManager();
        PlayerData data = ElementPluginFabric.getInstance().getDataStore().getPlayerData(player.getUUID());

        int currentMana = data.getMana();
        int maxMana = ElementPluginFabric.getInstance().getConfigManager().getMaxMana();

        source.sendSuccess(() -> Component.literal("Ⓜ Mana: ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.valueOf(currentMana))
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/" + maxMana)
                        .withStyle(ChatFormatting.GRAY)), false);

        return 1;
    }

    private static int setMana(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(context, "amount");

        ManaManager manaManager = ElementPluginFabric.getInstance().getManaManager();
        PlayerData data = ElementPluginFabric.getInstance().getDataStore().getPlayerData(player.getUUID());
        data.setMana(amount);
        ElementPluginFabric.getInstance().getDataStore().save(data);

        source.sendSuccess(() -> Component.literal("Mana set to: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(String.valueOf(amount))
                        .withStyle(ChatFormatting.WHITE)), false);

        return 1;
    }
}