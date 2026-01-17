package net.saturn.elementpluginfabric.elements.impl.basic.earth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryEntityData;

/**
 * Helper class for mob charm functionality
 */
public class MobCharmHelper {
    /**
     * Called when player punches an entity while charm is active
     */
    public static void charmMob(ServerPlayer player, LivingEntity target) {
        if (!(target instanceof Mob mob)) {
            return;
        }

        // Mark mob as charmed
        TemporaryEntityData.putString(mob.getUUID(), MetadataKeys.Earth.CHARMED_OWNER, player.getStringUUID());
        TemporaryEntityData.putLong(
                mob.getUUID(),
                MetadataKeys.Earth.CHARMED_UNTIL,
                System.currentTimeMillis() + Constants.Duration.EARTH_CHARM_MS
        );

        // Make mob follow player
        mob.setTarget(null);

        // Add glowing effect to show it's charmed
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0)); // 30 seconds

        player.sendSystemMessage(Component.literal("Charmed " + mob.getName().getString() + "!")
                .withStyle(ChatFormatting.GREEN));
    }
}

