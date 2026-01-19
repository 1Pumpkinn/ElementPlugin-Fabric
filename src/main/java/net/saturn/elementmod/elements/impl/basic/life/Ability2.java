package net.saturn.elementmod.elements.impl.basic.life;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.saturn.elementmod.ElementMod;
import net.saturn.elementmod.elements.ElementContext;
import net.saturn.elementmod.elements.ElementType;
import net.saturn.elementmod.managers.ManaManager;

import java.util.Map;
import java.util.UUID;

/**
 * Healing Beam - Project a continuous healing beam for 5 seconds (1 heart/sec)
 */
public class Ability2 {
    private final ElementMod plugin;
    private final Map<UUID, HealingBeamData> activeBeams;

    public Ability2(ElementMod plugin, Map<UUID, HealingBeamData> activeBeams) {
        this.plugin = plugin;
        this.activeBeams = activeBeams;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.LIFE);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Start healing beam
        activeBeams.put(player.getUuid(), new HealingBeamData(player, context));

        player.sendMessage(Text.literal("Healing Beam activated!")
                .formatted(Formatting.GREEN));

        return true;
    }

    public Map<UUID, HealingBeamData> getActiveBeams() {
        return activeBeams;
    }
}

