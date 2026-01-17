package net.saturn.elementpluginfabric.elements.impl.basic.fire;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Random;

/**
 * Data class for tracking active meteor showers
 */
public class MeteorShowerData {
    private final ServerPlayer player;
    private int ticks = 0;
    private int meteorsSpawned = 0;
    private static final int TOTAL_METEORS = 18;
    private static final int DURATION = 120; // 6 seconds
    private final Random random = new Random();

    public MeteorShowerData(ServerPlayer player) {
        this.player = player;
    }

    public void tick() {
        if (player.hasDisconnected() || player.isDeadOrDying()) {
            return;
        }

        ticks++;

        // Spawn meteor every ~0.33 seconds (6-7 ticks)
        if (ticks % 7 == 0 && meteorsSpawned < TOTAL_METEORS) {
            spawnMeteor();
            meteorsSpawned++;
        }
    }

    private void spawnMeteor() {
        Vec3 playerPos = player.position();

        // Random position in radius around player
        double angle = random.nextDouble() * 2 * Math.PI;
        double radius = 3 + random.nextDouble() * 7; // 3-10 block radius

        double x = playerPos.x + Math.cos(angle) * radius;
        double z = playerPos.z + Math.sin(angle) * radius;
        double y = playerPos.y + 15; // Spawn 15 blocks above

        // Direction: downward with slight angle towards center
        Vec3 targetPos = new Vec3(x - (Math.cos(angle) * 2), playerPos.y, z - (Math.sin(angle) * 2));
        Vec3 direction = targetPos.subtract(new Vec3(x, y, z)).normalize();

        LargeFireball fireball = new LargeFireball(
                EntityType.FIREBALL,
                player.level()
        );
        fireball.setOwner(player);
        fireball.setPos(x, y, z);
        fireball.setDeltaMovement(direction.x * 0.1, direction.y * 0.1, direction.z * 0.1);
        
        player.level().addFreshEntity(fireball);
    }

    public boolean isExpired() {
        return ticks >= DURATION || meteorsSpawned >= TOTAL_METEORS ||
                player.hasDisconnected() || player.isDeadOrDying();
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}

