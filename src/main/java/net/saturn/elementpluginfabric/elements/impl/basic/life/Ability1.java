package net.saturn.elementpluginfabric.elements.impl.basic.life;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

import java.util.List;

/**
 * Regeneration Aura - Grant Regeneration II for 10 seconds to self and trusted allies within 5 blocks
 */
public class Ability1 {
    private final ElementPluginFabric plugin;

    public Ability1(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.LIFE);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Apply to self
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1)); // 10 seconds, Regen II

        // Find trusted allies nearby
        List<ServerPlayer> nearbyPlayers = player.level().getEntitiesOfClass(
                ServerPlayer.class,
                player.getBoundingBox().inflate(Constants.Distance.LIFE_REGEN_RADIUS),
                p -> p != player &&
                        !p.isSpectator() &&
                        context.getTrustManager().isTrusted(player.getUUID(), p.getUUID())
        );

        for (ServerPlayer ally : nearbyPlayers) {
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            ally.sendSystemMessage(Component.literal(player.getName().getString() + " shared Regeneration with you!")
                    .withStyle(ChatFormatting.GREEN));
        }

        player.sendSystemMessage(Component.literal("Regeneration Aura activated! Affected " +
                        (nearbyPlayers.size() + 1) + " players!")
                .withStyle(ChatFormatting.GREEN));

        return true;
    }
}

