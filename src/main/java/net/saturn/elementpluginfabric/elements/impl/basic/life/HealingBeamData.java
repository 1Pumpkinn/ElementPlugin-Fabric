package net.saturn.elementpluginfabric.elements.impl.basic.life;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.elements.ElementContext;

import java.util.List;

/**
 * Data class for tracking active healing beams
 */
public class HealingBeamData {
    private final ServerPlayer player;
    private final ElementContext context;
    private int ticks = 0;
    private static final int DURATION = 100; // 5 seconds
    private static final float HEAL_PER_SECOND = 2.0f; // 1 heart

    public HealingBeamData(ServerPlayer player, ElementContext context) {
        this.player = player;
        this.context = context;
    }

    public void tick() {
        if (player.hasDisconnected() || player.isDeadOrDying()) {
            return;
        }

        ticks++;

        // Heal every second (20 ticks)
        if (ticks % 20 == 0) {
            // Find trusted allies in beam direction
            List<ServerPlayer> possibleTargets = player.level().getEntitiesOfClass(
                    ServerPlayer.class,
                    player.getBoundingBox().inflate(20),
                    p -> p != player &&
                            !p.isSpectator() &&
                            context.getTrustManager().isTrusted(player.getUUID(), p.getUUID())
            );

            // Find closest ally in beam path
            var start = player.getEyePosition();
            var direction = player.getLookAngle();

            ServerPlayer target = null;
            double closestDist = Double.MAX_VALUE;

            for (ServerPlayer ally : possibleTargets) {
                var allyPos = ally.position().add(0, ally.getBbHeight() / 2, 0);
                var toAlly = allyPos.subtract(start);
                double dot = toAlly.normalize().dot(direction);

                if (dot > 0.95) {
                    double dist = toAlly.length();
                    if (dist < closestDist) {
                        closestDist = dist;
                        target = ally;
                    }
                }
            }

            if (target != null) {
                target.heal(HEAL_PER_SECOND);
                target.sendSystemMessage(Component.literal("✚ Healing...")
                        .withStyle(ChatFormatting.GREEN), true);
            }
        }

        // Visual feedback
        if (ticks % 10 == 0) {
            player.sendSystemMessage(Component.literal("✚ Healing beam active... " +
                            ((DURATION - ticks) / 20) + "s remaining")
                    .withStyle(ChatFormatting.GREEN), true);
        }
    }

    public boolean isExpired() {
        return ticks >= DURATION || player.hasDisconnected() || player.isDeadOrDying();
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}

