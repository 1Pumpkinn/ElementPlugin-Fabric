package net.saturn.elementmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        long now = System.currentTimeMillis();

        // Check if entity is dashing (Metal Dash) - if so, don't stun
        if (TemporaryEntityData.getLong(entity.getUuid(), MetadataKeys.Metal.DASHING_UNTIL) > now) {
            return;
        }

        // Check if entity is stunned by Metal Chain, Metal Dash, or Frost
        // Stuns only apply when on ground or in water (no mid-air freezing)
        if ((entity.isOnGround() || entity.isTouchingWater() || entity.isSubmergedInWater()) &&
            (TemporaryEntityData.getLong(entity.getUuid(), MetadataKeys.Metal.CHAIN_STUN) > now ||
            TemporaryEntityData.getLong(entity.getUuid(), MetadataKeys.Metal.DASH_STUN) > now ||
            TemporaryEntityData.getLong(entity.getUuid(), MetadataKeys.Frost.FROZEN) > now ||
            TemporaryEntityData.getLong(entity.getUuid(), MetadataKeys.Frost.CIRCLE_FROZEN) > now)) {
            
            // Cancel all movement
            entity.setVelocity(Vec3d.ZERO);
            ci.cancel();
        }
    }
}
