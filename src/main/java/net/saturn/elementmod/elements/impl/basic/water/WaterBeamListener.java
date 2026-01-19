package net.saturn.elementmod.elements.impl.basic.water;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.UUID;

/**
 * Listener for water beam tick events
 */
public class WaterBeamListener {
    private final Map<UUID, WaterBeamData> activeBeams;

    public WaterBeamListener(Map<UUID, WaterBeamData> activeBeams) {
        this.activeBeams = activeBeams;
        startBeamTicker();
    }

    private void startBeamTicker() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            activeBeams.values().removeIf(beam -> {
                beam.tick();
                if (beam.isExpired()) {
                    beam.getPlayer().sendMessage(Text.literal("Water Beam ended!")
                            .formatted(Formatting.GRAY));
                    return true;
                }
                return false;
            });
        });
    }
}

