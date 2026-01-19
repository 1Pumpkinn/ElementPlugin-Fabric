package net.saturn.elementmod.elements.impl.basic.life;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.UUID;

/**
 * Listener for healing beam tick events
 */
public class HealingBeamListener {
    private final Map<UUID, HealingBeamData> activeBeams;

    public HealingBeamListener(Map<UUID, HealingBeamData> activeBeams) {
        this.activeBeams = activeBeams;
        startBeamTicker();
    }

    private void startBeamTicker() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            activeBeams.values().removeIf(beam -> {
                beam.tick();
                if (beam.isExpired()) {
                    beam.getPlayer().sendMessage(Text.literal("Healing Beam ended!")
                            .formatted(Formatting.GRAY));
                    return true;
                }
                return false;
            });
        });
    }
}

