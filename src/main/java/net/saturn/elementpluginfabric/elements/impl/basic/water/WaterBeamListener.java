package net.saturn.elementpluginfabric.elements.impl.basic.water;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

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
            for (WaterBeamData beam : activeBeams.values()) {
                beam.tick();
                if (beam.isExpired()) {
                    activeBeams.remove(beam.getPlayer().getUUID());
                    beam.getPlayer().sendSystemMessage(Component.literal("Water Beam ended!")
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        });
    }
}

