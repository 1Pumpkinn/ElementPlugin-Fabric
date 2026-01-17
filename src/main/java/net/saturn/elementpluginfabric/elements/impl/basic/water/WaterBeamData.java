package net.saturn.elementpluginfabric.elements.impl.basic.water;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;

import java.util.List;

/**
 * Data class for tracking active water beams
 */
public class WaterBeamData {
    private final ServerPlayer player;
    private final ElementPluginFabric plugin;
    private int ticks = 0;
    private static final int DURATION = 200; // 10 seconds
    private static final float DAMAGE_PER_TICK = 0.5f; // 10 damage over 200 ticks = 5 hearts

    public WaterBeamData(ServerPlayer player, ElementPluginFabric plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void tick() {
        if (player.hasDisconnected() || player.isDeadOrDying()) {
            return;
        }

        ticks++;

        // Ray trace to find target
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle();
        Vec3 end = start.add(direction.scale(20)); // 20 block range

        List<LivingEntity> possibleTargets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(20),
                entity -> entity != player &&
                        !entity.isSpectator() &&
                        plugin.getValidationService().isValidTarget(player, entity)
        );

        // Find closest entity in beam path
        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : possibleTargets) {
            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            Vec3 toEntity = entityPos.subtract(start);
            double dot = toEntity.normalize().dot(direction);

            if (dot > 0.95) { // Entity is in front and close to beam direction
                double dist = toEntity.length();
                if (dist < closestDist) {
                    closestDist = dist;
                    target = entity;
                }
            }
        }

        if (target != null) {
            target.hurt(player.damageSources().magic(), DAMAGE_PER_TICK);
        }

        // Visual feedback every half second
        if (ticks % 10 == 0) {
            player.sendSystemMessage(Component.literal("⚡ Beam active... " +
                            ((DURATION - ticks) / 20) + "s remaining")
                    .withStyle(ChatFormatting.AQUA), true);
        }
    }

    public boolean isExpired() {
        return ticks >= DURATION || player.hasDisconnected() || player.isDeadOrDying();
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}

