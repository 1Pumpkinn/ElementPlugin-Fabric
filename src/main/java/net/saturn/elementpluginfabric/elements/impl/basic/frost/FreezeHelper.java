package net.saturn.elementpluginfabric.elements.impl.basic.frost;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryEntityData;

/**
 * Helper class for freeze functionality
 */
public class FreezeHelper {
    /**
     * Called when player punches an entity with Frozen Punch ready
     */
    public static void freezeEntity(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 255)); // 5s, can't move
        TemporaryEntityData.putLong(
                target.getUUID(),
                MetadataKeys.Frost.FROZEN,
                System.currentTimeMillis() + Constants.Duration.FROST_FREEZE_MS
        );
    }
}

