package net.saturn.elementpluginfabric.services;

import net.saturn.elementpluginfabric.data.PlayerData;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.TrustManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class ValidationService {
    private final TrustManager trustManager;

    public ValidationService(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    public boolean isValidTarget(ServerPlayerEntity attacker, LivingEntity target) {
        if (target.equals(attacker)) return false;

        if (target instanceof ServerPlayerEntity targetPlayer) {
            return !trustManager.isTrusted(attacker.getUuid(), targetPlayer.getUuid());
        }

        return true;
    }

    public boolean hasUpgradeLevel(PlayerData pd, int required) {
        return pd.getCurrentElementUpgradeLevel() >= required;
    }

    public boolean canUseElementItem(ServerPlayerEntity player, ElementType itemElement, PlayerData pd) {
        return pd.getCurrentElement() == itemElement;
    }
}

