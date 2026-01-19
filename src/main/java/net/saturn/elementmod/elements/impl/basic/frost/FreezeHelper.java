package net.saturn.elementmod.elements.impl.basic.frost;

import net.minecraft.entity.LivingEntity;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryEntityData;

/**
 * Helper class for freeze functionality
 */
public class FreezeHelper {
    /**
     * Called when player punches an entity with Frozen Punch ready
     */
    public static void freezeEntity(LivingEntity target) {
        TemporaryEntityData.putLong(
                target.getUuid(),
                MetadataKeys.Frost.FROZEN,
                System.currentTimeMillis() + Constants.Duration.FROST_FREEZE_MS
        );
        // Removed slowness as stun is now handled by LivingEntityMixin
    }
}

