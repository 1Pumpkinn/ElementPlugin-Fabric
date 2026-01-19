package net.saturn.elementmod.elements.impl.basic.life;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

import java.util.List;

/**
 * Regeneration Aura - Grant Regeneration II for 10 seconds to self and trusted allies within 5 blocks
 */
public class Ability1 {
    private final ElementMod plugin;

    public Ability1(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.LIFE);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Apply to self
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1)); // 10 seconds, Regen II

        // Find trusted allies nearby
        List<ServerPlayerEntity> nearbyPlayers = player.getEntityWorld().getEntitiesByClass(
                ServerPlayerEntity.class,
                player.getBoundingBox().expand(Constants.Distance.LIFE_REGEN_RADIUS),
                p -> p != player &&
                        !p.isSpectator() &&
                        context.getTrustManager().isTrusted(player.getUuid(), p.getUuid())
        );

        for (ServerPlayerEntity ally : nearbyPlayers) {
            ally.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
            ally.sendMessage(Text.literal(player.getName().getString() + " shared Regeneration with you!")
                    .formatted(Formatting.GREEN));
        }

        player.sendMessage(Text.literal("Regeneration Aura activated! Affected " +
                        (nearbyPlayers.size() + 1) + " players!")
                .formatted(Formatting.GREEN));

        return true;
    }
}

