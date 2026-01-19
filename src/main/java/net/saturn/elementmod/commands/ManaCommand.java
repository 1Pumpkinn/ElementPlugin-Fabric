package net.saturn.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.data.PlayerData;
import net.saturn.elementmod.managers.ManaManager;

public class ManaCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mana")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(ManaCommand::checkMana)
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ManaCommand::setMana))));
    }

    private static int checkMana(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();

        ManaManager manaManager = ElementMod.getInstance().getManaManager();
        PlayerData data = ElementMod.getInstance().getDataStore().getPlayerData(player.getUuid());

        int currentMana = data.getMana();
        int maxMana = ElementMod.getInstance().getConfigManager().getMaxMana();

        source.sendFeedback(() -> Text.literal("Ⓜ Mana: ")
                .formatted(Formatting.AQUA)
                .append(Text.literal(String.valueOf(currentMana))
                        .formatted(Formatting.WHITE))
                .append(Text.literal("/" + maxMana)
                        .formatted(Formatting.GRAY)), false);

        return 1;
    }

    private static int setMana(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        int amount = IntegerArgumentType.getInteger(context, "amount");

        ManaManager manaManager = ElementMod.getInstance().getManaManager();
        PlayerData data = ElementMod.getInstance().getDataStore().getPlayerData(player.getUuid());
        data.setMana(amount);
        ElementMod.getInstance().getDataStore().save(data);

        source.sendFeedback(() -> Text.literal("Mana set to: ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(amount))
                        .formatted(Formatting.WHITE)), false);

        return 1;
    }
}