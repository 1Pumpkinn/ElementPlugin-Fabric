package net.saturn.elementpluginfabric.elements.impl.basic.air;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

import java.util.List;

/**
 * Air Dash - Dash forward, pushing enemies aside for 1 second
 */
public class Ability2 {
    private final ElementPluginFabric plugin;

    public Ability2(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.AIR);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Dash forward in the direction player is looking
        Vec3 lookVec = player.getLookAngle();
        double dashStrength = 2.0;

        Vec3 dashVelocity = new Vec3(
                lookVec.x * dashStrength,
                lookVec.y * dashStrength + 0.3, // Slight upward component
                lookVec.z * dashStrength
        );

        player.setDeltaMovement(dashVelocity);
        player.hurtMarked = true;

        // Push aside nearby enemies during dash
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(2.0),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        for (LivingEntity entity : nearbyEntities) {
            Vec3 entityPos = entity.position();
            Vec3 playerPos = player.position();
            Vec3 direction = entityPos.subtract(playerPos).normalize();

            Vec3 knockback = new Vec3(
                    direction.x * 1.5,
                    0.5,
                    direction.z * 1.5
            );

            entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            entity.hurtMarked = true;

        }

        player.sendSystemMessage(Component.literal("Air Dash!")
                .withStyle(ChatFormatting.AQUA));

        return true;
    }
}

