package net.saturn.elementmod.elements.impl.basic.air;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

import java.util.List;

/**
 * Air Blast - Push all nearby enemies away in 6-block radius
 */
public class Ability1 {
    private final ElementMod plugin;

    public Ability1(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.AIR);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Get nearby entities
        List<LivingEntity> nearbyEntities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(Constants.Distance.AIR_BLAST_RADIUS),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        if (nearbyEntities.isEmpty()) {
            player.sendMessage(Text.literal("No enemies nearby!")
                    .formatted(Formatting.YELLOW));
            return false;
        }

        Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());

        for (LivingEntity entity : nearbyEntities) {
            Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            Vec3d direction = entityPos.subtract(playerPos).normalize();

            // Push entity away with stronger force
            double pushStrength = 2.5;
            Vec3d knockback = new Vec3d(
                    direction.x * pushStrength,
                    0.8, // Upward component
                    direction.z * pushStrength
            );

            entity.setVelocity(entity.getVelocity().add(knockback));
            entity.velocityModified = true;
        }

        player.sendMessage(Text.literal("Air Blast! Pushed " + nearbyEntities.size() + " enemies!")
                .formatted(Formatting.AQUA));

        return true;
    }
}

