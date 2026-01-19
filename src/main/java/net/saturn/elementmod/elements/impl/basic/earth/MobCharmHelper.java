package net.saturn.elementmod.elements.impl.basic.earth;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryEntityData;

/**
 * Helper class for mob charm functionality
 */
public class MobCharmHelper {
    /**
     * Called when player punches an entity while charm is active
     */
    public static void charmMob(ServerPlayerEntity player, LivingEntity target) {
        if (!(target instanceof MobEntity mob)) {
            return;
        }

        // Mark mob as charmed
        TemporaryEntityData.putString(mob.getUuid(), MetadataKeys.Earth.CHARMED_OWNER, player.getUuidAsString());
        TemporaryEntityData.putLong(
                mob.getUuid(),
                MetadataKeys.Earth.CHARMED_UNTIL,
                System.currentTimeMillis() + Constants.Duration.EARTH_CHARM_MS
        );

        // Make mob follow player
        mob.setTarget(null);

        // Add glowing effect to show it's charmed
        mob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 600, 0)); // 30 seconds

        player.sendMessage(Text.literal("Charmed " + mob.getName().getString() + "!")
                .formatted(Formatting.GREEN));
    }
}

