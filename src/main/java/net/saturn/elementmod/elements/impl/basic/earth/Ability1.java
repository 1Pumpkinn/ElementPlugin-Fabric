package net.saturn.elementmod.elements.impl.basic.earth;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.config.Constants;
import net.saturn.elementmod.config.MetadataKeys;
import net.saturn.elementmod.data.TemporaryPlayerData;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

/**
 * Earth Tunnel - Dig through stone/dirt/ores for 20 seconds
 */
public class Ability1 {
    private final ElementMod plugin;

    public Ability1(ElementMod plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.EARTH);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Give haste 5 for fast mining + night vision to see in tunnels
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 400, 4)); // 20 seconds, Haste V
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0)); // 20 seconds

        // Store metadata for double drops during tunnel
        TemporaryPlayerData.putLong(
                player.getUuid(),
                MetadataKeys.Earth.MINE_UNTIL,
                System.currentTimeMillis() + Constants.Duration.EARTH_TUNNEL_MS
        );

        player.sendMessage(Text.literal("Earth Tunnel activated! Mine quickly!")
                .formatted(Formatting.GREEN));

        return true;
    }
}

