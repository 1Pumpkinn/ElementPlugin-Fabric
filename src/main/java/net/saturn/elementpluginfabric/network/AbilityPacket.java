package net.saturn.elementpluginfabric.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.saturn.elementpluginfabric.ElementPluginFabric;

public record AbilityPacket(int abilityNumber) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("elementplugin", "ability");
    public static final Type<AbilityPacket> TYPE = new Type<>(ID);
    
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            AbilityPacket::abilityNumber,
            AbilityPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void register() {
        // Register server-side packet type and handler
        PayloadTypeRegistry.playS2C().register(AbilityPacket.TYPE, AbilityPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(AbilityPacket.TYPE, AbilityPacket.CODEC);
        
        // Register server-side receiver
        ServerPlayNetworking.registerGlobalReceiver(AbilityPacket.TYPE, (packet, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ElementPluginFabric plugin = ElementPluginFabric.getInstance();
                if (plugin != null && plugin.getElementManager() != null) {
                    if (packet.abilityNumber() == 1) {
                        plugin.getElementManager().useAbility1(player);
                    } else if (packet.abilityNumber() == 2) {
                        plugin.getElementManager().useAbility2(player);
                    }
                }
            });
        });
    }
}

