package net.saturn.elementmod.listeners;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.PlayerData;
import net.saturn.elementmod.data.TemporaryEntityData;
import net.saturn.elementmod.data.TemporaryPlayerData;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.mixin.LivingEntityAccessor;

import java.util.List;
import java.util.UUID;

public class ElementPassiveListener {

    private final ElementMod plugin;

    public ElementPassiveListener(ElementMod plugin) {
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

    /* -------------------------------------------------- */
    /* DAMAGE                                             */
    /* -------------------------------------------------- */

    private void registerDamageListener() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return true;

            PlayerData pd = plugin.getDataStore().getPlayerData(player.getUuid());
            ElementType element = pd.getCurrentElement();
            if (element == null) return true;

            if (element == ElementType.AIR && source.isIn(DamageTypeTags.IS_FALL)) {
                return false;
            }

            if (element == ElementType.METAL && pd.getCurrentElementUpgradeLevel() >= 2) {
                if (source.getSource() instanceof PersistentProjectileEntity) {
                    return false;
                }
            }

            return true;
        });
    }

    /* -------------------------------------------------- */
    /* ATTACK                                             */
    /* -------------------------------------------------- */

    private void registerAttackListener() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;

            PlayerData pd = plugin.getDataStore().getPlayerData(serverPlayer.getUuid());
            ElementType element = pd.getCurrentElement();
            if (element == null) return ActionResult.PASS;

            if (element == ElementType.FIRE && pd.getCurrentElementUpgradeLevel() >= 2) {
                target.setFireTicks(80);
            }

            if (element == ElementType.AIR && pd.getCurrentElementUpgradeLevel() >= 2 && Math.random() < 0.05) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100));
            }

            long charmUntil = TemporaryPlayerData.getLong(serverPlayer.getUuid(), MetadataKeys.Earth.CHARM_NEXT_UNTIL);
            if (element == ElementType.EARTH && charmUntil > System.currentTimeMillis()) {
                TemporaryPlayerData.remove(serverPlayer.getUuid(), MetadataKeys.Earth.CHARM_NEXT_UNTIL);
                net.saturn.elementmod.elements.impl.basic.earth.MobCharmHelper.charmMob(serverPlayer, target);
            }

            long frozenPunchUntil = TemporaryPlayerData.getLong(serverPlayer.getUuid(), MetadataKeys.Frost.FROZEN_PUNCH_READY);
            if (element == ElementType.FROST && frozenPunchUntil > System.currentTimeMillis()) {
                TemporaryPlayerData.remove(serverPlayer.getUuid(), MetadataKeys.Frost.FROZEN_PUNCH_READY);
                net.saturn.elementmod.elements.impl.basic.frost.FreezeHelper.freezeEntity(target);
                serverPlayer.sendMessage(Text.literal("Frozen!").formatted(Formatting.AQUA));
            }

            return ActionResult.PASS;
        });
    }

    /* -------------------------------------------------- */
    /* BLOCK BREAK                                        */
    /* -------------------------------------------------- */

    private void registerBlockBreakListener() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

            PlayerData pd = plugin.getDataStore().getPlayerData(serverPlayer.getUuid());
            ElementType element = pd.getCurrentElement();
            if (element == null) return;

            boolean earthUpgrade = element == ElementType.EARTH && pd.getCurrentElementUpgradeLevel() >= 2;
            boolean earthTunnel = TemporaryPlayerData.getLong(serverPlayer.getUuid(), MetadataKeys.Earth.MINE_UNTIL) > System.currentTimeMillis();
            if (!earthUpgrade && !earthTunnel) return;

            ItemStack tool = serverPlayer.getMainHandStack();

            var enchantmentRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            var silkTouchEntry = enchantmentRegistry.getOptional(Enchantments.SILK_TOUCH);

            if (silkTouchEntry.isPresent() && EnchantmentHelper.getLevel(silkTouchEntry.get(), tool) > 0) {
                return;
            }

            if (isOre(state.getBlock())) {
                Block.dropStacks(state, world, pos, blockEntity, serverPlayer, tool);
            }
        });
    }

    /* -------------------------------------------------- */
    /* PLAYER TICK                                        */
    /* -------------------------------------------------- */

    private void registerTickListener() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                PlayerData pd = plugin.getDataStore().getPlayerData(player.getUuid());
                ElementType element = pd.getCurrentElement();
                if (element == null) continue;

                if (element == ElementType.FROST) {
                    if (player.getEquippedStack(EquipmentSlot.FEET).isOf(Items.LEATHER_BOOTS)) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 1, true, false));
                    }

                    if (pd.getCurrentElementUpgradeLevel() >= 2) {
                        Block block = player.getEntityWorld().getBlockState(player.getBlockPos().down()).getBlock();
                        if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE) {
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 2, true, false));
                        }
                    }
                }

                if (element == ElementType.LIFE) {
                    if (server.getTicks() % 40 == 0 && player.getHealth() < player.getMaxHealth()) {
                        player.heal(1.0f);
                    }

                    if (pd.getCurrentElementUpgradeLevel() >= 2 && server.getTicks() % 20 == 0) {
                        BlockPos base = player.getBlockPos();
                        for (int x = -5; x <= 5; x++) {
                            for (int y = -2; y <= 2; y++) {
                                for (int z = -5; z <= 5; z++) {
                                    BlockPos pos = base.add(x, y, z);
                                    BlockState state = player.getEntityWorld().getBlockState(pos);

                                    if (state.getBlock() instanceof CropBlock crop) {
                                        if (state.get(CropBlock.AGE) < crop.getMaxAge()) {
                                            player.getEntityWorld().setBlockState(
                                                    pos,
                                                    crop.withAge(crop.getMaxAge()),
                                                    Block.NOTIFY_LISTENERS
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (element == ElementType.AIR && pd.getCurrentElementUpgradeLevel() >= 2) {
                    boolean isJumping = ((LivingEntityAccessor) player).isJumpingField();
                    boolean wasOnGround = TemporaryPlayerData.getBoolean(player.getUuid(), "was_on_ground");

                    if (isJumping && wasOnGround) {
                        Vec3d v = player.getVelocity();
                        player.setVelocity(v.x, 0.8, v.z);
                        player.velocityModified = true;
                        player.getEntityWorld().playSound(
                                null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.PLAYERS, 0.5f, 1.5f
                        );
                    }
                    TemporaryPlayerData.putBoolean(player.getUuid(), "was_on_ground", player.isOnGround());
                }

                if (element == ElementType.DEATH && pd.getCurrentElementUpgradeLevel() >= 2 && server.getTicks() % 40 == 0) {
                    List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                            LivingEntity.class,
                            player.getBoundingBox().expand(5),
                            e -> e != player && plugin.getValidationService().isValidTarget(player, e)
                    );

                    for (LivingEntity e : entities) {
                        e.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 100));
                    }
                }
            }
        });
    }

    /* -------------------------------------------------- */
    /* MOB TICK                                           */
    /* -------------------------------------------------- */

    private void registerMobTickListener() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!(world instanceof ServerWorld serverWorld)) return;

            for (Entity entity : serverWorld.iterateEntities()) {
                if (!(entity instanceof MobEntity mob)) continue;

                String ownerStr = TemporaryEntityData.getString(mob.getUuid(), MetadataKeys.Earth.CHARMED_OWNER);
                if (ownerStr == null) continue;

                long until = TemporaryEntityData.getLong(mob.getUuid(), MetadataKeys.Earth.CHARMED_UNTIL);
                if (System.currentTimeMillis() > until) {
                    TemporaryEntityData.remove(mob.getUuid(), MetadataKeys.Earth.CHARMED_OWNER);
                    TemporaryEntityData.remove(mob.getUuid(), MetadataKeys.Earth.CHARMED_UNTIL);
                    mob.removeStatusEffect(StatusEffects.GLOWING);
                    continue;
                }

                ServerPlayerEntity owner = serverWorld.getServer().getPlayerManager().getPlayer(UUID.fromString(ownerStr));
                if (owner != null && mob.squaredDistanceTo(owner) > 100) {
                    mob.getNavigation().startMovingTo(owner, 1.2);
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

    public static void register(ElementMod plugin) {
        new ElementPassiveListener(plugin);
    }
}
