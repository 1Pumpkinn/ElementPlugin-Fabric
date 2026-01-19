package net.saturn.elementmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.managers.TrustManager;

import java.util.Set;
import java.util.UUID;

public class TrustCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("trust")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(TrustCommand::addTrust)))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(TrustCommand::removeTrust)))
                .then(CommandManager.literal("accept")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(TrustCommand::acceptTrust)))
                .then(CommandManager.literal("deny")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(TrustCommand::denyTrust)))
                .then(CommandManager.literal("list")
                        .executes(TrustCommand::listTrust)));
    }

    private static int addTrust(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity owner = source.getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

        if (owner.getUuid().equals(target.getUuid())) {
            source.sendError(Text.literal("You cannot trust yourself!")
                    .formatted(Formatting.RED));
            return 0;
        }

        TrustManager trustManager = ElementMod.getInstance().getTrustManager();
        if (trustManager.isTrusted(owner.getUuid(), target.getUuid())) {
            source.sendError(Text.literal(target.getName().getString() + " is already trusted.")
                    .formatted(Formatting.RED));
            return 0;
        }

        trustManager.addPending(target.getUuid(), owner.getUuid());
        
        source.sendFeedback(() -> Text.literal("Trust request sent to " + target.getName().getString())
                .formatted(Formatting.GREEN), false);
        
        target.sendMessage(Text.literal(owner.getName().getString() + " wants to trust you. Use ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal("/trust accept " + owner.getName().getString())
                        .formatted(Formatting.AQUA))
                .append(Text.literal(" to accept.")
                        .formatted(Formatting.YELLOW)));

        return 1;
    }

    private static int acceptTrust(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity owner = source.getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

        TrustManager trustManager = ElementMod.getInstance().getTrustManager();
        if (!trustManager.hasPending(owner.getUuid(), target.getUuid())) {
            source.sendError(Text.literal("No pending trust request from " + target.getName().getString())
                    .formatted(Formatting.RED));
            return 0;
        }

        trustManager.clearPending(owner.getUuid(), target.getUuid());
        trustManager.addMutualTrust(owner.getUuid(), target.getUuid());

        source.sendFeedback(() -> Text.literal("You now mutually trust " + target.getName().getString())
                .formatted(Formatting.GREEN), false);
        
        target.sendMessage(Text.literal(owner.getName().getString() + " accepted your trust request. You now mutually trust each other!")
                .formatted(Formatting.GREEN));

        return 1;
    }

    private static int removeTrust(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity owner = source.getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

        TrustManager trustManager = ElementMod.getInstance().getTrustManager();
        if (!trustManager.isTrusted(owner.getUuid(), target.getUuid())) {
            source.sendError(Text.literal(target.getName().getString() + " is not trusted.")
                    .formatted(Formatting.RED));
            return 0;
        }

        trustManager.removeTrust(owner.getUuid(), target.getUuid());

        source.sendFeedback(() -> Text.literal("You no longer trust " + target.getName().getString())
                .formatted(Formatting.YELLOW), false);
        
        target.sendMessage(Text.literal(owner.getName().getString() + " no longer trusts you.")
                .formatted(Formatting.YELLOW));

        return 1;
    }

    private static int denyTrust(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity owner = source.getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");

        TrustManager trustManager = ElementMod.getInstance().getTrustManager();
        if (!trustManager.hasPending(owner.getUuid(), target.getUuid())) {
            source.sendError(Text.literal("No pending trust request from " + target.getName().getString())
                    .formatted(Formatting.RED));
            return 0;
        }

        trustManager.clearPending(owner.getUuid(), target.getUuid());

        source.sendFeedback(() -> Text.literal("Denied trust request from " + target.getName().getString())
                .formatted(Formatting.YELLOW), false);

        return 1;
    }

    private static int listTrust(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity owner = source.getPlayerOrThrow();

        TrustManager trustManager = ElementMod.getInstance().getTrustManager();
        Set<UUID> trusted = trustManager.getTrusted(owner.getUuid());

        if (trusted.isEmpty()) {
            source.sendFeedback(() -> Text.literal("You don't trust anyone yet.")
                    .formatted(Formatting.YELLOW), false);
            return 1;
        }

        source.sendFeedback(() -> Text.literal("--- Trusted Players ---").formatted(Formatting.GOLD), false);
        for (UUID uuid : trusted) {
            ServerPlayerEntity p = source.getServer().getPlayerManager().getPlayer(uuid);
            String name = p != null ? p.getName().getString() : uuid.toString();
            source.sendFeedback(() -> Text.literal("- " + name).formatted(Formatting.WHITE), false);
        }

        return 1;
    }
}
