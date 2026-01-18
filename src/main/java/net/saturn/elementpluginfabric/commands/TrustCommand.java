package net.saturn.elementpluginfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.managers.TrustManager;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TrustCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("trust")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(TrustCommand::addTrust)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(TrustCommand::removeTrust)))
                .then(Commands.literal("accept")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(TrustCommand::acceptTrust)))
                .then(Commands.literal("deny")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(TrustCommand::denyTrust)))
                .then(Commands.literal("list")
                        .executes(TrustCommand::listTrust)));
    }

    private static int addTrust(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        if (owner.getUUID().equals(target.getUUID())) {
            source.sendFailure(Component.literal("You cannot trust yourself!")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        TrustManager trustManager = ElementPluginFabric.getInstance().getTrustManager();
        if (trustManager.isTrusted(owner.getUUID(), target.getUUID())) {
            source.sendFailure(Component.literal(target.getScoreboardName() + " is already trusted.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        trustManager.addPending(target.getUUID(), owner.getUUID());
        
        source.sendSuccess(() -> Component.literal("Trust request sent to " + target.getScoreboardName())
                .withStyle(ChatFormatting.GREEN), false);
        
        target.sendSystemMessage(Component.literal(owner.getScoreboardName() + " wants to trust you. Use ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("/trust accept " + owner.getScoreboardName())
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" to accept.")
                        .withStyle(ChatFormatting.YELLOW)));

        return 1;
    }

    private static int acceptTrust(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        TrustManager trustManager = ElementPluginFabric.getInstance().getTrustManager();
        if (!trustManager.hasPending(owner.getUUID(), target.getUUID())) {
            source.sendFailure(Component.literal("No pending trust request from " + target.getScoreboardName())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        trustManager.clearPending(owner.getUUID(), target.getUUID());
        trustManager.addMutualTrust(owner.getUUID(), target.getUUID());

        source.sendSuccess(() -> Component.literal("You now mutually trust " + target.getScoreboardName())
                .withStyle(ChatFormatting.GREEN), false);
        
        target.sendSystemMessage(Component.literal(owner.getScoreboardName() + " accepted your trust request. You now mutually trust each other!")
                .withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int denyTrust(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        TrustManager trustManager = ElementPluginFabric.getInstance().getTrustManager();
        if (!trustManager.hasPending(owner.getUUID(), target.getUUID())) {
            source.sendFailure(Component.literal("No pending trust request from " + target.getScoreboardName())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        trustManager.clearPending(owner.getUUID(), target.getUUID());

        source.sendSuccess(() -> Component.literal("Denied trust request from " + target.getScoreboardName())
                .withStyle(ChatFormatting.YELLOW), false);
        
        target.sendSystemMessage(Component.literal(owner.getScoreboardName() + " denied your trust request.")
                .withStyle(ChatFormatting.RED));

        return 1;
    }

    private static int removeTrust(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");

        TrustManager trustManager = ElementPluginFabric.getInstance().getTrustManager();
        if (!trustManager.isTrusted(owner.getUUID(), target.getUUID())) {
            source.sendFailure(Component.literal(target.getScoreboardName() + " is not trusted.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        trustManager.removeTrust(owner.getUUID(), target.getUUID());

        source.sendSuccess(() -> Component.literal("Removed " + target.getScoreboardName() + " from your trust list.")
                .withStyle(ChatFormatting.YELLOW), false);
        
        target.sendSystemMessage(Component.literal(owner.getScoreboardName() + " no longer trusts you.")
                .withStyle(ChatFormatting.RED));

        return 1;
    }

    private static int listTrust(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();

        TrustManager trustManager = ElementPluginFabric.getInstance().getTrustManager();
        Set<UUID> trusted = trustManager.getTrusted(owner.getUUID());

        if (trusted.isEmpty()) {
            source.sendSuccess(() -> Component.literal("You don't trust anyone yet.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("Trusted Players: ")
                .withStyle(ChatFormatting.GREEN), false);

        for (UUID uuid : trusted) {
            ServerPlayer player = source.getServer().getPlayerList().getPlayer(uuid);
            String name = (player != null) ? player.getScoreboardName() : uuid.toString();
            source.sendSuccess(() -> Component.literal("- ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(name).withStyle(ChatFormatting.WHITE)), false);
        }

        return 1;
    }
}
