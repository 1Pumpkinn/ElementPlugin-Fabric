package net.saturn.elementpluginfabric.elements.impl.basic.air;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

import java.util.List;

/**
 * Air Blast - Push all nearby enemies away in 6-block radius
 */
public class Ability1 {
    private final ElementPluginFabric plugin;

    public Ability1(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.AIR);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Get nearby entities
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(Constants.Distance.AIR_BLAST_RADIUS),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        if (nearbyEntities.isEmpty()) {
            player.sendSystemMessage(Component.literal("No enemies nearby!")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }

        Vec3 playerPos = player.position();

        for (LivingEntity entity : nearbyEntities) {
            Vec3 entityPos = entity.position();
            Vec3 direction = entityPos.subtract(playerPos).normalize();

            // Push entity away with stronger force
            double pushStrength = 2.5;
            Vec3 knockback = new Vec3(
                    direction.x * pushStrength,
                    0.8, // Upward component
                    direction.z * pushStrength
            );

            entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            entity.hurtMarked = true;
        }

        player.sendSystemMessage(Component.literal("Air Blast! Pushed " + nearbyEntities.size() + " enemies!")
                .withStyle(ChatFormatting.AQUA));

        return true;
    }
}

