package net.saturn.elementmod.elements.impl.basic.air;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

import java.util.List;

/**
 * Air Dash - Dash forward, pushing enemies aside for 1 second
 */
public class Ability2 {
    private final ElementMod plugin;

    public Ability2(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.AIR);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Dash forward in the direction player is looking
        Vec3d lookVec = player.getRotationVec(1.0f);
        double dashStrength = 2.0;

        Vec3d dashVelocity = new Vec3d(
                lookVec.x * dashStrength,
                lookVec.y * dashStrength + 0.3, // Slight upward component
                lookVec.z * dashStrength
        );

        player.setVelocity(dashVelocity);
        player.velocityModified = true;

        // Push aside nearby enemies during dash
        List<LivingEntity> nearbyEntities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(2.0),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        for (LivingEntity entity : nearbyEntities) {
            Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
            Vec3d direction = entityPos.subtract(playerPos).normalize();

            Vec3d knockback = new Vec3d(
                    direction.x * 1.5,
                    0.5,
                    direction.z * 1.5
            );

            entity.setVelocity(entity.getVelocity().add(knockback));
            entity.velocityModified = true;

        }

        player.sendMessage(Text.literal("Air Dash!")
                .formatted(Formatting.AQUA));

        return true;
    }
}

