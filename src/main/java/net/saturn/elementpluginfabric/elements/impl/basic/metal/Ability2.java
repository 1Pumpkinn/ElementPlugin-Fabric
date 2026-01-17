package net.saturn.elementpluginfabric.elements.impl.basic.metal;

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
 * Metal Dash - Dash 20 blocks, deal 4 true damage to enemies
 */
public class Ability2 {
    private final ElementPluginFabric plugin;

    public Ability2(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.METAL);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        Vec3 lookVec = player.getLookAngle();
        double dashStrength = 3.0;

        Vec3 dashVelocity = new Vec3(
                lookVec.x * dashStrength,
                lookVec.y * dashStrength + 0.2,
                lookVec.z * dashStrength
        );

        player.setDeltaMovement(dashVelocity);
        player.hurtMarked = true;

        // Check for enemies hit during dash
        List<LivingEntity> hitEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(2.0),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        if (hitEntities.isEmpty()) {
            // Missed all enemies - stun self for 5 seconds
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 255));
            player.sendSystemMessage(Component.literal("Metal Dash missed! You're stunned!")
                    .withStyle(ChatFormatting.RED));
        } else {
            // Hit enemies - deal true damage
            for (LivingEntity entity : hitEntities) {
                entity.hurt(player.damageSources().generic(), 8.0f); // 4 hearts true damage
            }
            player.sendSystemMessage(Component.literal("Metal Dash! Hit " + hitEntities.size() + " enemies!")
                    .withStyle(ChatFormatting.GRAY));
        }

        return true;
    }
}

