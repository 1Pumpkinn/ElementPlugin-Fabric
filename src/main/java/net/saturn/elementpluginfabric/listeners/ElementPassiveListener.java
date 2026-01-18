package net.saturn.elementpluginfabric.listeners;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.PlayerData;
import net.saturn.elementpluginfabric.data.TemporaryPlayerData;
import net.saturn.elementpluginfabric.data.TemporaryEntityData;
import net.saturn.elementpluginfabric.elements.ElementType;

import java.util.List;

/**
 * Handles all passive element effects that require event listeners
 */
public class ElementPassiveListener {
    private final ElementPluginFabric plugin;

    public ElementPassiveListener(ElementPluginFabric plugin) {
        this.plugin = plugin;
        registerAllListeners();
    }

    private void registerAllListeners() {
        registerDamageListener();
        registerAttackListener();
        registerBlockBreakListener();
        registerTickListener();
        registerMobTickListener();
    }

    private void registerDamageListener() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return true; // Allow damage for non-players
            }

            PlayerData pd = plugin.getDataStore().getPlayerData(player.getUUID());
            ElementType element = pd.getCurrentElement();
            if (element == null) {
                return true;
            }

            // AIR: No fall damage
            if (element == ElementType.AIR && source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
                return false; // Cancel fall damage
            }

            // METAL Upgrade 2: Arrow immunity
            if (element == ElementType.METAL && pd.getCurrentElementUpgradeLevel() >= 2) {
                if (source.getDirectEntity() instanceof AbstractArrow) {
                    return false; // Cancel arrow damage
                }
            }

            return true; // Allow damage
        });
    }

    private void registerAttackListener() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            if (!(entity instanceof LivingEntity target)) {
                return InteractionResult.PASS;
            }

            PlayerData pd = plugin.getDataStore().getPlayerData(serverPlayer.getUUID());
            ElementType element = pd.getCurrentElement();
            if (element == null) {
                return InteractionResult.PASS;
            }

            // FIRE Upgrade 2: Fire Aspect on attacks
            if (element == ElementType.FIRE && pd.getCurrentElementUpgradeLevel() >= 2) {
                target.setRemainingFireTicks(80); // 4 seconds = 80 ticks
            }

            // AIR Upgrade 2: 5% chance to apply Slow Falling
            if (element == ElementType.AIR && pd.getCurrentElementUpgradeLevel() >= 2) {
                if (Math.random() < 0.05) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0)); // 5 seconds
                }
            }

            // EARTH: Mob Charm
            long charmUntil = TemporaryPlayerData.getLong(serverPlayer.getUUID(), MetadataKeys.Earth.CHARM_NEXT_UNTIL);
            if (element == ElementType.EARTH && charmUntil > System.currentTimeMillis()) {
                TemporaryPlayerData.remove(serverPlayer.getUUID(), MetadataKeys.Earth.CHARM_NEXT_UNTIL);
                net.saturn.elementpluginfabric.elements.impl.basic.earth.MobCharmHelper.charmMob(serverPlayer, target);
            }

            // FROST: Frozen Punch
            long frozenPunchUntil = TemporaryPlayerData.getLong(serverPlayer.getUUID(), MetadataKeys.Frost.FROZEN_PUNCH_READY);
            if (element == ElementType.FROST && frozenPunchUntil > System.currentTimeMillis()) {
                TemporaryPlayerData.remove(serverPlayer.getUUID(), MetadataKeys.Frost.FROZEN_PUNCH_READY);
                net.saturn.elementpluginfabric.elements.impl.basic.frost.FreezeHelper.freezeEntity(target);
                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("Frozen!")
                        .withStyle(net.minecraft.ChatFormatting.AQUA));
            }

            return InteractionResult.PASS;
        });
    }

    private void registerBlockBreakListener() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }

            PlayerData pd = plugin.getDataStore().getPlayerData(serverPlayer.getUUID());
            ElementType element = pd.getCurrentElement();
            if (element == null) {
                return;
            }

            // EARTH Upgrade 2 or Earth Tunnel: Double ore drops
            boolean isEarthUpgrade2 = element == ElementType.EARTH && pd.getCurrentElementUpgradeLevel() >= 2;
            long mineUntil = TemporaryPlayerData.getLong(serverPlayer.getUUID(), MetadataKeys.Earth.MINE_UNTIL);
            boolean isEarthTunnel = element == ElementType.EARTH && mineUntil > System.currentTimeMillis();

            if (isEarthUpgrade2 || isEarthTunnel) {
                ItemStack tool = serverPlayer.getMainHandItem();

                // Check if tool has Silk Touch
                var enchantmentRegistry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                var silkTouchHolder = enchantmentRegistry.getOrThrow(Enchantments.SILK_TOUCH);
                if (EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder, tool) > 0) {
                    return; // Don't double if Silk Touch
                }

                // Check if block is an ore
                Block block = state.getBlock();
                if (isOre(block)) {
                    // Drop extra items
                    Block.dropResources(state, world, pos, blockEntity, serverPlayer, tool);
                }
            }
        });
    }

    private void registerTickListener() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PlayerData pd = plugin.getDataStore().getPlayerData(player.getUUID());
                ElementType element = pd.getCurrentElement();
                if (element == null) continue;

                // FROST Passive 1: Speed II when wearing leather boots
                if (element == ElementType.FROST) {
                    ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
                    if (boots.is(Items.LEATHER_BOOTS)) {
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1, true, false));
                    }

                    // FROST Passive 2 (Upgrade II): Speed III on ice
                    if (pd.getCurrentElementUpgradeLevel() >= 2) {
                        BlockPos below = player.blockPosition().below();
                        BlockState blockBelow = player.level().getBlockState(below);
                        if (blockBelow.is(Blocks.ICE) || blockBelow.is(Blocks.PACKED_ICE) || blockBelow.is(Blocks.BLUE_ICE)) {
                            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 2, true, false));
                        }
                    }
                }

                // LIFE Upgrade 2: Auto-grow crops in 5-block radius
                if (element == ElementType.LIFE) {
                    // Passive 1: Faster natural regeneration (0.5 heart every 2 seconds)
                    if (server.getTickCount() % 40 == 0 && player.getHealth() < player.getMaxHealth()) {
                        player.heal(1.0f);
                    }

                    if (pd.getCurrentElementUpgradeLevel() >= 2) {
                        if (server.getTickCount() % 20 == 0) { // Every second
                            BlockPos playerPos = player.blockPosition();
                            for (int x = -5; x <= 5; x++) {
                                for (int y = -2; y <= 2; y++) {
                                    for (int z = -5; z <= 5; z++) {
                                        BlockPos cropPos = playerPos.offset(x, y, z);
                                        BlockState cropState = player.level().getBlockState(cropPos);
                                        if (cropState.getBlock() instanceof CropBlock crop) {
                                            if (!crop.isMaxAge(cropState)) {
                                                player.level().setBlock(cropPos, crop.getStateForAge(crop.getMaxAge()), 2);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // AIR Upgrade 2: Super Jump
                if (element == ElementType.AIR && pd.getCurrentElementUpgradeLevel() >= 2) {
                    // Detect jump server-side
                    boolean isJumping = player.isJumping();
                    boolean wasOnGround = TemporaryPlayerData.getBoolean(player.getUUID(), "was_on_ground");
                    
                    if (isJumping && wasOnGround) {
                        net.minecraft.world.phys.Vec3 velocity = player.getDeltaMovement();
                        player.setDeltaMovement(velocity.x, 0.8, velocity.z); // High jump
                        player.hurtMarked = true;
                    }
                    TemporaryPlayerData.putBoolean(player.getUUID(), "was_on_ground", player.onGround());
                }

                // DEATH: Food effects
                if (element == ElementType.DEATH) {
                    // Check if player is finishing eating
                    if (player.isUsingItem() && player.getUseItemRemainingTicks() == 1) {
                        ItemStack item = player.getUseItem();
                        if (isUndeadFood(item)) {
                            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 3)); // Reg IV for 5s
                            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0)); // Absorption for 2m
                        }
                    }
                }

                // DEATH Upgrade 2: Nearby enemies get Hunger I
                if (element == ElementType.DEATH && pd.getCurrentElementUpgradeLevel() >= 2) {
                    if (server.getTickCount() % 40 == 0) { // Every 2 seconds
                        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                                LivingEntity.class,
                                player.getBoundingBox().inflate(5.0),
                                e -> e != player &&
                                        !e.isSpectator() &&
                                        plugin.getValidationService().isValidTarget(player, e)
                        );

                        for (LivingEntity entity : nearbyEntities) {
                            entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));
                        }
                    }
                }
            }
        });
    }

    private void registerMobTickListener() {
        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            if (!(world instanceof ServerLevel serverWorld)) return;
            
            for (net.minecraft.world.entity.Entity entity : serverWorld.getAllEntities()) {
                if (!(entity instanceof Mob mob)) continue;
                
                String ownerUuidStr = TemporaryEntityData.getString(mob.getUUID(), MetadataKeys.Earth.CHARMED_OWNER);
                if (ownerUuidStr == null) {
                    ownerUuidStr = TemporaryEntityData.getString(mob.getUUID(), MetadataKeys.Death.SUMMONED_OWNER);
                }

                if (ownerUuidStr != null) {
                    long until = TemporaryEntityData.getLong(mob.getUUID(), MetadataKeys.Earth.CHARMED_UNTIL);
                    if (until == 0) {
                        until = TemporaryEntityData.getLong(mob.getUUID(), MetadataKeys.Death.SUMMONED_UNTIL);
                    }

                    if (System.currentTimeMillis() > until) {
                        // Charm/Summon expired
                        boolean isSummoned = TemporaryEntityData.getString(mob.getUUID(), MetadataKeys.Death.SUMMONED_OWNER) != null;
                        
                        TemporaryEntityData.remove(mob.getUUID(), MetadataKeys.Earth.CHARMED_OWNER);
                        TemporaryEntityData.remove(mob.getUUID(), MetadataKeys.Earth.CHARMED_UNTIL);
                        TemporaryEntityData.remove(mob.getUUID(), MetadataKeys.Death.SUMMONED_OWNER);
                        TemporaryEntityData.remove(mob.getUUID(), MetadataKeys.Death.SUMMONED_UNTIL);
                        mob.removeEffect(MobEffects.GLOWING);
                        
                        // If it's a summoned mob, kill it
                        if (isSummoned) {
                             mob.discard();
                        }
                        continue;
                    }
                    
                    // Handle charmed mob behavior
                    try {
                        java.util.UUID ownerUuid = java.util.UUID.fromString(ownerUuidStr);
                        ServerPlayer owner = serverWorld.getServer().getPlayerList().getPlayer(ownerUuid);
                        
                        if (owner != null && owner.level() == mob.level()) {
                            double distSq = mob.distanceToSqr(owner);
                            
                            // Follow owner if too far
                            if (distSq > 100) { // 10 blocks
                                mob.getNavigation().moveTo(owner, 1.2);
                            }
                            
                            // Teleport if way too far
                            if (distSq > 400) { // 20 blocks
                                mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
                            }
                            
                            // Protect owner
                            LivingEntity attacker = owner.getLastHurtByMob();
                            if (attacker != null && attacker.isAlive() && attacker != mob) {
                                mob.setTarget(attacker);
                            }
                            
                            // Attack owner's target
                            LivingEntity ownerTarget = owner.getLastHurtMob();
                            if (ownerTarget != null && ownerTarget.isAlive() && ownerTarget != mob) {
                                mob.setTarget(ownerTarget);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    private boolean isOre(Block block) {
        return block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE ||
                block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
                block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE ||
                block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE ||
                block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE ||
                block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE ||
                block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE ||
                block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE ||
                block == Blocks.NETHER_GOLD_ORE || block == Blocks.NETHER_QUARTZ_ORE;
    }

    private boolean isUndeadFood(ItemStack stack) {
        return stack.is(Items.ROTTEN_FLESH) || 
               stack.is(Items.BEEF) || 
               stack.is(Items.CHICKEN) || 
               stack.is(Items.PORKCHOP) || 
               stack.is(Items.MUTTON) || 
               stack.is(Items.RABBIT) || 
               stack.is(Items.SPIDER_EYE);
    }

    /**
     * Register this listener in ElementPluginFabric.java's registerListeners() method
     */
    public static void register(ElementPluginFabric plugin) {
        new ElementPassiveListener(plugin);
    }
}