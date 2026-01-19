package net.saturn.elementmod.elements.impl.basic.metal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryEntityData;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

import java.util.List;

/**
 * Metal Chain - Pull targeted enemy and stun for 3 seconds
 */
public class Ability1 {
    private final ElementMod plugin;

    public Ability1(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.METAL);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        // Find target
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0f);

        List<LivingEntity> possibleTargets = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(20),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : possibleTargets) {
            Vec3d entityPos = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ());
            Vec3d toEntity = entityPos.subtract(start);
            double dot = toEntity.normalize().dotProduct(direction);

            if (dot > 0.95) {
                double dist = toEntity.length();
                if (dist < closestDist && dist <= 20) {
                    closestDist = dist;
                    target = entity;
                }
            }
        }

        if (target == null) {
            player.sendMessage(Text.literal("No target found!")
                    .formatted(Formatting.YELLOW));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Pull target towards player
        Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d pullDirection = playerPos.subtract(targetPos).normalize();

        target.setVelocity(pullDirection.multiply(2.0).add(0, 0.5, 0));
        target.velocityModified = true;

        // Stun target
        TemporaryEntityData.putLong(
                target.getUuid(),
                MetadataKeys.Metal.CHAIN_STUN,
                System.currentTimeMillis() + Constants.Duration.METAL_CHAIN_STUN_MS
        );
        // Removed slowness as stun is now handled by LivingEntityMixin

        player.sendMessage(Text.literal("Metal Chain! Pulled and stunned " + target.getName().getString() + "!")
                .formatted(Formatting.GRAY));

        return true;
    }
}

