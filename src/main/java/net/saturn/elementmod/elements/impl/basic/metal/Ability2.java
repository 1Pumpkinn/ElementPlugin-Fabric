package net.saturn.elementmod.elements.impl.basic.metal;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryEntityData;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

/**
 * Metal Dash - Dash 20 blocks, deal 4 true damage to enemies
 */
public class Ability2 {
    private final ElementMod plugin;

    public Ability2(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.METAL);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        Vec3d lookVec = player.getRotationVec(1.0f);
        double dashStrength = 1.5; // Nerfed from 2.0 to be less OP

        Vec3d dashVelocity = new Vec3d(
                lookVec.x * dashStrength,
                lookVec.y * dashStrength + 0.2, // Slightly reduced upward boost
                lookVec.z * dashStrength
        );

        player.setVelocity(dashVelocity);
        player.velocityModified = true;

        // Clear old dash states
        TemporaryEntityData.remove(player.getUuid(), MetadataKeys.Metal.HAS_HIT);
        TemporaryEntityData.remove(player.getUuid(), MetadataKeys.Metal.WAITING_FOR_LANDING);
        TemporaryEntityData.remove(player.getUuid(), MetadataKeys.Metal.GRACE_UNTIL);
        TemporaryEntityData.remove(player.getUuid(), MetadataKeys.Metal.DASH_STUN);

        // Set dashing state for 15 ticks (0.75 seconds)
        TemporaryEntityData.putLong(
                player.getUuid(),
                MetadataKeys.Metal.DASHING_UNTIL,
                System.currentTimeMillis() + 750L
        );

        player.sendMessage(Text.literal("Metal Dash!")
                .formatted(Formatting.GRAY));

        return true;
    }
}

