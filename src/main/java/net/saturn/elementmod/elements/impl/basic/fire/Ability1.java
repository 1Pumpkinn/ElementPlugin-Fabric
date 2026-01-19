package net.saturn.elementmod.elements.impl.basic.fire;

import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

/**
 * Fireball - Launch an explosive fireball
 */
public class Ability1 {
    private final ElementMod plugin;

    public Ability1(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.FIRE);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Spawn fireball
        Vec3d lookVec = player.getRotationVec(1.0f).normalize();
        Vec3d spawnPos = player.getEyePos().add(lookVec.multiply(1.5));

        FireballEntity fireball = new FireballEntity(player.getEntityWorld(), player, lookVec, 1);
        fireball.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);

        player.getEntityWorld().spawnEntity(fireball);

        player.sendMessage(Text.literal("Fireball launched!")
                .formatted(Formatting.GOLD));

        return true;
    }
}