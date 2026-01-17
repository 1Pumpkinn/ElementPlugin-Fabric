package net.saturn.elementpluginfabric.elements.impl.basic.fire;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

/**
 * Fireball - Launch an explosive fireball
 */
public class Ability1 {
    private final ElementPluginFabric plugin;

    public Ability1(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.FIRE);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Spawn fireball
        Vec3 lookVec = player.getLookAngle().normalize();
        Vec3 spawnPos = player.getEyePosition().add(lookVec.scale(1.5));

        LargeFireball fireball = new LargeFireball(EntityType.FIREBALL, player.level());
        fireball.setOwner(player);
        fireball.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        fireball.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5f, 0.0f);

        player.level().addFreshEntity(fireball);

        player.sendSystemMessage(Component.literal("Fireball launched!")
                .withStyle(ChatFormatting.GOLD));

        return true;
    }
}