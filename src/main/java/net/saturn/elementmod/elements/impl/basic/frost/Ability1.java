package net.saturn.elementmod.elements.impl.basic.frost;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryEntityData;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

import java.util.List;

/**
 * Freezing Circle - 5-block radius that severely slows enemies for 10 seconds
 */
public class Ability1 {
    private final ElementMod plugin;

    public Ability1(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.FROST);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        List<LivingEntity> nearbyEntities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(Constants.Distance.FROST_CIRCLE_RADIUS),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        for (LivingEntity entity : nearbyEntities) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 4));
            TemporaryEntityData.putLong(
                    entity.getUuid(),
                    MetadataKeys.Frost.CIRCLE_FROZEN,
                    System.currentTimeMillis() + Constants.Duration.FROST_CIRCLE_MS
            );
        }

        player.sendMessage(Text.literal("Freezing Circle! Stunned " + nearbyEntities.size() + " enemies!")
                .formatted(Formatting.AQUA));

        return true;
    }
}

