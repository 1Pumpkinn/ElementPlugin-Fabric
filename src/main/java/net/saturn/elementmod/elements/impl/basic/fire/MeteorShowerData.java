package net.saturn.elementmod.elements.impl.basic.fire;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * Data class for tracking active meteor showers
 */
public class MeteorShowerData {
    private final ServerPlayerEntity player;
    private int ticks = 0;
    private int meteorsSpawned = 0;
    private static final int TOTAL_METEORS = 18;
    private static final int DURATION = 120; // 6 seconds
    private final Random random = new Random();

    public MeteorShowerData(ServerPlayerEntity player) {
        this.player = player;
    }

    public void tick() {
        if (player.isRemoved() || player.isDead()) {
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
        Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());

        // Random position in radius around player
        double angle = random.nextDouble() * 2 * Math.PI;
        double radius = 3 + random.nextDouble() * 7; // 3-10 block radius

        double x = playerPos.x + Math.cos(angle) * radius;
        double z = playerPos.z + Math.sin(angle) * radius;
        double y = playerPos.y + 15; // Spawn 15 blocks above

        // Direction: downward with slight angle towards center
        Vec3d targetPos = new Vec3d(x - (Math.cos(angle) * 2), playerPos.y, z - (Math.sin(angle) * 2));
        Vec3d direction = targetPos.subtract(new Vec3d(x, y, z)).normalize();

        FireballEntity fireball = new FireballEntity(
                player.getEntityWorld(),
                player,
                direction.multiply(0.1),
                1
        );
        fireball.setOwner(player);
        fireball.setPos(x, y, z);
        
        player.getEntityWorld().spawnEntity(fireball);
    }

    public boolean isExpired() {
        return ticks >= DURATION || meteorsSpawned >= TOTAL_METEORS ||
                player.isRemoved() || player.isDead();
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
}

