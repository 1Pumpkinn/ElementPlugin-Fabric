package net.saturn.elementpluginfabric.elements.impl.basic.earth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.saturn.elementpluginfabric.ElementPluginFabric;
import net.saturn.elementpluginfabric.config.Constants;
import net.saturn.elementpluginfabric.config.MetadataKeys;
import net.saturn.elementpluginfabric.data.TemporaryPlayerData;
import net.saturn.elementpluginfabric.elements.ElementContext;
import net.saturn.elementpluginfabric.elements.ElementType;
import net.saturn.elementpluginfabric.managers.ManaManager;

/**
 * Earth Tunnel - Dig through stone/dirt/ores for 20 seconds
 */
public class Ability1 {
    private final ElementPluginFabric plugin;

    public Ability1(ElementPluginFabric plugin) {
        this.plugin = plugin;
    }

    public boolean execute(ElementContext context) {
        ServerPlayer player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility1Cost(ElementType.EARTH);

        if (!manaManager.hasMana(player, cost)) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + cost)
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Give haste 5 for fast mining + night vision to see in tunnels
        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 400, 4)); // 20 seconds, Haste V
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0)); // 20 seconds

        // Store metadata for double drops during tunnel
        TemporaryPlayerData.putLong(
                player.getUUID(),
                MetadataKeys.Earth.MINE_UNTIL,
                System.currentTimeMillis() + Constants.Duration.EARTH_TUNNEL_MS
        );

        player.sendSystemMessage(Component.literal("Earth Tunnel activated! Mine quickly!")
                .withStyle(ChatFormatting.GREEN));

        return true;
    }
}

