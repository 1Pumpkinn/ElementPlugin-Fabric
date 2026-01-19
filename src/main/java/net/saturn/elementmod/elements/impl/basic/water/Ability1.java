package net.saturn.elementmod.elements.impl.basic.water;

import net.minecraft.entity.LivingEntity;
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
 * Water Geyser - Launch nearby enemies upward to 20 blocks
 */
public class Ability1 {
    private final ElementMod plugin;

    public Ability1(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.WATER);

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
                player.getBoundingBox().expand(Constants.Distance.WATER_GEYSER_RADIUS),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        if (nearbyEntities.isEmpty()) {
            player.sendMessage(Text.literal("No enemies nearby!")
                    .formatted(Formatting.YELLOW));
            return false;
        }

        for (LivingEntity entity : nearbyEntities) {
            // Launch upward - velocity to reach ~20 blocks
            // v = sqrt(2 * g * h) where g ≈ 0.08 in Minecraft, h = 20
            // v ≈ 1.8 for 20 blocks
            entity.setVelocity(entity.getVelocity().add(0, 1.8, 0));
            entity.velocityModified = true;
        }

        player.sendMessage(Text.literal("Water Geyser! Launched " + nearbyEntities.size() + " enemies!")
                .formatted(Formatting.BLUE));

        return true;
    }
}

