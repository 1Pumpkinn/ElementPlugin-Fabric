package net.saturn.elementmod.elements.impl.basic.death;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryEntityData;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

import java.util.Random;

/**
 * Summon Undead - Random undead ally with 20 hearts for 30 seconds
 */
public class Ability1 {
    private final ElementMod plugin;
    private final Random random = new Random();

    public Ability1(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.DEATH);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        if (!(player.getEntityWorld() instanceof ServerWorld serverLevel)) {
            return false;
        }

        // Random undead type - create using constructors
        MobEntity mob = switch (random.nextInt(4)) {
            case 0 -> new ZombieEntity(net.minecraft.entity.EntityType.ZOMBIE, serverLevel);
            case 1 -> new SkeletonEntity(net.minecraft.entity.EntityType.SKELETON, serverLevel);
            case 2 -> new HuskEntity(net.minecraft.entity.EntityType.HUSK, serverLevel);
            default -> new StrayEntity(net.minecraft.entity.EntityType.STRAY, serverLevel);
        };
        
        if (mob != null) {
            Vec3d spawnPos = new Vec3d(player.getX(), player.getY(), player.getZ()).add(
                    Math.cos(player.getYaw() * Math.PI / 180) * 2,
                    0,
                    Math.sin(player.getYaw() * Math.PI / 180) * 2
            );

            mob.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

            // Set health to 20 hearts
            var attr = mob.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(40.0);
            }
            mob.setHealth(40.0f);

            // Mark as summoned
            TemporaryEntityData.putString(mob.getUuid(), MetadataKeys.Death.SUMMONED_OWNER, player.getUuidAsString());
            TemporaryEntityData.putLong(
                    mob.getUuid(),
                    MetadataKeys.Death.SUMMONED_UNTIL,
                    System.currentTimeMillis() + Constants.Duration.DEATH_SUMMON_MS
            );

            // Add glowing effect
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 600, 0));

            player.getEntityWorld().spawnEntity(mob);

            player.sendMessage(Text.literal("Summoned " + mob.getName().getString() + "!")
                    .formatted(Formatting.DARK_PURPLE));
        }

        return true;
    }
}

