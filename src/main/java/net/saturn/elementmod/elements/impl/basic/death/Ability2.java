package net.saturn.elementmod.elements.impl.basic.death;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

/**
 * Wither Skull - Fire an explosive wither skull
 */
public class Ability2 {
    private final ElementMod plugin;

    public Ability2(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.DEATH);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        Vec3d lookVec = player.getRotationVec(1.0f);
        Vec3d spawnPos = player.getEyePos().add(lookVec.multiply(1.5));

        WitherSkullEntity skull = new WitherSkullEntity(player.getEntityWorld(), player, lookVec);
        skull.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        skull.setCharged(true); // Makes it explosive (blue)

        player.getEntityWorld().spawnEntity(skull);

        player.sendMessage(Text.literal("Wither Skull launched!")
                .formatted(Formatting.DARK_PURPLE));

        return true;
    }
}

