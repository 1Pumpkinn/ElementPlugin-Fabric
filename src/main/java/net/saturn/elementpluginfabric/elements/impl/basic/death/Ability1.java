package net.saturn.elementpluginfabric.elements.impl.basic.death;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryEntityData;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

import java.util.Random;

/**
 * Summon Undead - Random undead ally with 20 hearts for 30 seconds
 */
public class Ability1 {
    private final ElementPluginFabric plugin;
    private final Random random = new Random();

    public Ability1(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.DEATH);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        // Random undead type - create using constructors
        Mob mob = switch (random.nextInt(4)) {
            case 0 -> new Zombie(net.minecraft.world.entity.EntityType.ZOMBIE, serverLevel);
            case 1 -> new Skeleton(net.minecraft.world.entity.EntityType.SKELETON, serverLevel);
            case 2 -> new Husk(net.minecraft.world.entity.EntityType.HUSK, serverLevel);
            default -> new Stray(net.minecraft.world.entity.EntityType.STRAY, serverLevel);
        };
        
        if (mob != null) {
            Vec3 spawnPos = player.position().add(
                    Math.cos(player.getYRot() * Math.PI / 180) * 2,
                    0,
                    Math.sin(player.getYRot() * Math.PI / 180) * 2
            );

            mob.setPos(spawnPos);

            // Set health to 20 hearts
            var attr = mob.getAttribute(Attributes.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(40.0);
            }
            mob.setHealth(40.0f);

            // Mark as summoned
            TemporaryEntityData.putString(mob.getUUID(), MetadataKeys.Death.SUMMONED_OWNER, player.getStringUUID());
            TemporaryEntityData.putLong(
                    mob.getUUID(),
                    MetadataKeys.Death.SUMMONED_UNTIL,
                    System.currentTimeMillis() + Constants.Duration.DEATH_SUMMON_MS
            );

            // Add glowing effect
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0));

            player.level().addFreshEntity(mob);

            player.sendSystemMessage(Component.literal("Summoned " + mob.getName().getString() + "!")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }

        return true;
    }
}

