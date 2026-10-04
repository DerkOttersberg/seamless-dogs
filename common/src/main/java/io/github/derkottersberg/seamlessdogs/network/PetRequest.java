package io.github.derkottersberg.seamlessdogs.network;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Fixed-size target identity. Clients cannot select duration, sounds, or ownership. */
public record PetRequest(int dogId) implements CustomPacketPayload {
    public static final Type<PetRequest> TYPE = new Type<>(SeamlessDogs.id("pet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PetRequest> CODEC = StreamCodec.of(
        (buffer, packet) -> buffer.writeInt(packet.dogId), buffer -> new PetRequest(buffer.readInt()));
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
