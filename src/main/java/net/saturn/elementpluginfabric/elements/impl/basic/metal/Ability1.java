package net.saturn.elementpluginfabric.elements.impl.basic.metal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryEntityData;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

import java.util.List;

/**
 * Metal Chain - Pull targeted enemy and stun for 3 seconds
 */
public class Ability1 {
    private final ElementPluginFabric plugin;

    public Ability1(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.METAL);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // Find target
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle();

        List<LivingEntity> possibleTargets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(20),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : possibleTargets) {
            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            Vec3 toEntity = entityPos.subtract(start);
            double dot = toEntity.normalize().dot(direction);

            if (dot > 0.95) {
                double dist = toEntity.length();
                if (dist < closestDist && dist <= 20) {
                    closestDist = dist;
                    target = entity;
                }
            }
        }

        if (target == null) {
            player.sendSystemMessage(Component.literal("No target found!")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Pull target towards player
        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();
        Vec3 pullDirection = playerPos.subtract(targetPos).normalize();

        target.setDeltaMovement(pullDirection.scale(2.0).add(0, 0.5, 0));
        target.hurtMarked = true;

        // Stun target
        TemporaryEntityData.putLong(
                target.getUUID(),
                MetadataKeys.Metal.CHAIN_STUN,
                System.currentTimeMillis() + Constants.Duration.METAL_CHAIN_STUN_MS
        );
        // Removed slowness as stun is now handled by LivingEntityMixin

        player.sendSystemMessage(Component.literal("Metal Chain! Pulled and stunned " + target.getName().getString() + "!")
                .withStyle(ChatFormatting.GRAY));

        return true;
    }
}

