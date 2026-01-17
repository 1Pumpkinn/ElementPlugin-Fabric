package net.saturn.elementpluginfabric.elements.impl.basic.water;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

import java.util.List;

/**
 * Water Geyser - Launch nearby enemies upward to 20 blocks
 */
public class Ability1 {
    private final ElementPluginFabric plugin;

    public Ability1(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.WATER);

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
                player.getBoundingBox().inflate(Constants.Distance.WATER_GEYSER_RADIUS),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        if (nearbyEntities.isEmpty()) {
            player.sendSystemMessage(Component.literal("No enemies nearby!")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }

        for (LivingEntity entity : nearbyEntities) {
            // Launch upward - velocity to reach ~20 blocks
            // v = sqrt(2 * g * h) where g ≈ 0.08 in Minecraft, h = 20
            // v ≈ 1.8 for 20 blocks
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 1.8, 0));
            entity.hurtMarked = true;
        }

        player.sendSystemMessage(Component.literal("Water Geyser! Launched " + nearbyEntities.size() + " enemies!")
                .withStyle(ChatFormatting.BLUE));

        return true;
    }
}

