package net.saturn.elementpluginfabric.elements.impl.basic.metal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryEntityData;
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
        double dashStrength = 1.5; // Nerfed from 2.0 to be less OP

        Vec3 dashVelocity = new Vec3(
                lookVec.x * dashStrength,
                lookVec.y * dashStrength + 0.2, // Slightly reduced upward boost
                lookVec.z * dashStrength
        );

        player.setDeltaMovement(dashVelocity);
        player.hurtMarked = true;

        // Clear old dash states
        TemporaryEntityData.remove(player.getUUID(), MetadataKeys.Metal.HAS_HIT);
        TemporaryEntityData.remove(player.getUUID(), MetadataKeys.Metal.WAITING_FOR_LANDING);
        TemporaryEntityData.remove(player.getUUID(), MetadataKeys.Metal.GRACE_UNTIL);
        TemporaryEntityData.remove(player.getUUID(), MetadataKeys.Metal.DASH_STUN);

        // Set dashing state for 15 ticks (0.75 seconds)
        TemporaryEntityData.putLong(
                player.getUUID(),
                MetadataKeys.Metal.DASHING_UNTIL,
                System.currentTimeMillis() + 750L
        );

        player.sendSystemMessage(Component.literal("Metal Dash!")
                .withStyle(ChatFormatting.GRAY));

        return true;
    }
}

