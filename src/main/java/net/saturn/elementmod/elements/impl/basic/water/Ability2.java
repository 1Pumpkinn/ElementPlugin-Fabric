package net.saturn.elementmod.elements.impl.basic.water;

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
 * Water Beam - Fire a continuous damaging water beam (deals 5 hearts total over 10 seconds)
 */
public class Ability2 {
    private final ElementMod plugin;
    private final Map<UUID, WaterBeamData> activeBeams;

    public Ability2(ElementMod plugin, Map<UUID, WaterBeamData> activeBeams) {
        this.plugin = plugin;
        this.activeBeams = activeBeams;
    }

    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ManaManager manaManager = context.getManaManager();

        int cost = context.getConfigManager().getAbility2Cost(ElementType.WATER);

        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana! Need " + cost)
                    .formatted(Formatting.RED));
            return false;
        }

        if (!manaManager.spend(player, cost)) {
            return false;
        }

        // Start water beam
        activeBeams.put(player.getUuid(), new WaterBeamData(player, plugin));

        player.sendMessage(Text.literal("Water Beam activated!")
                .formatted(Formatting.BLUE));

        return true;
    }

    public Map<UUID, WaterBeamData> getActiveBeams() {
        return activeBeams;
    }
}

