package net.saturn.elementpluginfabric.services;

import net.saturn.elementpluginfabric.data.PlayerData;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.TrustManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;

public class ValidationService {
    private final TrustManager trustManager;

    public ValidationService(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    public boolean isValidTarget(ServerPlayer attacker, LivingEntity target) {
        if (target.equals(attacker)) return false;

        if (target instanceof ServerPlayer targetPlayer) {
            return !trustManager.isTrusted(attacker.getUUID(), targetPlayer.getUUID());
        }

        return true;
    }

    public boolean hasUpgradeLevel(PlayerData pd, int required) {
        return pd.getCurrentElementUpgradeLevel() >= required;
    }

    public boolean canUseElementItem(ServerPlayer player, ElementType itemElement, PlayerData pd) {
        return pd.getCurrentElement() == itemElement;
    }
}