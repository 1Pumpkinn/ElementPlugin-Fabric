package net.saturn.elementmod.elements.impl.basic.life;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.saturn.elementmod.elements.ElementContext;

import java.util.List;

/**
 * Data class for tracking active healing beams
 */
public class HealingBeamData {
    private final ServerPlayerEntity player;
    private final ElementContext context;
    private int ticks = 0;
    private static final int DURATION = 100; // 5 seconds
    private static final float HEAL_PER_SECOND = 2.0f; // 1 heart

    public HealingBeamData(ServerPlayerEntity player, ElementContext context) {
        this.player = player;
        this.context = context;
    }

    public void tick() {
        if (player.isRemoved() || player.isDead()) {
            return;
        }

        ticks++;

        // Heal every second (20 ticks)
        if (ticks % 20 == 0) {
            // Find trusted allies in beam direction
            List<ServerPlayerEntity> possibleTargets = player.getEntityWorld().getEntitiesByClass(
                    ServerPlayerEntity.class,
                    player.getBoundingBox().expand(20),
                    p -> p != player &&
                            !p.isSpectator() &&
                            context.getTrustManager().isTrusted(player.getUuid(), p.getUuid())
            );

            // Find closest ally in beam path
            var start = player.getEyePos();
            var direction = player.getRotationVec(1.0f);

            ServerPlayerEntity target = null;
            double closestDist = Double.MAX_VALUE;

            for (ServerPlayerEntity ally : possibleTargets) {
                var allyPos = new Vec3d(ally.getX(), ally.getY() + ally.getHeight() / 2, ally.getZ());
                var toAlly = allyPos.subtract(start);
                double dot = toAlly.normalize().dotProduct(direction);

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
                target.sendMessage(Text.literal("✚ Healing...")
                        .formatted(Formatting.GREEN), true);
                
                // Add particles at target
                if (player.getEntityWorld() instanceof ServerWorld serverLevel) {
                    serverLevel.spawnParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1, target.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
                }
            }
        }

        // Draw beam particles
        if (player.getEntityWorld() instanceof ServerWorld serverLevel) {
            var start = player.getEyePos();
            var direction = player.getRotationVec(1.0f);
            for (double i = 1; i < 20; i += 0.5) {
                Vec3d point = start.add(direction.multiply(i));
                serverLevel.spawnParticles(ParticleTypes.HEART, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
        }

        // Visual feedback
        if (ticks % 10 == 0) {
            player.sendMessage(Text.literal("✚ Healing beam active... " +
                            ((DURATION - ticks) / 20) + "s remaining")
                    .formatted(Formatting.GREEN), true);
        }
    }

    public boolean isExpired() {
        return ticks >= DURATION || player.isRemoved() || player.isDead();
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
}

