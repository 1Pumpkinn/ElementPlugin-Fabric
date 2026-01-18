package net.saturn.elementpluginfabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3 movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        long now = System.currentTimeMillis();

        // Check if entity is dashing (Metal Dash) - if so, don't stun
        if (TemporaryEntityData.getLong(entity.getUUID(), MetadataKeys.Metal.DASHING_UNTIL) > now) {
            return;
        }

        // Check if entity is stunned by Metal Chain, Metal Dash, or Frost
        // Stuns only apply when on ground (no mid-air freezing)
        if (entity.onGround() && (TemporaryEntityData.getLong(entity.getUUID(), MetadataKeys.Metal.CHAIN_STUN) > now ||
            TemporaryEntityData.getLong(entity.getUUID(), MetadataKeys.Metal.DASH_STUN) > now ||
            TemporaryEntityData.getLong(entity.getUUID(), MetadataKeys.Frost.FROZEN) > now ||
            TemporaryEntityData.getLong(entity.getUUID(), MetadataKeys.Frost.CIRCLE_FROZEN) > now)) {
            
            // Cancel all movement
            entity.setDeltaMovement(Vec3.ZERO);
            ci.cancel();
        }
    }
}
