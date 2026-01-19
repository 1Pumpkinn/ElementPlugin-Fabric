package net.saturn.elementmod.elements.impl.basic.water;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.saturn.elementmod.ElementMod;

import java.util.List;

/**
 * Data class for tracking active water beams
 */
public class WaterBeamData {
    private final ServerPlayerEntity player;
    private final ElementMod plugin;
    private int ticks = 0;
    private static final int DURATION = 200; // 10 seconds
    private static final float DAMAGE_PER_TICK = 0.5f; // 10 damage over 200 ticks = 5 hearts

    public WaterBeamData(ServerPlayerEntity player, ElementMod plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void tick() {
        if (player.isRemoved() || player.isDead()) {
            return;
        }

        ticks++;

        // Ray trace to find target
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(20)); // 20 block range

        List<LivingEntity> possibleTargets = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(20),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        // Find closest entity in beam path
        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : possibleTargets) {
            Vec3d entityPos = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ());
            Vec3d toEntity = entityPos.subtract(start);
            double dot = toEntity.normalize().dotProduct(direction);

            if (dot > 0.95) { // Entity is in front and close to beam direction
                double dist = toEntity.length();
                if (dist < closestDist) {
                    closestDist = dist;
                    target = entity;
                }
            }
        }

        if (target != null) {
            if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
                target.damage(serverWorld, player.getDamageSources().magic(), DAMAGE_PER_TICK);
            }
            // Add particles at target
            if (player.getEntityWorld() instanceof ServerWorld serverLevel) {
                serverLevel.spawnParticles(ParticleTypes.BUBBLE, target.getX(), target.getY() + 1, target.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
            }
        }

        // Draw beam particles
        if (player.getEntityWorld() instanceof ServerWorld serverLevel) {
            for (double i = 1; i < 20; i += 0.5) {
                Vec3d point = start.add(direction.multiply(i));
                serverLevel.spawnParticles(ParticleTypes.DRIPPING_WATER, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
        }

        // Visual feedback every half second
        if (ticks % 10 == 0) {
            player.sendMessage(Text.literal("⚡ Beam active... " +
                            ((DURATION - ticks) / 20) + "s remaining")
                    .formatted(Formatting.AQUA), true);
        }
    }

    public boolean isExpired() {
        return ticks >= DURATION || player.isRemoved() || player.isDead();
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
}

