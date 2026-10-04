package io.github.derkottersberg.seamlessdogs.network;

import io.github.derkottersberg.seamlessdogs.SeamlessDogs;
import net.minecraft.network.FriendlyByteBuf;
import io.github.derkottersberg.seamlessdogs.network.PacketCodec;
import io.github.derkottersberg.seamlessdogs.internal.DogsPayload;

/** Fixed-size target identity. Clients cannot select duration, sounds, or ownership. */
public record PetRequest(int dogId) implements DogsPayload {
    public static final Type<PetRequest> TYPE = new Type<>(SeamlessDogs.id("pet"));
    public static final PacketCodec<PetRequest> CODEC = PacketCodec.of(
        (buffer, packet) -> buffer.writeInt(packet.dogId), buffer -> new PetRequest(buffer.readInt()));
    public Type<? extends DogsPayload> type() { return TYPE; }
}
